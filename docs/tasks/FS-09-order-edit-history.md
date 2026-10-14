# Feature Task Ledger: FS-09 Order Edit History & Audit Trail

- **Feature ID:** FS-09 (Order Edit History & Conflict Audit)
- **Branch:** `feature/FS-09-order-edit-history`
- **Base Revision:** `77da5c6` (`main`)
- **Owner:** flutter-developer / orchestrator / qa-engineer
- **Source Requirements:**
  - `BRD.md` §6.4 (FS-09: Order edit history, audit log of modifications, previous & new values, mandatory reason; reversal entries instead of deletes)
  - `BRD.md` §6.5 (STK-10: Audit log of who, what, when, previous and new value, reason; reversal entries instead of deletes)
  - `design/screen-map-v1.0-field-sales-app.md` §2.11 (Order Edit History screen specification)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (append-only audit trail, never collapse state during sync conflict, side-by-side local vs server diff)

## Objective
Implement the native Android Jetpack Compose Order Edit History screen (`order_history/{orderId}`) allowing sales reps and drivers to:
1. View a chronological audit timeline of all modifications for a selected order.
2. Track who made the change (Sales Rep, Admin, System, Driver), timestamp, and operation UUID.
3. Review event types: `ORDER_CREATED`, `ITEMS_AMENDED`, `PAYMENT_UPDATED`, `INVOICE_GENERATED`, `STATUS_CHANGED`, `CONFLICT_RESOLVED`.
4. Inspect side-by-side Previous vs New values (e.g. quantities changed from 10 to 12 pkts, total adjusted, payment terms updated).
5. Review mandatory reason / justification attached to each amendment (e.g. "Store requested 2 additional batter packets upon delivery").
6. Highlight offline conflict resolution records showing Local Proposed vs Server Authoritative data.
7. Add an "Amend Order / Add Revision Note" action modal requiring a change reason.
8. Link from `OrderDetailSheet` in `OrdersScreen.kt` and wire navigation in `MainActivity.kt`.

## Acceptance Criteria
- [x] AC1: Screen displays order summary banner (Outlet name, Order ID, Date, Current Status, Total).
- [x] AC2: Displays chronological vertical timeline of audit entries with visual timeline nodes and event-coded color tags.
- [x] AC3: Shows actor identity ("Sales Rep (Swat)", "Admin", "Sync Engine"), relative & formatted timestamp, and operation UUID.
- [x] AC4: Shows structured field diffs (Field name, Previous Value, New Value) and amendment reason.
- [x] AC5: Highlights sync conflict resolution events with local vs server comparison badge.
- [x] AC6: Reps can record an order note/amendment reason with minimum character validation.
- [x] AC7: Comprehensive unit tests in `OrderHistoryViewModelTest` covering loading, filtering, and amendment creation.
- [x] AC8: Live verification on Android emulator (`Pixel_10_Pro_XL`).

## Verification Evidence
- **Unit Tests:** 67/67 tests passing (`testDebugUnitTest` via Gradle 9.6.0, 12 test suites, 0 failures, 0 errors).
  - `OrderHistoryViewModelTest`: initial state load, state transitions, filter by event type, amendment note creation, validation rules.
- **Emulator Verification:** Verified on `Pixel_10_Pro_XL` (Android 15):
  - Navigated from Orders tab -> Sharma Kirana order detail bottom sheet -> "Audit & History (संशोधन इतिहास)".
  - Inspected audit trail cards with actors, timestamps, diffs, and justifications.
  - Successfully added revision note via "+ Add Note" modal bottom sheet with reason validation.
  - Verified instant reactive update in timeline and success snackbar.

## Status
- **Status:** Complete
