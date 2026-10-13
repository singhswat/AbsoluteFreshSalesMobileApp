# Absolute Fresh Android App — Draft Information Architecture v0.1
UIUXDesigner · 10 Oct 2026 · Source: BRD.md (Appendix C, section 10 Roadmap P5) in this folder

## Status: DRAFT — pending BizAnalyst user stories and Swat's confirmation of scope

## 1. What this is based on
No user stories exist yet from BizAnalyst. The project folder already contains `BRD.md`, a
reverse-engineered Business Requirements Document for the existing Absolute Fresh web system
(owner: Swatantra Singh, 10 Oct 2026). Two things in it directly define this mobile app:

- Section 3 (Scope): "Native Android app (planned, separate project)" is listed as explicitly
  out of scope for the web rebuild — i.e. it is *this* project.
- Section 10 (Roadmap), Phase 5 "Growth": "native Android sales app for 10 to 15 internal
  users... Reps use the app daily." This is the last phase, built on top of a secured,
  corrected web/database base (Phases 0–4).
- Appendix C describes the existing field-sales web app (sales/salesrep/) screen by screen.
  Section C1 explicitly says: "Rep and driver pages are mobile-first" — these are the screens
  this app should replace with a native equivalent.

**Working assumption (needs Swat's sign-off): the Android app = a native rebuild of the
field-sales app for Sales Reps and Delivery Drivers (10–15 internal users), NOT the D2C
customer-facing ordering app.** Admin-only screens (Beat Plans, Ledger, Analytics, Settings,
Team, Stock Dashboard) stay on the web admin portal per the current architecture and are
out of scope for the phone app unless told otherwise.

## 2. Users / roles in this app
| Role | Primary job on the phone |
| --- | --- |
| Sales Rep | Run today's beat, take orders, manage outlets, log stock pickup/settlement, collect cash, raise invoices |
| Delivery Driver | Confirm deliveries against orders, collect payment |
(Admin may get read-only visibility later — not in v0.1)

## 3. Proposed navigation structure
Single app, role-based home screen after sign-in (role comes from the existing unified
login — admin/rep/driver/influencer).

### Sign In
- Phone/email + password. Bilingual (English/Hindi) labels per existing app convention.
- Routes to Rep Home or Driver Home by role.

### Rep — bottom navigation (5 tabs)
1. **Beat** (home) — today's visit list: sequence, outlet, area, target, product, kg, invoice,
   status (planned/active/completed). Date nav (prev/next/today), pull to refresh.
   Tapping an outlet opens **New Order** for that outlet.
2. **Orders** — "My Orders" list, filters: All / Today / This Week / Pending / Delivered.
   Tapping an order opens its detail/edit (replace items, update notes).
3. **Outlets** — "My Outlets" searchable list; + Add Outlet (business name*, area*, owner,
   phone, price tier, GSTIN optional, address).
4. **Stock** — date-based pickup log (store, product, packets, notes) + running totals;
   **Settle** flow per outlet (packets sold, packets returned, cash collected, payment method:
   Cash/UPI/Pending-Credit) → optionally "Settle & Generate Invoice" → share invoice via
   WhatsApp/share-sheet.
5. **More** — Cash (Currently Held / History + Record Cash Received), My Invoices (list +
   share), Create Invoice (ad-hoc, from beat), Sign Out, language toggle.

Cross-cutting screen: **New Order** (outlet, product, kg, delivery date, notes, +add line;
live price = tier price − outlet discount; live invoice total) — reached from Beat or Orders.

### Driver — bottom navigation (2 tabs)
1. **Deliveries** (home) — tabs *Pending* / *Done Today*. Tapping a pending delivery opens
   **Confirm Delivery**: delivered kg per line item (reason required if delivered < ordered),
   amount collected, payment mode (Cash/Cheque/Credit/UPI), Confirm.
2. **Done Today** — day log: time, outlet, area, product, ordered, delivered, fill %, invoice
   value, collected, mode, note. (Same data admin sees on web.)

## 4. Design principles for this app (from BRD §9 non-functional notes)
- Mobile-first, large tap targets (field use, sometimes one-handed, outdoors).
- Must work usably on 4G with weak signal; order/delivery submit should confirm fast —
  design for optimistic UI + clear pending/syncing/failed states, since reps work in
  low-connectivity retail backrooms.
- Offline resilience matters: the current web app only has an installable shell (service
  worker). For the native app this likely needs real offline queueing for Beat visit marks,
  stock pickups and delivery confirmations — **flagging to Architect**, this is a technical
  constraint I need their ruling on before finalising offline-state UI patterns.
- Bilingual English/Hindi labels throughout, consistent with the existing web app.
- No hard-delete patterns in the UI for orders/invoices/stock (BRD flags this as a bug in the
  current web app, F1/F4) — mobile screens should only expose create/correct/reverse actions,
  never destructive edits of historical records.

## 5a. Sync / offline / auth states (per Architect's ADR-001, 10 Oct 2026)
Architect ruled: offline-first queueing for all core field actions (not online-required),
Supabase Auth against the same Supabase project as the web apps. These are now first-class
UI states, not incidental loading spinners.

**Per-record sync status (Orders, Outlets-added-in-field, Stock Pickup, Settle, Cash
Received, Delivery confirmations, ad-hoc Invoices):**
- `Pending sync` — saved locally, not yet sent. Shown with a distinct badge/icon on list
  rows (Beat grid, My Orders, Cash History, Stock log, Deliveries Done Today) and on the
  record's own confirmation screen right after save.
- `Syncing` — background worker is sending it now.
- `Synced` — confirmed by server; normal/default row state, badge clears.
- `Failed` — server rejected it (e.g. duplicate order for that outlet/day). Surfaced as an
  explicit in-app conflict the rep must resolve (edit/cancel/retry) — never silently dropped
  or silently overwritten.
- App-level indicator: a small persistent banner/icon (not a blocking modal) showing
  "Working offline — N pending" whenever the outbox is non-empty, visible from any screen,
  so a rep always knows unsynced work exists before ending their day/shift.

**Invoice-specific rule (Decision 1b):** an invoice created offline must NEVER show a fake/
local invoice number. Its confirmation screen and any share action show "Invoice pending —
number assigned once synced" until the real server-issued number (AFF-YYMM-NNN) comes back.
Share/WhatsApp actions on a still-pending invoice should warn before sending ("This invoice
number isn't confirmed yet") — **open question for Swat (Architect flagged this too):
should Create Invoice simply require connectivity for v1 instead, so a rep can always hand
over a confirmed number on the spot? Noting as unresolved, not designing around it yet.**

**Auth / session states (sign-in and app-wide):**
| State | UI behaviour |
| --- | --- |
| Invalid credentials | Inline error on sign-in form, same wording as web |
| Account inactive | Blocking message mirroring web's inactive-account text; no retry loop |
| Staff request pending approval | Mirrors web "pending approval" message; no further action available |
| Session expired while online | Non-destructive re-login prompt (modal), preserves any unsynced local queue |
| Offline (no network) | **Never** a logout or error state — app opens straight into cached data + queued-work mode; the only indicator is the "Working offline — N pending" banner above |
| Online, token rejected by server | Only this condition forces re-auth — distinct from plain "no network" |

## 5. Open items before pixels
1. **Swat to confirm**: is this field-sales rebuild the right scope for "the mobile app",
   or is a customer-facing D2C app also wanted on Android? BRD scope table suggests field-
   sales rebuild only; influencer app and D2C are not mentioned for Android.
2. **BizAnalyst**: please turn this draft IA into formal user stories + acceptance criteria
   (or correct it) so design and build have a BA-owned source of truth, not just my reading
   of a reverse-engineered BRD.
3. ~~Architect ruling on offline/sync + auth~~ — **resolved**, see §5a (ADR-001, 10 Oct 2026).
4. I currently have no browser-automation tool to drive stitch.google.com for actual visual
   mockups — asked Swat to enable Browser clearance so a Playwright connector can be added.
   Until then this IA is text/structure only, no visuals.
