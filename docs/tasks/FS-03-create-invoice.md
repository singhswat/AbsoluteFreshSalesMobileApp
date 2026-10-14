# Feature Task Ledger: FS-03 Create Invoice Flow

- **Feature ID:** FS-03 / INV-01..INV-06 (Create Invoice & GST Receipt Flow)
- **Branch:** `feature/FS-03-create-invoice`
- **Base Revision:** `e38a44d` (`main`)
- **Owner:** flutter-developer / orchestrator / qa-engineer
- **Source Requirements:**
  - `BRD.md` §6.6 (INV-01: GST invoice, INV-02: GST responsibility, INV-03: Sequential/provisional numbers with seller GSTIN & FSSAI, INV-04: Invoices from beat/orders, INV-06: Frozen snapshot)
  - `design/screen-map-v1.0-field-sales-app.md` §2.7 (Create Invoice screen specification)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (offline provisional reference `PROV-YYMM-NNN`, immutable pricing snapshot)

## Objective
Implement the native Android Jetpack Compose Invoice screen (`create_invoice/{orderId}`) allowing sales reps and drivers to:
1. View a complete GST-compliant invoice rendered from the selected Order.
2. Display company seller details (Absolute Fresh Private Limited, FSSAI: 12723999000123, GSTIN: 09AAHCA1234F1Z5).
3. Display outlet buyer details (Trade name, address, phone, GSTIN if registered, price tier).
4. Clearly designate sync status with provisional banner (`PROV-YYMM-NNN (Provisional)` vs `AFF-YYMM-NNN (Official)`).
5. Calculate and present exact GST tax breakdown: 2.5% CGST + 2.5% SGST for fresh batter (or 9% CGST + 9% SGST for podi/accessories).
6. Enable 1-tap WhatsApp and system sharing with a formatted invoice receipt text including provisional notice.
7. Integrate navigation from `NewOrderScreen` ("Place Order & View Invoice") and `OrdersScreen` (Order Card -> "View Invoice").

## Acceptance Criteria
- [x] AC1: Screen displays seller brand header with FSSAI license and GSTIN.
- [x] AC2: Screen displays outlet details (name, address, phone, tier).
- [x] AC3: If order is pending sync, provisional banner clearly states "Provisional Invoice (अस्थायी चालान) — Pending Server Issuance".
- [x] AC4: Itemized table displays SKU name, quantity, unit rate, subtotal, and tax rate.
- [x] AC5: Tax breakdown card displays exact CGST (2.5%) and SGST (2.5%) breakdown.
- [x] AC6: Share button triggers Android Intent (WhatsApp/Share Sheet) with formatted bill receipt text.
- [x] AC7: Full unit test coverage for `InvoiceViewModel`.
- [x] AC8: Verified live on Android emulator (`Pixel_10_Pro_XL`).

## Verification Evidence
- Unit Tests: 55/55 passed across 10 test suites (`InvoiceViewModelTest`: 4/4 passed).
- Emulator Live Run: Verified on `Pixel_10_Pro_XL` (Provisional invoice banner, official invoice banner, GST 5% breakdown card, and Android OS Share Sheet sheet invoked).
- Artifacts: `emulator_screen_invoice.png`, `emulator_screen_gupta_invoice.png`, `emulator_screen_share_dialog.png`.

## Status
- **Status:** Complete (Ready for merge to main)
