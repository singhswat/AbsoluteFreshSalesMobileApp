# Feature Task Ledger: FS-04 Dedicated Add Outlet Screen Flow

- **Feature ID:** FS-04 (Add Outlet)
- **Branch:** `feature/FS-04-add-outlet`
- **Base Revision:** `2c2b3d1` (`main`)
- **Owner:** flutter-developer / orchestrator / qa-engineer
- **Source Requirements:**
  - `BRD.md` §3 / §6.1 / Appendix J #4 (FS-04: Rep adds outlet in field / admin reviews prospect)
  - `design/screen-map-v1.0-field-sales-app.md` §2.5 (My Outlets) & §2.5a (Add Outlet form)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (offline create, client UUID idempotency, duplicate warning)

## Objective
Implement the dedicated native Android Jetpack Compose Add Outlet screen (`add_outlet`) enabling field sales reps to:
1. Capture new retail outlets/prospects directly in the field with full offline support.
2. Input mandatory outlet details with real-time inline validation (Business Name min 2 chars, Area, 10-digit Phone).
3. Select Channel/Type (`GENERAL_TRADE`, `STANDALONE_SHOP`, `CHAIN_STORE`, `DARK_STORE`).
4. Select Price Tier (Tier 1 Wholesale, Tier 2 Distributor, Tier 3 Retailer, Tier 4 MRP/Standard default).
5. Input optional GSTIN with standard 15-character format validation (`^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$`).
6. Enter expected daily sales volume (kg/day), credit limit, and payment terms (7, 15, 30 days, or COD).
7. Capture GPS location pin / coordinates (with manual or auto mock coordinates) to verify field presence.
8. Perform duplicate detection against existing cached outlets (matching phone or matching business name in the same area), presenting an audited confirmation banner ("Potential duplicate found, confirm anyway") per ADR-001 / FS-04 AC5.
9. Persist durable offline record with client UUID (`out-UUID`), assign `SyncStatus.PENDING`, and queue in repository.
10. Wire navigation from `OutletsScreen.kt` "+ Add Outlet" button -> `Screen.AddOutlet.route`, and on completion navigate back to `OutletsScreen` with instant list update and "Pending Sync" badge.
11. Register route `Screen.AddOutlet.route` in `MainActivity.kt`.

## Acceptance Criteria
- [x] AC1: Screen displays clean Material3 form with bilingual English/Hindi field labels and top app bar with back navigation.
- [x] AC2: Real-time validation for required fields: Business Name (non-blank), Area (non-blank), Phone (>= 10 digits).
- [x] AC3: GSTIN format validation warns if formatted incorrectly while remaining optional.
- [x] AC4: Price tier selector (Tier 1 to 4) defaults to Tier 4 per BRD §6.1.
- [x] AC5: Duplicate check detects matching phone or outlet name and displays actionable warning without hard-blocking.
- [x] AC6: GPS coordinate capture records latitude/longitude tag.
- [x] AC7: Submits outlet with durable client UUID, `SyncStatus.PENDING`, and updates outlets flow.
- [x] AC8: Full unit test coverage in `AddOutletViewModelTest.kt` verifying all field validations, GSTIN checker, duplicate warning dismissals, and repository insertion.
- [x] AC9: Live emulator verification on `Pixel_10_Pro_XL` verifying form input, validation triggers, submission, and appearance in `OutletsScreen`.

## Verification Evidence
- **Unit Tests:** 91/91 passed across 15 test suites in `BUILD SUCCESSFUL in 1m 13s`.
  - Added `AddOutletViewModelTest.kt` with 11 comprehensive tests covering:
    - `initial state has empty fields and is invalid with default tier 4`
    - `name validation requires non blank and at least 2 characters`
    - `phone validation requires at least 10 digits and strips non-digits`
    - `area validation enforces required non-blank`
    - `optional GSTIN validation allows blank but rejects invalid format`
    - `valid GSTIN format passes validation and converts to uppercase`
    - `price tier and channel type update correctly`
    - `GPS coordinates capture and clear correctly`
    - `duplicate detection triggers warning when existing outlet name or phone matches`
    - `confirm add anyway bypasses duplicate warning and saves outlet`
    - `submit valid new outlet persists to repository with client UUID and PENDING status`
- **Emulator Live Run:** Pixel 10 Pro XL (`emulator-5554`)
  - Logged in as sales rep (`rep` / `pass123`).
  - Navigated to "My Outlets" tab; tapped "+ Add Outlet".
  - Verified `AddOutletScreen` loaded with Material3 form, bilingual headers, and "Offline Ready" badge.
  - Tested duplicate check with "Sharma Kirana" + "9876543201": amber warning banner appeared with matching details and "Edit Form" / "Add Anyway" actions.
  - Updated store to "Bake Hub" with phone "9811223344" and area "Indira Nagar".
  - Tapped "Tag Shop Location": GPS coordinates `📍 26.8467° N, 80.9462° E` tagged with green `✔ Tagged` chip.
  - Selected channel chip "Dairy / Sweet (डेयरी)".
  - Tapped "Save Outlet / आउटलेट सुरक्षित करें": automatically navigated back to `My Outlets`.
  - Verified new outlet "Bake Hub" appears at the top of the list with phone `9811223344`, `STANDALONE SHOP`, and amber `Pending` badge, while header pending counter increased to `3 pending`.
