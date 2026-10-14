# Feature Task Ledger: FS-05 Suggest Beat Change

- **Feature ID:** FS-05 (Suggest Beat Change Flow)
- **Branch:** `feature/FS-05-suggest-beat-change`
- **Base Revision:** `41e728d` (`main`)
- **Owner:** flutter-developer / orchestrator / qa-engineer
- **Source Requirements:**
  - `BRD.md` §6.4 (FS-05: Rep can suggest beat changes; admin accepts or rejects)
  - `design/screen-map-v1.0-field-sales-app.md` §2.10 (Suggest Beat Change screen specification)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (offline queued proposal, client UUID idempotency, sync states)

## Objective
Implement the native Android Jetpack Compose Suggest Beat Change screen (`beat_change`) allowing sales reps to:
1. Select an assigned outlet from their beat/territory.
2. View current scheduled beat details (Day of week, visit frequency, sequence number).
3. Propose schedule adjustments:
   - Proposed Visit Day (Monday through Saturday)
   - Proposed Frequency (Daily, Alternate Days, Twice Weekly, Weekly)
   - Mandatory Reason / Justification field (e.g., "Owner available only in afternoon", "Market closed on Mondays").
4. Submit the proposal offline-first with client UUID idempotency and `SyncStatus.PENDING`.
5. View past submitted change requests with live status badges (`PENDING_APPROVAL`, `APPROVED`, `REJECTED`), submission timestamps, and admin review notes.
6. Integrate entry point in `MoreScreen` ("Operations & Shortcuts -> Suggest Beat Change") and route in `MainActivity.kt`.

## Acceptance Criteria
- [x] AC1: Screen provides tab switcher between "New Proposal (नया प्रस्ताव)" and "Past Requests (पिछले अनुरोध)".
- [x] AC2: Outlet selection dropdown/selector shows assigned outlets with their current beat schedule.
- [x] AC3: Form allows selecting proposed visit day and frequency with clear bilingual labels.
- [x] AC4: Validation ensures reason is non-empty (at least 5 characters) before allowing submission.
- [x] AC5: Submitting creates proposal with unique UUID, `PENDING_APPROVAL` status, and local pending sync badge (`SyncStatus.PENDING`).
- [x] AC6: Past Requests tab displays all submitted proposals with status chips (`PENDING`, `APPROVED`, `REJECTED`), dates, reasons, and admin notes.
- [x] AC7: Comprehensive unit tests in `BeatChangeViewModelTest` covering validation, submission, and list filtering.
- [x] AC8: Verified live on Android emulator (`Pixel_10_Pro_XL`).

## Verification Evidence
- **Unit Tests:** 61/61 tests passing across 11 test suites via Gradle (`BUILD SUCCESSFUL in 56s`, 0 failures, 0 errors).
- **Emulator Live Run:** Verified on `Pixel_10_Pro_XL` (`emulator-5554`):
  - Navigated from More tab -> "Suggest Beat Change".
  - Verified outlet auto-population ("Sharma Kirana"), current schedule inspection, proposed day/frequency selection.
  - Entered justification reason and tapped "Submit Proposal (प्रस्ताव भेजें)".
  - Verified instant feedback snackbar, automated navigation to "Past Requests (3)" tab, pending count bump, and card showing "Pending Approval" with "Offline Draft (Pending Sync)".

## Status
- **Status:** Complete (Verified & Ready to Merge)
