# ADR-001: Offline Strategy & Auth/Backend Integration — Field Sales Android App

Status: Decision 1 (offline-first) LOCKED by Swat 10 Oct 2026, via Orchestrator — broadened to
ALL features offline-capable including invoicing, with auto-sync/push on reconnect. Decision 2
(auth/backend) still PROPOSED pending P0 security-status confirmation.
Author: Architect · 10 Oct 2026
Source: BRD.md (Appendix A1, A2, C1, D3, D4, E2-E3, G, R1-R5, Roadmap P0/P5), UIUXDesigner IA-draft-v0.1
Scope confirmed by BRD + IA draft: this app is a native rebuild of the field-sales app
(sales/salesrep/) for Sales Reps and Delivery Drivers only (10-15 internal users), NOT the
D2C consumer app. Admin screens stay on web.

## Decision 1 — Offline capability: offline-first queueing, not "online required"

**Ruling: YES, build offline-first.** Online-required-with-retry is not acceptable for v1.

**UPDATE 10 Oct 2026 (Swat, via Orchestrator): scope broadened and locked.** This is now a
locked product requirement, not a proposal — Swat wants **every feature offline-capable**,
invoicing explicitly included, with auto-sync/push as soon as connectivity returns. The
"Create Invoice while offline" open question below is therefore resolved: yes, invoicing
works offline (see Decision 1b, which already specified how — deferred server-issued
numbering, "pending sync" state). BizAnalyst is turning this into per-screen acceptance
criteria and will work with me directly on sync/conflict-resolution design (e.g. offline
invoice numbering, two reps editing the same outlet concurrently). See the new "Conflict
scenarios" section below, drafted ahead of that conversation.

**Why:**
- BRD non-functional requirement is explicit: "field pages work with a weak signal," and the
  whole reason a native app exists in the roadmap (Phase 5) is that the current web app's
  offline support is only an installable PWA shell with no real write queueing — a named
  gap, not a design choice we should repeat.
- Reps work in retail backrooms with weak 4G (UIUXDesigner's field knowledge, consistent with
  BRD). The core revenue-capture actions (order placement, delivery confirmation, cash
  collection) happen exactly where connectivity is worst. A failed submit here is lost
  revenue or an unrecorded cash handover, not just a bad UX moment.
- Scale is small (10-15 users, one order per outlet per day enforced server-side) so
  sync/conflict complexity is modest — this is not a large distributed-writer problem.

**What must work fully offline (read + queued write):**
| Action | Offline behaviour |
| --- | --- |
| View today's Beat (visit list) | Must be available offline — sync beat plan, outlet list, product/price list, discount rules each time the app is online (on launch + periodically), cache locally. |
| New Order / Edit Order | Queue locally, show "Pending sync" state, submit when connectivity returns. |
| Add Outlet | Queue locally (client-generated temp id), reconcile to server id on sync. |
| Stock Pickup log | Queue locally. |
| Settle (stock settlement) | Queue locally; **do not** generate the resulting invoice number client-side (see Decision 1b). |
| Cash Received | Queue locally, clearly flagged "unsynced" until confirmed, to avoid double-collection confusion at handover/reconciliation. |
| Delivery confirmation (driver) | Queue locally — this is the one driver screen and must not block on signal in a store basement. |
| Create Invoice (ad-hoc, from beat) | Queue locally; number assignment deferred to sync (Decision 1b). |

**Decision 1a — Local store & sync mechanism:**
- Local persistence: Room (SQLite) on-device, mirroring the subset of entities above needed
  for a rep/driver's own day (not a full database replica — scoped to "my beat," "my
  outlets," "my orders," "my pending actions").
- Write queue: every offline-capable action writes to a local outbox table first (action
  type, payload, client-generated UUID idempotency key, created-at, status: pending/syncing/
  failed/synced). A background sync worker (WorkManager, periodic + connectivity-triggered)
  drains the outbox against Supabase (REST/RPC or Edge Functions), in creation order per
  entity.
- Idempotency: every queued write carries a client UUID. Server-side inserts must be made
  idempotent on that key (unique constraint or upsert) so a retried submit after a timeout
  never double-creates an order, delivery, or cash entry. This needs a corresponding change
  on the Supabase/Edge Function side — flagging to Backend/API Developer as an integration
  contract requirement, not just a mobile concern.
- Conflict policy: given "one order per outlet per day" is already a server rule, true
  conflicting edits should be rare. Default policy: server is source of truth; a queued
  edit that the server rejects (e.g. duplicate order) surfaces to the rep as a clear
  in-app conflict needing their decision, never silently dropped or silently overwritten.

**Decision 1b — Invoice numbers stay server-issued, not generated offline:**
BRD E3 is explicit that invoice numbers are issued by the database so two users never
collide, and that a client-side fallback number is a last resort requiring duplicate
checking. The native app must not generate invoice numbers offline. An invoice created
while offline is queued as a draft with a local reference; the real AFF-YYMM-NNN number is
assigned only on successful sync, and the UI must show "Invoice pending — number assigned
once synced" rather than a fake number a rep might read out or write down.

## Decision 2 — Auth & backend: same Supabase project, Supabase Auth, native SDK

**Ruling:** The native app authenticates via **Supabase Auth** (official Android/Kotlin
Supabase SDK) and talks to the **same Supabase project** (fbfopgzblqmsflkfejwa) and schema
as the web apps. No separate backend, no custom auth server — consistent with BRD's
explicit "no framework rewrite" constraint and the fact that this is one shared backend
for all five front ends.

**Why same project / same Auth mechanism:**
- BRD Appendix A1 already describes staff login (admin/rep/driver) going through Supabase
  Auth first, with the profile looked up in the `users` table by auth id. That's the correct
  pattern to mirror natively — it is the roadmap's target state (P0: "Supabase Auth for all
  roles"), not something to reinvent.
- One shared backend keeps data consistent in real time between the web admin portal and the
  phone app (e.g. admin sees a rep's order the moment it syncs) with no second integration
  layer to maintain.

**RESOLVED 10 Oct 2026 (Swat, via Orchestrator) — worst case confirmed:** the Supabase project
is live production (BRD R4: the project labelled "DEV" actually holds production data; no real
staging exists today). Swat has given a hard rule: **no architecture, integration, dev or test
work touches the real Supabase project directly, at any point.** DBEngineer has been asked to
provision a proper isolated dev/staging backend. Until that exists, no mobile write-path work
proceeds past design on paper — this is now a hard gate, not a risk note. I've sent DBEngineer
the schema/parity requirements this architecture needs from that environment (see below).

Original flag for the record: BRD risk R1 states most Supabase tables currently allow the
**anonymous role** full read/write with the key embedded client-side, and R3 flags that
session control is weak. Roadmap Phase 0 (RLS policies per Appendix G, Supabase Auth for all
roles) is meant to close this before Phase 5 (this app) — status of that work is separate
from, and in addition to, the dev/staging gate above.

**Session/error states for UI/UX (answering UIUXDesigner directly):**
- Sign-in: phone/email + password against Supabase Auth, same fields/flow as the existing
  unified login (bilingual EN/HI labels, consistent with web).
- Session storage: Supabase JWT + refresh token stored in Android EncryptedSharedPreferences/
  Keystore-backed storage, not plain prefs.
- **Do not force logout on connectivity loss.** Because this app is offline-first, a rep must
  be able to keep working all day on a valid cached session even if the token can't refresh
  mid-shift. Only force re-auth when the app is online AND the server actively rejects the
  token (expired/revoked), never purely because of "no network."
- Error states to design for: invalid credentials; account inactive (`is_active=false`, mirror
  web message); staff request still pending approval (mirror web "pending approval" message);
  session expired *while online* → re-login prompt; network unreachable → fall back silently
  to cached session + queued-work mode, no scary error.
- Role routing: rep → Rep Home (Beat), driver → Driver Home (Deliveries) — matches
  UIUXDesigner's IA draft exactly, no change needed there.

## Conflict scenarios (drafted for BizAnalyst collaboration)

Now that every feature is offline-capable, here are the concrete conflict cases worth
per-screen acceptance criteria, with a proposed resolution for each — for BizAnalyst to
confirm/adjust against business rules, not for me to unilaterally finalise:

| Scenario | Why it can happen | Proposed resolution |
| --- | --- | --- |
| Two reps create an order for the same outlet, same day, both offline | Each outlet has one assigned rep (BRD D2), so this should be rare, but an outlet reassignment mid-day plus two offline devices could still collide | Server enforces "one order per outlet per day" (existing rule) at sync time. First sync wins; the second queued order is rejected and surfaced to that rep as "An order for this outlet today already exists — review and merge or cancel," never silently dropped. |
| Rep edits an outlet's details (e.g. phone, price tier) offline while admin edits the same outlet on web | Outlet master data can be touched from both web and phone | Last-write-wins by server timestamp for non-financial fields (phone, address, notes); price tier changes always need explicit review on sync if the server copy differs from what the rep's queued edit was based on — show a simple "this outlet changed since you edited it, keep yours / keep server's" prompt rather than silently overwriting pricing. |
| Driver confirms a delivery offline; admin cancels the underlying order on web before sync | Order cancelled from under a queued delivery | Sync rejects the delivery with a clear reason ("order was cancelled"); never silently drop — surface to driver/admin for a human decision, since cash may already be physically collected. |
| Invoice created offline, but the outlet/company details it depended on changed before sync | Invoice line data depends on tier price/discount at creation time | Lock the pricing snapshot into the queued invoice payload at creation time (don't recompute on sync) — the rep quoted that price to the shopkeeper, it should not silently change. Numbering is still deferred to sync per Decision 1b. |
| Cash Received logged offline, then the rep's device loses the entry before sync (app reinstall, device swap) | Local-only until synced = window of physical loss | Sync as early/often as possible (don't wait for app close); treat this as the key argument for aggressive background sync rather than a reason to make cash non-offline. |

**General principle for all of the above:** never auto-resolve a conflict that touches money
or stock quantities by silently picking a winner — surface it as a short, clear decision to
a human (rep, driver or admin) and keep both versions visible until resolved. Auto-resolve is
fine for cosmetic/non-financial fields only (notes, display order, etc.).

## Decision 3 — Sync mechanics, confirmed with DBEngineer's dev schema (10 Oct 2026)

DBEngineer's `001_schema_dev.sql` adds `client_id uuid unique, device_id text, sync_version
int, synced_at timestamptz` to the priority offline tables. Confirming the design:

- **`client_id` stays a unique side column, not the primary key.** The server-generated `id`
  (uuid PK) remains authoritative; `client_id` is purely the client's idempotency key for safe
  retry. This also keeps the same tables working for non-offline writes from the web admin
  portal, which never sets `client_id`. Correct as built — no change needed.
- **Inserts (new order, delivery, cash entry, stock pickup, settlement, outlet):** on retry,
  the write must be a no-op that returns the existing row, not an error — `insert ... on
  conflict (client_id) do nothing returning *`, or equivalent RPC, falling back to a `select`
  by `client_id` if nothing is returned. This is an integration-contract requirement for
  Backend/API Developer, not just a DB constraint.
- **Business-rule conflicts on insert** (e.g. `field_orders` unique `(outlet_id, order_date)`)
  are not retried — they're surfaced to the rep per the scenario table above ("an order for
  this outlet today already exists").
- **Edits (outlet details, an order before sync, etc.): `sync_version` is an optimistic-
  concurrency token, not a timestamp race.** The client includes the `sync_version` it last
  read with its edit; the server accepts the write only if the current `sync_version` still
  matches, then increments it. A mismatch is a real conflict — handle it exactly as the
  scenario table above says (explicit prompt for pricing/financial fields, silent last-write-
  wins only for notes/cosmetic fields). This is deliberately **not** blind last-write-wins by
  wall-clock timestamp, which would let a stale offline edit silently overwrite a newer server
  change.
- **No server-side "local draft" status is needed.** "Pending sync" is a purely client-side
  (Room outbox) concept — a record doesn't exist on the server until its first successful
  sync, so `field_order_status` and similar enums stay exactly as the BRD defines them
  (pending/delivered/cancelled), no new offline-only state added to the schema.
- **Priority-table list correction:** `cash_register_entries`, `settlements` and
  `daily_beat_log` are explicitly offline-capable actions per this ADR (Cash Received, Settle,
  and beat-visit status/completion all happen in the field) and per Swat's "every feature
  offline-capable" ruling — these need the same four offline-sync columns as the other
  priority tables. Flagged back to DBEngineer.

## Decision 4 — BizAnalyst's FS-01..09 backlog (AF-SA-FRS-001 v1.0) adopted as authoritative

BizAnalyst delivered the final functional-requirements backlog (`attachments/absolute-fresh-
sales-app-functional-requirements-v1.0 (2).md` as received 12 Oct 2026) with per-screen
acceptance criteria, global acceptance criteria (GAC), and finalised resolutions for all five
conflict scenarios drafted above. **This backlog, not the hand-drafted scenario table above,
is now the acceptance-criteria source of truth for FS-01..09.** It confirms/refines what this
ADR already decided:

- Four of five conflict resolutions line up with or sharpen this ADR's drafted scenarios
  (outlet concurrent-edit → version-based compare, approved-field auto-merge only, matching
  Decision 3's `sync_version` mechanic, not the "last-write-wins by timestamp" language in the
  scenario table above, which is now superseded by Decision 3 and should be read as such;
  delivery-vs-cancellation → backend time/version-ordered operational review; invoice pricing →
  immutable snapshot; cash → atomic local commit required before success shown, never shown
  successful on a failed local write).
- **RESOLVED 12 Oct 2026 — Swat's ruling via BizAnalyst (AF-SA-FRS-001 v1.1):** option (b).
  Multiple legitimate orders, including same-day top-ups, are permitted per outlet/day.
  `field_orders (outlet_id, order_date)` must **not** be a unique DB constraint — two distinct
  operation UUIDs are always two distinct orders. The backend may flag a *probable duplicate*
  (configurable outlet/time/content matching) for an authorized user to confirm-both or
  cancel-one via audited reversal, but must never hard-reject or auto-delete solely because
  outlet/date match. Retrying the same operation UUID remains idempotent (no-op, returns the
  existing row) regardless. Sent to DBEngineer 12 Oct 2026 to drop the unique constraint and
  add duplicate-flagging instead — this is now unblocked, FS-03 can proceed.

## Still open / dependencies
- [ ] **Hard gate:** isolated dev/staging Supabase project (schema + RLS parity with intended
      production design) — DBEngineer provisioning, requested by Orchestrator 10 Oct 2026.
      No mobile write-path dev/test work starts until this exists. I've sent DBEngineer the
      parity requirements this architecture needs (idempotency-friendly constraints, RLS
      matching Appendix G roles, seed data for 3 reps/outlets/products to develop against).
- [ ] Separately, Phase 0 (RLS policies, repo privacy) completion status on the **real**
      production project — still unconfirmed, tracked by Orchestrator with Swat.
- [ ] Backend/API Developer: needs idempotency-key support (unique constraint/upsert) on
      order, delivery, cash-register, outlet and invoice-draft inserts/RPCs for safe offline
      replay. I'll send this as a formal integration contract once BizAnalyst's user stories
      confirm the exact write endpoints in scope for v1.
- [x] BizAnalyst delivered the final FS-01..09 backlog with per-screen acceptance criteria and
      conflict resolutions 12 Oct 2026 — see Decision 4.
- [x] Duplicate-order handling resolved 12 Oct 2026 — Swat ruled option (b), see Decision 4.
      FS-03 unblocked. DBEngineer instructed to drop the unique constraint and add a
      probable-duplicate flag instead.
