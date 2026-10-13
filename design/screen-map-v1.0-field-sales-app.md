# Absolute Fresh Android App — Screen Map & UX Spec v1.0
UIUXDesigner · 12 Oct 2026
Status: **DRAFT v1.0 — text/structure spec, no visual mockups yet** (blocked on Stitch zip extraction, see Dependencies). Supersedes `IA-draft-v0.1-field-sales-app.md` for scope/nav; that file's §5a (sync/auth states) and design principles still apply and are folded in below.

## 0. Scope (confirmed, not re-derived)
Source: BizAnalyst `absolute-fresh-sales-app-functional-requirements-v1.0.md` (AF-SA-FRS-001 v1.0), Architect ADR-001/002.

- Android v1 = offline-first field-sales app for ~10–15 **Sales Representatives** and **Delivery Drivers**. D2C excluded.
- Covers **FS-01, 02, 03, 04, 05, 06, 08, 09**. **FS-07 (admin) stays web-only** — no Android admin nav/screens in this spec. (Flagged separately to Orchestrator: t10u's "administration and business operations" wording needs confirming against this — proceeding on this boundary unless told otherwise.)
- **Appendix K (Milkman programme) deferred**, no screens here.
- Every screen below must satisfy GAC 1–16 (offline create/edit, pending/syncing/synced/failed/conflict states, idempotent retry, dependency sync order, audit trail) — not repeated per screen, treated as a cross-cutting spec in §4.

## 1. Roles & navigation
Single app, role-based home after sign-in (Supabase Auth, same project as web — per ADR-001).

**Sales Rep — bottom nav, 5 tabs:** Beat · Orders · Outlets · Stock · More
**Delivery Driver — bottom nav, 2 tabs:** Deliveries · Done Today

No admin tab/role in this app (FS-07 stays web).

## 2. Screen specifications

Each screen lists: **Maps to**, **Purpose**, **Key elements**, **Primary states**, **Nav**.

### 2.1 Sign In
- **Maps to:** cross-cutting (auth, GAC 12)
- **Purpose:** authenticate, route by role.
- **Key elements:** phone/email + password fields, bilingual EN/HI labels, Sign In button, forgot-password link (web parity — confirm exists).
- **States:** empty (first load) · validating · invalid credentials (inline error) · account inactive (blocking message, no retry) · pending approval (blocking message) · success → route to Rep Home or Driver Home by role.
- **Nav:** entry point. No back nav once authenticated except Sign Out (in More/Settings).

### 2.2 Beat (Rep home) — FS-01
- **Maps to:** FS-01 (beat planning/assignment)
- **Purpose:** show today's assigned visit sequence; cached offline.
- **Key elements:** date nav (prev/today/next), pull-to-refresh, visit list rows (sequence #, outlet name, area, target, product, kg, invoice status, visit state: planned/active/completed), "last refresh" timestamp (GAC 15).
- **States:** loading (skeleton list) · empty ("No beat assigned for this date") · populated · stale-data warning banner if cache beyond threshold (GAC 15, threshold TBD — open item) · revoked/reassigned-beat-with-local-activity flagged row (FS-01 AC5).
- **Nav:** tap outlet row → Today's Visit / Outlet actions (2.3). Tab bar persistent.

### 2.3 Today's Visit / Outlet actions — FS-02
- **Maps to:** FS-02
- **Purpose:** from a visit, let rep record outcome or branch to order/invoice/cash/add-outlet.
- **Key elements:** outlet details (address, sequence, state), action buttons: Open Outlet Details, New Order, Create Invoice (where permitted), Add Outlet, Record Cash, Complete/No-Order.
- **States:** default · completed (locked view, read-only) · conflict ("This visit was completed/reassigned on the server — your local record is preserved, choose an action") per FS-02 AC6.
- **Nav:** → New Order (2.6) · My Outlets/Outlet Detail (2.5) · Cash (2.9) · Add Outlet (2.5a).

### 2.4 My Orders — FS-03
- **Maps to:** FS-03
- **Purpose:** list and manage orders raised by this rep.
- **Key elements:** filter chips (All/Today/This Week/Pending/Delivered), order rows (outlet, date, items summary, total, sync-status badge — Pending/Syncing/Synced/Failed-retrying/Action required/Rejected per GAC 4).
- **States:** loading · empty ("No orders yet — start one from Beat") · populated · row-level sync badges (never colour-only, GAC 4) · tap-to-expand conflict detail for Action-required rows (links to FS-09 history, 2.11).
- **Nav:** tap row → Order Detail/Edit (locked fields once submitted, per FS-03 AC4/9 — amendment flow only, no destructive edit). Also reached by new-order creation from Beat/Outlet.

### 2.5 My Outlets / Outlet Detail — FS-02, FS-04
- **Maps to:** FS-04 (add outlet), FS-02 (outlet detail usage)
- **Purpose:** searchable outlet list; view/add outlets.
- **Key elements:** search bar, outlet rows (name, area, price tier), **+ Add Outlet**.
- **States:** loading · empty · populated · possible-duplicate warning on add (FS-04 AC5: "shown for authorized review, no silent merge/delete") · Pending badge on locally-created outlets not yet synced.
- **Nav:** → Add Outlet form (2.5a) · outlet row → detail (read-only core fields, tier/discount visible) → can launch New Order from here too.

### 2.5a Add Outlet form — FS-04
- **Maps to:** FS-04
- **Key elements:** business name*, area*, owner, phone, price tier, GSTIN (optional), address — all enterable offline with local format validation (FS-04 AC1).
- **States:** validating (inline field errors) · saved (Pending, UUID assigned, visible immediately in My Outlets per AC2) · possible-duplicate flag.
- **Nav:** Save → back to My Outlets with new row visible.

### 2.6 New Order — FS-03
- **Maps to:** FS-03
- **Purpose:** build an order with live pricing.
- **Key elements:** outlet (cached or newly-added), product/qty lines (+add line), live price = tier price − outlet discount, taxes, line-level calc retained, delivery date, notes, order total.
- **States:** draft (resumable offline) · validating · submitted (freezes calc snapshot per AC4) · Pending-sync confirmation.
- **Nav:** Save → My Orders (2.4), with option to continue to Create Invoice (2.7).

### 2.7 Create Invoice — FS-03
- **Maps to:** FS-03
- **Purpose:** generate invoice from an order or ad-hoc, offline-capable.
- **Key elements:** linked order (or ad-hoc entry), total, payment info, **provisional reference** clearly marked "Pending sync / provisional number — not the final invoice number" (AC5/6), share action (WhatsApp/share-sheet) with a warning if number is still provisional (per IA draft §5a open question — flagged to Swat, not resolved: should Create Invoice require connectivity for v1 instead?).
- **States:** draft · Pending sync (provisional ref shown) · Synced (real AFF-YYMM-NNN number replaces provisional, immutable cross-reference kept per AC8) · Action required (server rejection/recalculation — AC9, app never silently rewrites an issued amount).
- **Nav:** reached from New Order, Today's Visit, or More ▸ Create Invoice (ad-hoc). → My Invoices (in More).

### 2.8 Stock — FS-03
- **Maps to:** FS-03 (stock pickup/return/settlement, part of combined FS-03 scope)
- **Purpose:** date-based pickup log + running totals; per-outlet settle flow.
- **Key elements:** store/product/packets/notes entry, running totals, **Settle** flow (packets sold, packets returned, cash collected, payment method Cash/UPI/Pending-Credit) → optional "Settle & Generate Invoice" chaining into 2.7.
- **States:** loading · empty · populated with Pending/Synced badges per entry · settle-in-progress (multi-step, resumable offline).
- **Nav:** Settle → optionally jumps to Create Invoice (2.7).

### 2.9 Cash — FS-03
- **Maps to:** FS-03 (cash collection/settlement)
- **Purpose:** track cash currently held + history; record new cash receipt.
- **Key elements:** Currently Held summary, History list (outlet/order/invoice, amount, method, user, time — AC11), **Record Cash Received** action.
- **States:** loading · empty · populated with sync badges · duplicate-retry guard (AC11: retries cannot duplicate).
- **Nav:** inside More tab; also reachable from Today's Visit (2.3).

### 2.10 Suggest Beat Change — FS-05
- **Maps to:** FS-05
- **Purpose:** rep proposes a routing correction without altering authoritative assignment.
- **Key elements:** affected outlet/current beat, proposed change, reason (required).
- **States:** Pending sync → Awaiting approval → Approved/Rejected (status chip, AC3) · Action-required if the referenced beat/outlet changed server-side before this synced (AC5).
- **Nav:** entry point TBD — recommend from Beat screen (outlet row long-press or overflow menu) or My Outlets detail. **Open item: not covered in IA draft v0.1 — needs an entry point decision**, flagging to Orchestrator/Architect as a small gap, not a blocker.

### 2.11 Order Edit History — FS-09
- **Maps to:** FS-09
- **Purpose:** chronological audit of an order's changes (and linked invoice/delivery/cash/sync outcomes).
- **Key elements:** timeline list (actor, time, operation ID, changed fields/values, reason where required), conflict entries show local vs. server values side by side with resolution outcome (AC7).
- **States:** loading · empty (no history yet) · populated, append-only (AC4 — sync never collapses to latest state).
- **Nav:** Rep: full edit/reason capability from Order Detail (2.4) "History" link. Driver: read-only where necessary (e.g. from a delivery's linked order).

### 2.12 Holiday Calendar — FS-08
- **Maps to:** FS-08
- **Purpose:** show applicable holidays offline for planning.
- **Key elements:** date, name, scope; last-refresh indicator.
- **States:** loading · empty · populated · stale warning. Changed holiday never deletes local activity — affected work flagged (AC3).
- **Nav:** recommend inside More tab (Rep) — not in IA draft v0.1, adding here as a small new item.

### 2.13 Deliveries (Driver home) — FS-06
- **Maps to:** FS-06
- **Purpose:** driver's route, Pending/Done Today tabs.
- **Key elements:** route cache (outlet/order, quantities, collection expectations), tap pending delivery → Confirm Delivery.
- **States:** loading · empty ("No deliveries assigned") · populated with sync badges.
- **Nav:** → Confirm Delivery (2.14); tab switch → Done Today (2.15).

### 2.14 Confirm Delivery — FS-06
- **Maps to:** FS-06
- **Key elements:** delivered qty per line (reason required if delivered < ordered, per FS-06 AC2/BRD), amount collected, payment mode (Cash/Cheque/Credit/UPI), Confirm button. **Open item (BizAnalyst unresolved decision #6): proof of delivery (signature/photo/GPS/OTP) not yet specified** — this screen's layout has a placeholder slot for it, not built out until BizAnalyst/Swat confirm which method(s).
- **States:** draft (resumable offline) · submitted (Pending, queues associated cash/stock event per AC5) · conflict (concurrent server change — AC6, visible, never silent overwrite).
- **Nav:** Confirm → Done Today (2.15).

### 2.15 Done Today — FS-06
- **Maps to:** FS-06
- **Purpose:** driver's completed-today log (same data admin sees on web).
- **Key elements:** time, outlet, area, product, ordered, delivered, fill %, invoice value, collected, mode, note, sync badge.
- **States:** loading · empty · populated.
- **Nav:** tab from Deliveries home.

### 2.16 More (Rep) / Settings
- **Maps to:** cross-cutting
- **Key elements:** Cash shortcut, My Invoices (list + share), Create Invoice (ad-hoc), Suggest Beat Change entry (2.10, pending confirmation), Holiday Calendar entry (2.12), Sign Out, language toggle (EN/HI), **sync-status view** (GAC 11: last successful sync, pending/failed/conflict counts, Retry now button).
- **States:** standard list.
- **Nav:** from Rep bottom nav tab 5.

## 3. Cross-cutting states (apply to every screen above, GAC 1–16 — not repeated per-screen)
Carried over verbatim from `IA-draft-v0.1-field-sales-app.md` §5a, now aligned to BizAnalyst's GAC terms:
- Record states: **Pending · Syncing · Synced · Failed — retrying · Action required/conflict · Rejected** (GAC 4) — badge + icon, never colour-only.
- App-level "Working offline — N pending" persistent banner whenever outbox is non-empty (not a blocking modal).
- Sync-status view (2.16) is the single place to see sync health across all record types (GAC 11).
- Auth/session states table (invalid creds / inactive / pending-approval / session-expired / offline / token-rejected) — unchanged from IA draft v0.1 §5a, still valid against FRS v1.0.
- No destructive delete UI anywhere — only create/amend/reverse (FS-03 AC13, FS-09 AC4, consistent with BRD F1/F4 bug flag).

## 4. Reusable components (for MobileDev/AndroidUIEngineer)
- Sync-status badge/chip (6 states above)
- Offline outbox banner
- Line-item order/invoice row (price = tier − discount, live total)
- Conflict resolution panel (local vs. server value, side-by-side, pick/merge)
- Bilingual label wrapper (EN/HI)
- Empty-state pattern (icon + message + primary action where relevant)
- Date-range/filter chip bar (used in Beat, Orders)

## 5. Open items / assumptions flagged by this spec (not decided by me)
1. **Scope wording check** — t10u's "administration and business operations" vs. confirmed FS-07-stays-web boundary — raised to Orchestrator separately (t10s0), this spec assumes the confirmed boundary holds.
2. **Suggest Beat Change entry point** (§2.10) and **Holiday Calendar entry point** (§2.12) weren't in IA draft v0.1 — I've placed them (Beat overflow / More tab) as a design default; Architect/MobileDev should confirm or move them.
3. **Proof of delivery method** (§2.14) — BizAnalyst's unresolved decision #6 (signature/photo/GPS/OTP) blocks finishing Confirm Delivery's layout; placeholder only until that's answered.
4. **Create Invoice connectivity requirement** — still open per IA draft v0.1 (should it require connectivity for v1 instead of a provisional number)? Carried forward unresolved.
5. **Stale-data threshold** (GAC 15) — no number specified anywhere yet; needs Architect/BizAnalyst input before it's a real UI value instead of a placeholder.

## Dependencies
- Visual mockups: blocked on `stitch_sales_absolute_fresh_food.zip` being extracted — I have the zip file itself in my attachments but no unzip/Shell tool; asked MobileDev (t10s1) to extract into `design/stitch-export/`.
- This text spec is sufficient for MobileDev to scaffold navigation/screens now; visuals (spacing, colour, exact component styling) will follow once Stitch assets are available.
