# Feature Task Ledger: FS-03 New Order Booking Flow

- **Feature ID:** FS-03 (New Order Booking & Pricing Calculation)
- **Branch:** `feature/FS-03-new-order-booking`
- **Base Revision:** `3c13ce8` (`main`)
- **Owner:** flutter-developer / orchestrator / qa-engineer
- **Source Requirements:**
  - `BRD.md` §6.4 (FS-03: Rep places order in kg per product with tier price & discount rules)
  - `design/screen-map-v1.0-field-sales-app.md` §2.6 (New Order screen specification)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (idempotency client UUID, immutable calculation snapshot, optimistic concurrency)

## Objective
Implement the native Android Jetpack Compose New Order Booking screen (`new_order/{outletId}`) allowing sales reps to:
1. Select/review target outlet with its assigned price tier and custom discount.
2. Select quantities for all active SKUs (500g Batter, 1kg Batter, Podi Chutney) with responsive steppers.
3. Automatically compute live pricing based on outlet tier, custom discounts, and GST (5% for fresh batter, 18% for dry goods/podi per BRD).
4. Enforce business validations: minimum quantity validation, non-empty order, delivery date scheduling.
5. Freeze calculation snapshot on order creation with `client_id` (UUID) for offline idempotency.
6. Connect navigation from Beat -> Visit -> New Order, Outlets -> New Order, and Orders -> New Order.

## Acceptance Criteria
- [x] AC1: Screen displays outlet name, tier badge, and available credit/discount.
- [x] AC2: SKU list provides increment/decrement steppers with minimum step 1.
- [x] AC3: Live subtotal, GST tax calculation, and total update synchronously as items change.
- [x] AC4: Validation prevents submitting an empty order (total qty = 0).
- [x] AC5: Delivery date picker allows choosing delivery date (default tomorrow).
- [x] AC6: Submitting generates an Order with unique UUID `clientId`, frozen item snapshots, and enters pending sync status.
- [x] AC7: Full unit test coverage for `NewOrderViewModel` (6 tests in `NewOrderViewModelTest`).
- [x] AC8: Verified live on Android emulator (`Pixel_10_Pro_XL`).

## Verification Evidence
- Unit Tests: Gradle test task completed with 51/51 passing tests (0 failures, 0 skipped).
- Unit Tests File: `app/src/test/java/com/absolutefresh/sales/NewOrderViewModelTest.kt`
- Emulator Live Run: Pixel 10 Pro XL (`emulator-5554`)
  - Navigated Beat -> New Delhi Canteen -> Book Field Order.
  - Selected 2x 500g batter (1.0 kg), verified live subtotal ₹81.00, GST ₹4.05, total ₹85.05.
  - Selected Payment Method (UPI QR / Cash).
  - Submitted order -> verified return to My Orders with new provisional invoice `PROV-2610-674` in pending sync state.

## Status
- **Status:** Completed & Verified

