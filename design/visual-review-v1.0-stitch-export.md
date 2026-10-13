# Stitch Export — Visual Review & Implementation Spec v1.0
UIUXDesigner · 13 Oct 2026
Source export (already extracted by Swat): `C:\Workspace\Absolute Fresh\Mobile App\stitch_sales_absolute_fresh_food\`
Scope of this doc: cross-check the export against `screen-map-v1.0-field-sales-app.md`, pick authoritative visuals per screen, extract build-ready design tokens, and list what's still missing or inconsistent. This does **not** supersede the screen-map's states/nav logic — it adds real visuals + tokens on top of it.

## 1. What's in the export
17 top-level folders. Each screen folder = `code.html` (full Tailwind-compiled HTML, inline config) + `screen.png` (rendered mobile/desktop PNG). Plus `absolute_fresh/DESIGN.md` (design system source) and two `extracted_text_from_...md` files (web copy, not used here).

| Folder | Viewport | Maps to |
|---|---|---|
| `field_rep_today_s_beat` | mobile | **FS-01/02 candidate A** |
| `today_s_beat_outlets` | mobile | FS-01/02 candidate B |
| `rep_beat_visit_execution` | mobile | FS-01/02 candidate C |
| `rep_new_order_booking` | mobile | **FS-03 candidate A** |
| `book_field_order` | mobile | FS-03 candidate B |
| `book_new_outlet_order` | mobile | FS-03 candidate C |
| `cash_collections_register` | mobile | **FS-03 Cash candidate A** |
| `cash_stock_settlement` | mobile | FS-03 Cash/Stock hybrid |
| `van_stock_settlements` | mobile | **FS-03 Stock candidate** |
| `b2b_gst_invoicing_receivables` | **desktop** | FS-07 (admin web — out of scope) |
| `admin_sales_operations_dashboard` | **desktop** | FS-07 (admin web — out of scope) |
| `absolute_fresh_logo.png`, `front_500_gms_...png`, `podi_chutney.png` | assets | brand/product imagery |

**Not present in this export at all:** Sign In, Add Outlet, Suggest Beat Change, Holiday Calendar, Order Edit History, Deliveries/Confirm Delivery/Done Today (driver role), More/Settings. These still need separate Stitch generation or hand-built screens — see §5.

## 2. Screen-to-FS mapping — authoritative picks
Stitch produced 2–3 independent variations for Beat and New Order. I picked one authoritative source per screen using closest fit to the GAC/FRS acceptance criteria and most complete feature coverage; the others are kept as component/style reference, not to be built twice.

### 2.1 Beat / Rep Home — FS-01 + FS-02 (combined)
**Authoritative: `field_rep_today_s_beat/code.html` + `screen.png`**
Reasons: only variant with (a) route execution progress widget (Planned/Done/Pending/Missed counts), (b) quota progress bar, (c) in-line per-outlet action set on the active row (Book Order / Stock & Expiry / Collect Cash / Call Owner) — this is exactly FS-02's "branch to order/invoice/cash" requirement rendered on the *same* screen as FS-01's list, so Stitch has effectively merged my spec's 2.2 (Beat) and 2.3 (Today's Visit) into one screen. **Recommend adopting that merge** — fewer taps, same states — rather than forcing them apart to match the original screen-map's two-screen split.
Also includes: GPS-geoperimeter match confirmation on arrival, "10-Day Shelf Window" batch-freshness callout, Podi cross-sell incentive banner, **+ Prospect Store** CTA (this *is* the Add-Outlet entry point FS-04 needs — closes screen-map open item §2.5a nav placement).
Reference only (same intent, different layout/data density): `today_s_beat_outlets/`, `rep_beat_visit_execution/` — useful for alternate card styles (e.g. `rep_beat_visit_execution`'s "ACTIVE VISIT IN-PROGRESS" callout card is a good pattern for the in-progress state specifically).

### 2.2 New Order + Create Invoice — FS-03
**Authoritative: `rep_new_order_booking/code.html` + `screen.png`**
Reasons: only variant that carries the full chain end-to-end on one screen — credit limit/available/terms header, dispatch cutoff banner, catalog with quick-add chips + stepper, GST-inclusive order calc, **payment settlement method selector (Cash / UPI QR / Credit-15D)**, and both "Confirm & Place Order" and "Settle & Print B2B Invoice" actions. This matches FS-03's combined New-Order-then-Invoice flow better than treating them as two hops.
Reference only: `book_field_order/` (good stepper + threshold-discount banner pattern — ">25kg bulk tier unlocked, saved ₹X"), `book_new_outlet_order/` (good draft-invoice-preview pattern, cutoff countdown "T-42m", margin-% per line for rep incentive visibility).
**Gap, flagged not fixed:** none of the three show a *provisional/pending invoice number* state (ADR-001's "Invoice pending — number assigned once synced" rule). All three show a real-looking invoice number immediately. MobileDev must add that state — it doesn't exist as a visual to copy yet.

### 2.3 Cash — FS-03 (cash collection/settlement)
**Authoritative: `cash_collections_register/code.html` + `screen.png`**
Reasons: cleanest 1:1 match to screen-map 2.9 (Currently Held summary + History list + Record action), with Held/Cleared tab split and per-entry sync-relevant metadata (invoice ref, time, method). "Clear by 8:00 PM" cutoff banner is a nice addition, not in the original spec — keep it.
Reference only: `cash_stock_settlement/` — good "Record Cash Receipt" form layout (retailer select, payment-mode toggle, amount with quick-add chips, invoice-ref note field) — reuse that *form* inside the authoritative screen's "Record Cash Received" action, rather than building the whole hybrid screen.

### 2.4 Stock — FS-03 (pickup/return/settlement)
**Authoritative: `van_stock_settlements/code.html` + `screen.png`**
Reasons: only dedicated Stock screen — van inventory load/delivered/balance, live chilled-SKU breakdown, FIFO shelf-life urgent notice (direct hit on BRD's 10-day decay rule), per-outlet settle flow (packets dropped/damaged/expired-pickup → net payable → Settle & Generate GST Invoice → share e-receipt via WhatsApp). This is a strong, complete reference — build against it directly.

### 2.5 Out of scope — do not build
`b2b_gst_invoicing_receivables/` and `admin_sales_operations_dashboard/` are desktop admin-console screens (left sidebar nav: Dashboard, D2C Orders, Field Sales & Beats, B2B GST Invoicing, Stock & Batch Trail, Milkman Programme, Team & Settings — Super Admin role). This confirms, independently of the BRD text, that FS-07 is a separate web product, not part of this Android app. No action needed from MobileDev on these two folders.

## 3. Design tokens (for MobileDev's Compose theme)
Primary source: `absolute_fresh/DESIGN.md`. **Flag: the file's own YAML frontmatter and its prose disagree on which hex is "Primary."** Frontmatter: `primary: '#004c22'`, `primary-container: '#166534'`. Prose (§Colors, §Palette Hierarchy): "Primary (`#166534`)... deep herbal botanical green." The rendered screens (`field_rep_today_s_beat`, `rep_new_order_booking`, etc.) all use **`#166534`** for primary CTA buttons, active tab icons, and progress bars — matching the prose, not the frontmatter's `primary`.
**Recommendation:** build the Compose Material3 theme's `primary` role = `#166534` (not `#004c22`), matching what's actually rendered. Keep `#004c22` only if Architect/BizAnalyst confirm it's an intentional darker variant for some other role (none observed in any screen). This needs a one-line confirm from Architect, not a blocker.

Also flag: individual screens' inline Tailwind configs are **not identical to each other or to DESIGN.md's `rounded` scale** — e.g. `rep_beat_visit_execution/code.html` defines `borderRadius.xl = 0.5rem`, while DESIGN.md's prose (§Shapes) says overlays use `rounded-xl = 0.75rem/12px`. Treat **DESIGN.md's prose §Shapes/§Colors/§Typography as the single source of truth**; do not copy a Tailwind config verbatim out of any one screen's `code.html` — the Compose theme should be hand-built from DESIGN.md, spot-checked against the PNGs.

Confirmed-safe tokens to lift directly (prose matches rendered screens on all of these):
- **Colors:** Primary `#166534` (CTAs, active states), Secondary/saffron `#D97706` (secondary actions, 7–8 day aging warnings), Tertiary/crimson `#DC2626` (hard cutoffs, safety limits), `admin-crimson #CC0000` (admin overrides — web only, N/A here), `amber-warning #F59E0B` (day 7–8 shelf badges), canvas `#F8FAFC`, card `#FFFFFF`, border `#E2E8F0`.
- **Freshness chip colors:** Fresh (Day 1–6) bg `#DCFCE7` / text `#166534` · Aging (Day 7–8) bg `#FEF3C7` / text `#B45309` · Critical (Day 9+) bg `#FEE2E2` / text `#B91C1C`.
- **Typography:** Inter, single family. Scale exactly as listed in DESIGN.md §frontmatter (`headline-xl` 36/44 down to `label-sm` 10/14). **Tabular numerics required** (`font-variant-numeric: tabular-nums`) for all batch codes, counters, prices, weights — confirmed in multiple screens (₹ totals, kg figures).
- **Radius:** 4px standard (buttons/inputs/badges), 8px containers (cards/panels/modals), 12px overlays (bottom sheets/drawers).
- **Elevation:** no heavy shadows — 1px borders for structure, one soft ambient shadow `0 4px 12px rgba(15,23,42,0.08)` for floating bars/sheets, 40% dark scrim for modals.
- **Touch targets:** minimum 48px height on all mobile interactive elements (confirmed in every exported mobile screen's buttons/rows).

## 4. Reusable components catalog (concrete, from real markup — supersedes the placeholder list in screen-map §4)
- **Outlet visit row card** (`field_rep_today_s_beat`): sequence badge, name + tier chip, address line, status chip (Visited/In-Progress/Planned — color-coded + icon, not colour-only), result banner (order/invoice summary + timestamp), action row (contextual buttons only on active row).
- **Metric bento widget** (`rep_beat_visit_execution`, `field_rep_today_s_beat`): 2-up grid, label-sm uppercase header, big number, progress bar, two-line footer stat split — used for Beat Progress and Cash-in-Bag; reuse for any "progress + secondary stat" pairing.
- **Catalog line item w/ stepper** (`rep_new_order_booking`, `book_field_order`): product thumb, name + freshness/SKU chip, MRP strikethrough + net price, quick-add chip row (+5kg/+10kg/+20kg style), stepper with live line total.
- **Order calc summary block**: subtotal/GST/net-payable stack, right-aligned bold total, "B2B Auto-Reconciled" or similar status chip.
- **Payment-method selector**: 2–3 segmented buttons (Cash/UPI/Credit), selected state = solid primary fill.
- **Cutoff/urgency banner**: amber or crimson soft background, icon + bold deadline text + countdown — used for dispatch cutoffs and shelf-life alerts; same component, different color token by severity.
- **Shelf-life/freshness chip**: 3-state color system per §3 above — this is the one component that should also carry the sync-status badge pattern (see gap below).

## 5. Gaps — real, not guessed
1. **No Sign-In screen anywhere in this export.** My `sign-in-screen-package-v1.0.md` (text spec, 9 states) is still the only Sign-In artifact. Needs a dedicated Stitch generation once browser/Playwright access exists, or MobileDev builds from the text spec + these tokens now.
2. **No GAC-4 sync-status states rendered anywhere** (Pending/Syncing/Synced/Failed/Action-required/Rejected). Every screen in this export assumes a connected, already-synced state. MobileDev will need to design the badge/banner overlay using tokens from §3 (recommend: Pending = neutral/`amber-warning`, Syncing = `primary` with spinner, Failed = `#DC2626`, Action-required = `#B91C1C` on `#FEE2E2`) — not a Stitch-sourced visual, this is new work either by me or MobileDev directly from tokens.
3. **Driver role (FS-06: Deliveries/Confirm Delivery/Done Today) has zero visuals** in this export — nothing to review yet.
4. **FS-04/05/08/09 screens (Add Outlet form, Suggest Beat Change, Holiday Calendar, Order Edit History) have zero visuals** — Add Outlet's *entry point* is covered (the "+ Prospect Store" CTA in §2.1), but not the form itself.
5. **Bottom nav is inconsistent across all 3 Beat variants and doesn't match screen-map v1.0's 5-tab spec** (Beat/Orders/Outlets/Stock/More). Observed in exports: `Beat·Orders·Deliveries&Cash·Profile&Stock` / `Beat·Orders·Cash·Stock` / `Today Beat·New Order·Collections·Stock&Dues` — all 4 tabs, none matching mine exactly, and "Outlets" never appears as its own tab (outlet search/add is reached from Beat instead). **This needs an explicit decision, not a silent pick by me** — recommend 4 tabs (Beat · Orders · Cash & Stock combined into "More" or two tabs, TBD) since three independent AI generations converged away from a 5th tab. Routing this to Architect/BizAnalyst for sign-off before MobileDev locks nav scaffolding.

## 6. What MobileDev should change/do
- Build Compose Material3 theme from §3 tokens (primary = `#166534`, confirm with Architect per the frontmatter/prose conflict above).
- Scaffold Beat (FS-01/02 merged), New Order+Invoice (FS-03), Cash (FS-03), Stock (FS-03) screens pixel-matching the four authoritative `screen.png`/`code.html` pairs named in §2 — treat `code.html` as the layout/spacing/class source of truth, PNG as the visual check.
- Do **not** build `today_s_beat_outlets`, `rep_beat_visit_execution`, `book_field_order`, `book_new_outlet_order`, or `cash_stock_settlement` as separate screens — they're reference only (see §2 for which specific pattern to borrow from each).
- Add the sync-status overlay system from scratch (§5.2) — nothing to copy.
- Hold off on Sign-In pixel work until either Playwright access lands or I hand-build it from tokens; the text spec (`sign-in-screen-package-v1.0.md`) is enough to start the ViewModel/logic layer now regardless.

## 7. Open decisions routed elsewhere (not mine to make)
- Bottom-nav tab count/labels (§5.5) → Architect + BizAnalyst.
- `primary` token conflict, `#004c22` vs `#166534` (§3) → Architect, one-line confirm.
- Everything else already open in `screen-map-v1.0-field-sales-app.md` §5 is unchanged by this review.
