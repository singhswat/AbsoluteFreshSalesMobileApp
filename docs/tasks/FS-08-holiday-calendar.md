# Feature Task Ledger: FS-08 Holiday Calendar & Beat Planning

- **Feature ID:** FS-08 (Holiday Calendar)
- **Branch:** `feature/FS-08-holiday-calendar`
- **Base Revision:** `f67cc03` (`main`)
- **Owner:** flutter-developer / orchestrator / qa-engineer
- **Source Requirements:**
  - `BRD.md` §3 / §6.1 / Appendix J #12 (FS-08: Holiday calendar to skip beat days & plan visits)
  - `design/screen-map-v1.0-field-sales-app.md` §2.12 (Holiday Calendar screen specification)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (offline caching, offline stale-data badge, never delete local activities on holiday changes)

## Objective
Implement the dedicated native Android Jetpack Compose Holiday Calendar screen (`holidays`) enabling sales reps and drivers to:
1. View a chronological schedule of upcoming and past holidays (festivals, national holidays, dry days/market closures).
2. Display bilingual titles (English & Hindi) and festival descriptions.
3. Show holiday operational scope: `Pan-India (राष्ट्रीय)`, `Regional UP/Lucknow (क्षेत्रीय)`, `Market Closure / Dry Day (बाजार बंद)`.
4. Highlight beat impact advisory badges for affected route days (e.g., "Monday Beat (Aliganj) skipped / rescheduled", "Diwali: All delivery vans grounded").
5. Display offline resilience indicator: Last cache refresh time and offline banner.
6. Provide month filter chips (All, Oct 2026, Nov 2026, Dec 2026, Jan 2027) and search query filtering.
7. Allow sales reps to submit a "Special Pre-Holiday Stocking / Reschedule Note" for advance outlet supply.
8. Wire navigation from `MoreScreen.kt` ("Holiday Calendar" row) and top app bar back navigation in `MainActivity.kt`.

## Acceptance Criteria
- [x] AC1: Screen displays header with bilingual title, subtitle, and offline cache status pill.
- [x] AC2: Shows month filter bar and text search for festival/holiday name and scope.
- [x] AC3: Lists holiday cards with date badge (Day, Month, Day of week), EN/HI name, scope tag, and description.
- [x] AC4: Shows beat route impact alert banner on affected holidays with affected beat names.
- [x] AC5: Actionable modal bottom sheet to submit a "Pre-Holiday Stock Advance" note with reason validation.
- [x] AC6: Unit tests covering `HolidayViewModel` (loading, month filter, scope filter, search, advance request submission) with 6/6 tests passing (73/73 total across codebase).
- [x] AC7: Live emulator verification on `Pixel_10_Pro_XL` (API 36) confirming navigation, bento cards, filtering, and advance stock bottom sheet submission.

## Verification Evidence
- **Unit Tests:** 73/73 passing tests across 13 test suites (`HolidayViewModelTest`, `OrderAuditViewModelTest`, `NewOrderViewModelTest`, `CreateInvoiceViewModelTest`, `BeatChangeViewModelTest`, `SignInViewModelTest`, `BeatViewModelTest`, `OrdersViewModelTest`, `OutletsViewModelTest`, `StockViewModelTest`, `OrderRepositoryTest`, `OutletRepositoryTest`, `AuthRepositoryTest`).
- **Live Emulator Run:**
  - Screen navigated via More Screen -> "Holiday Calendar (अवकाश कैलेंडर)".
  - Top Bar with Bilingual EN/HI and "Cached Offline" indicator.
  - Bento KPI Metrics: "9 Upcoming Holidays", "6 Affected Beat Days", "Durable Offline Status".
  - Month chips (All, Oct 2026, Nov 2026, Dec 2026, Jan 2027) & scope filter chips (Pan-India, Regional UP, Market Closure).
  - Holiday Cards rendering Dussehra / Vijayadashami with Beat Impact: "Tuesday Beat — Gomti Nagar" and early dispatch warning.
  - Advance Stocking Modal Bottom Sheet: filled Store Name ("Sharma Kirana"), Packets (20), Reason ("Pooja Demand"), and verified submission with snackbar confirmation: *"Pre-holiday advance of 20 pkts recorded for Sharma Kirana (अग्रिम स्टॉक दर्ज किया गया)"*.

## Status
- **Status:** Complete & Ready for Main Merge
