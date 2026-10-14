# Feature Task Ledger: FS-06 Confirm Delivery & Proof of Delivery (POD)

- **Feature ID:** FS-06 (Confirm Delivery)
- **Branch:** `feature/FS-06-confirm-delivery`
- **Base Revision:** `e2755a4` (`main`)
- **Owner:** flutter-developer / orchestrator / qa-engineer
- **Source Requirements:**
  - `BRD.md` §3 / §6.1 / Appendix J #6 (FS-06: Driver delivery confirmation & collection)
  - `design/screen-map-v1.0-field-sales-app.md` §2.14 (Confirm Delivery screen specification) & §2.15 (Done Today)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (offline create/edit, client UUID idempotency, optimistic concurrency)

## Objective
Implement the dedicated native Android Jetpack Compose Confirm Delivery screen (`confirm_delivery/{deliveryId}`) enabling delivery drivers to:
1. View detailed stop information (Stop sequence, Outlet name, Address, Contact, Phone call shortcut, Google Maps navigation shortcut).
2. Review line items and ordered quantities (Idli/Dosa batter packets, weights).
3. Record actual delivered quantity with interactive steppers and auto-calculated delivery fill percentage.
4. Enforce field policy validation: if delivered packets < ordered packets (shortfall), require a documented shortfall reason (e.g. "Chiller space full", "Damage in transit", "Retailer cancelled part order").
5. Real-time recalculation of gross amount, taxes, and net invoice amount based on actual delivered packets.
6. Record payment collection:
   - Payment Mode: `CASH`, `UPI_QR` (with dynamic QR preview), `CHEQUE` (with Cheque # & Bank name), `CREDIT_15D` (credit ledger).
   - Amount collected validation (can be full, partial, or zero for credit).
7. Capture Proof of Delivery (POD):
   - Touch/Gesture Signature canvas (clear & capture).
   - Photographic evidence placeholder / camera capture status.
   - 4-digit Customer Delivery Verification OTP input.
8. Driver notes / remarks field for delivery handover.
9. Offline resilience: generates client UUID idempotency key, records delivery transaction offline, updates driver's cash held, and moves stop to `Done Today`.
10. Wire navigation from `DeliveriesScreen.kt` card "Confirm Drop" button -> `Screen.ConfirmDelivery.createRoute(deliveryId)`, and on completion navigate to `Screen.DoneToday.route`.

## Acceptance Criteria
- [x] AC1: Screen displays stop header with outlet name, address, contact, and quick call/map buttons.
- [x] AC2: Shows ordered line items with delivered quantity controls and live fill percentage.
- [x] AC3: Detects shortfall (delivered < ordered) and enforces mandatory shortfall reason selection/entry.
- [x] AC4: Displays dynamic recalculation of invoice payable amount and collected payment entry.
- [x] AC5: Supports multiple payment modes (`CASH`, `UPI_QR`, `CHEQUE` with cheque details, `CREDIT_15D`).
- [x] AC6: Includes interactive Proof of Delivery (POD) section with Signature drawing pad, Photo proof, and Customer OTP options.
- [x] AC7: Submits delivery confirmation durable offline record and updates driver delivery state.
- [x] AC8: Full unit test coverage in `ConfirmDeliveryViewModelTest.kt` verifying all validation, shortfall logic, payment calculation, and submission states.
- [x] AC9: Live emulator verification on `Pixel_10_Pro_XL` logging in as driver (`driver` / `pass123`).

## Verification Evidence
- **Unit Tests:** 80/80 passed across 14 test suites in `BUILD SUCCESSFUL in 48s`.
  - Added `ConfirmDeliveryViewModelTest.kt` with 7 comprehensive tests covering:
    - `initial state loads delivery details and ordered line items`
    - `packet stepper adjust updates delivered packets and fill percentage`
    - `shortfall detection enforces shortfall reason requirement`
    - `validation fails when shortfall present but reason is blank`
    - `payment mode change updates calculation and credit handling`
    - `POD method selection and validation`
    - `confirm delivery submits offline sync event and updates status`
- **Emulator Live Run:** Pixel 10 Pro XL (`emulator-5554`)
  - Logged in as driver (`driver` / `pass123`).
  - Tapped "Confirm Drop" on Stop #3 (`Krishna Sweets`, `del-03`).
  - Tested live delivered packet decrement from 25 to 20 pkts:
    - Delivery fill meter updated to 80% (20/25) with amber indicator.
    - Short delivery warning appeared with required reason enforcement.
    - Invoice payable dynamically recalculated from ₹2,152.50 to ₹1,722.00.
    - Confirm button was disabled with "Shortfall reason required" helper.
  - Selected shortfall reason chip "Retailer Chiller Full (फ्रिज में जगह नहीं)":
    - Active chip highlighted, warning dismissed, Confirm button enabled.
  - Switched payment to "Cash (नकद)":
    - Amount collected updated to ₹1,722.00 with driver van handover badge.
  - Drew digital signature stroke on Canvas:
    - Rendered stroke path, showed "Clear" control, POD verified.
  - Tapped "Confirm & Complete Drop":
    - Successfully confirmed, transitioned to "Done Today" tab.
    - Krishna Sweets displayed as Delivered with 20/25 pkts, shortfall reason, and signature POD.
    - Total Stops Done updated from 2 to 3, Van Cash updated from ₹1,239 to ₹2,961 (+₹1,722).

## Status
- **Status:** Complete & Verified

