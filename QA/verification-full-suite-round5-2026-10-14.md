# QA Verification — Full Suite & All Core Screens, Round 5

**Date:** 2026-10-14
**Tester:** QAEngineer
**Scope:** Verification of all newly implemented and wired screens: Orders (FS-03), Outlets & Add Outlet (FS-04), More/Settings (cross-cutting), Van Stock & Settlements (FS-03), Today's Visit (FS-02), and Driver Deliveries & Done Today (FS-06).

## Scope Implemented & Wired
1. **OrdersScreen (FS-03)**:
   - Filter chips: All, Today, This Week, Pending, Delivered.
   - Real-time search by store or invoice number.
   - Sync status badges (Synced, Pending, Failed) per GAC 4.
   - Interactive order detail bottom sheet with complete line-item breakdown.
2. **OutletsScreen & Add Outlet (FS-04)**:
   - Filter chips by territory area.
   - Phone dialer and maps shortcuts.
   - Add Outlet form with local validations (phone >= 10 digits, mandatory fields).
   - Duplicate warning detection (FS-04 AC5) with "Confirm & Add Anyway" override.
3. **MoreScreen (Settings, Cash, Diagnostics)**:
   - Profile summary with language toggle (EN/HI).
   - GAC 11 Offline sync health dashboard with manual sync flush.
   - Cash-in-hand register with "+ Collect Cash" modal and recent receipt logs.
   - Holiday calendar bottom sheet for field sales.
   - Sign Out routing back to login.
4. **StockScreen (FS-03 Van Stock & Settlements)**:
   - Active batch card with 10-day natural decay freshness meter.
   - 3-card bento grid: Loaded, Delivered, Balance.
   - Live chilled SKU breakdown (500g, 1kg, Podi).
   - FIFO Shelf-life urgent warning notice (Day 8 decay).
   - Interactive store settlement ledger with packet dropper, expired returns credit calculation, payment instrument selection, and WhatsApp e-receipt share.
5. **TodaysVisitScreen (FS-02 Outlet Execution)**:
   - Check-in to outlet with GPS timestamp recording.
   - Visit state transition: PLANNED -> ACTIVE -> COMPLETED.
   - Structured No-Order dialog with reasons (sufficient stock, closed, etc.).
6. **DeliveriesScreen & DoneTodayScreen (FS-06 Driver Role)**:
   - Route progress bar and cash collection summary.
   - Pending delivery cards with Call and Directions shortcuts.
   - Confirm delivery bottom sheet with short-delivery detection and reason requirement.
   - Done Today log with volume dropped and cash collected.

## Unit Test Coverage Summary
- `SignInViewModelTest.kt`: 10/10 PASS
- `BeatViewModelTest.kt`: 6/6 PASS
- `OrdersViewModelTest.kt`: 6/6 PASS
- `OutletsViewModelTest.kt`: 5/5 PASS
- `MoreViewModelTest.kt`: 5/5 PASS
- `StockViewModelTest.kt`: 5/5 PASS
- `TodaysVisitViewModelTest.kt`: 4/4 PASS
- `DeliveriesViewModelTest.kt`: 4/4 PASS
Total = 45 unit tests.
