# QA Test Plan v0.1 — Field-Sales/Admin Android App (per-feature, branch sign-off gate)

Author: QAEngineer · 10 Oct 2026
Source: BRD.md Appendix J (rebuild acceptance tests), Appendix K5 (milkman acceptance tests),
§6.4 (FS-01..FS-09), Appendix G (roles matrix); ADR-001-offline-strategy-and-auth.md (offline
queueing, idempotency, conflict policy, auth session rules).

Purpose: one sign-off gate per feature branch. A branch does not close until every row below
for that feature is **Pass**, run against real builds/tests (not reasoned about). QA will not
sign off on a feature it has not actually executed tests for.

---

## 0. Test levels used per feature

- **Unit** — Kotlin/JUnit, pricing/discount/commission math, offline-outbox state machine, no device needed.
- **Instrumented (Room/WorkManager)** — androidTest, local SQLite + WorkManager test driver, no network needed; simulates queue/retry/idempotency.
- **Integration (device/emulator ↔ backend)** — needs a live Supabase project. **BLOCKED on all real/dev data until the isolated dev/staging Supabase project exists** — see §4.
- **Manual/exploratory** — adb network toggling for offline scenarios, two-device conflict scenarios.

---

## 1. Scope note to flag back to the room

ADR-001 (Architect, L8-10) states this native app's scope is **reps and drivers only (10-15
users); admin screens stay on the existing web app.** But BRD §6.4 lists **FS-07 "Admin sees
deliveries, completed-today list, orders list with cancellation, team list with approvals"**
as a requirement in the same table QAEngineer was asked to test against. If FS-07 is web-only,
it is out of scope for this test plan and for MobileDev's branches — flagging to
@Orchestrator / @Architect to confirm before a branch is opened for it. Test rows for FS-07
below are written but marked **scope TBC**.

Appendix K (milkman programme) acceptance tests are included (§3) since BRD confirms it's
in scope overall, but whether it's a v1 native-app branch or a later branch set is still open
per Orchestrator's ask to BizAnalyst — marked **sequencing TBC**.

---

## 2. Per-feature test cases (FS-01..FS-09)

| Feature | Test case | Level | Pass criteria | Offline variant |
| --- | --- | --- | --- | --- |
| FS-01 Beat plan (admin builds, publish) | Publish a plan; weekly/fortnightly(wk1&3)/monthly(wk1) outlets generate correctly; holiday skipped | Integration | Matches Appendix J test #12 exactly | n/a (admin/web per scope note) |
| FS-02 Rep's daily beat (visit list, mark visited/missed) | Rep opens Beat for a date with a published plan → correct outlet list in sequence, filtered by preferred weekday rule (BRD F3.4); mark visited/missed with reason persists | Instrumented + Integration | List matches server-side rule; missed reason stored | **Must work fully offline**: open Beat, mark visited/missed with no network → queued, UI shows pending state, syncs on reconnect (ADR-001 outbox table) |
| FS-03 Order placement (kg, tier price, discount) | Place order at outlet; tier price (T1-T4) and threshold/outlet-specific discount applied correctly; one order per outlet per day enforced | Unit (pricing math) + Instrumented + Integration | Price/discount matches BRD E-series rules; duplicate same-day order rejected server-side, not silently created twice | **Must work fully offline**: create order offline → outbox entry with client UUID; reconnect → single order created (idempotency key), never duplicated even if sync retries after timeout (ADR-001 Decision 1a) |
| FS-04 Rep adds outlet in field / admin reviews prospect | Rep adds outlet with required fields (business name, area); appears as prospect for admin review; convert-to-active creates active outlet assigned to that rep | Instrumented + Integration | Required-field validation; prospect→active conversion matches BRD F2.5 | **Offline**: add outlet with client-generated temp id while offline → reconciles to real server id on sync, no duplicate outlet created |
| FS-05 Rep suggests beat change, admin accepts/reject | Rep submits suggested change; shows in admin queue; accept updates beat plan, reject leaves it unchanged | Integration | BRD flags this "Partial (table exists)" — test is a **gap check**: confirm whether screen exists before writing pass/fail criteria | Offline queue same pattern as FS-02 if screen exists |
| FS-06 Driver delivery confirmation (kg delivered, short-delivery reason, cash, payment mode) | Driver confirms delivered kg per item; short-delivery reason required if qty < ordered; cash collected + payment mode recorded | Instrumented + Integration | Matches BRD §6.4 FS-06 + Appendix J test #8 (delivered order → influencer credit flows, commission appears next monthly run) | **Must work fully offline — explicitly called out in ADR-001 as "must not block on signal in a store basement."** Confirm delivery + cash entry offline → clearly flagged "unsynced" until confirmed (no double-collection risk at handover) |
| FS-07 Admin deliveries/orders/team views | **Scope TBC — see §1.** If in native app scope: deliveries list, completed-today, order cancellation, team approvals all reflect server state correctly, admin-only (Appendix G matrix) | Integration | Role-restricted per Appendix G; Appendix J test #13/14/15 (rep sees own beat only, driver sees own deliveries only, anonymous denied) apply here | n/a if web-only |
| FS-08 Holidays calendar (skip beat days) | Holiday added → beat generation for that date skips affected outlets (fortnightly/weekly) | Integration | Matches Appendix J test #12 | BRD flags "Partial (table exists), screens not live" — gap check first |
| FS-09 Order edit history | Edited order retains history of changes | Integration | BRD flags "Partial, table exists, unused" — **gap check**: confirm feature exists in native app before testing | n/a until built |

---

## 3. Offline-sync edge cases (ADR-001, applies across all offline-capable actions: order, add-outlet, stock pickup, settle, cash received, delivery confirmation, ad-hoc invoice)

| # | Edge case | Expected result | Level |
| --- | --- | --- | --- |
| OS-1 | Submit action with no connectivity | Writes to local outbox immediately; UI shows "Pending sync," app remains usable | Instrumented |
| OS-2 | Connectivity returns | WorkManager background sync drains outbox in creation order per entity, without user action | Instrumented |
| OS-3 | Sync retried after a timeout (e.g. request sent, ack lost) | Server-side idempotency on client UUID prevents duplicate order/delivery/cash-entry creation | Integration (needs backend idempotency support — flagged by Architect to Backend/API Developer as a contract requirement; QA cannot pass this until that server-side change exists) |
| OS-4 | Server rejects a queued write (e.g. duplicate same-day order) | Surfaces to rep as a clear in-app conflict requiring a decision — never silently dropped, never silently overwritten | Integration |
| OS-5 | Create invoice offline | Queued as draft with local reference, no fake AFF-YYMM-NNN number generated client-side; real number assigned only on sync; UI reads "Invoice pending — number assigned once synced" | Instrumented + Integration |
| OS-6 | Token can't refresh mid-shift (offline) | Rep is NOT force-logged-out; keeps working on cached session | Instrumented |
| OS-7 | Online but server actively rejects token (expired/revoked) | Re-auth prompt shown (only trigger for forced logout) | Integration |
| OS-8 | Network unreachable at launch | Falls back silently to cached session + queued-work mode; no scary error | Instrumented |
| OS-9 | App killed mid-sync / low battery | Outbox entries survive restart (Room persistence); sync resumes, no data loss | Instrumented |
| OS-10 | Two reps/devices editing the same outlet concurrently | Conflict handled per server-source-of-truth policy; no silent data loss on either device | Integration, needs 2 test devices/emulators |

---

## 4. Hard blocker — cannot execute any Integration-level row above yet

Per Orchestrator's note to the room (2026-10-10) and confirmed independently in ADR-001 L94-100:
the Supabase project is **live production data, mislabelled "DEV," with no real staging
environment**. Swat's rule, relayed by Orchestrator: **no dev or test work touches the real
Supabase project directly, at any point.**

Consequence for this test plan: every row marked **Integration** above is written and ready,
but **cannot be run** until DBEngineer/DevOpsEngineer stand up an isolated dev/staging
Supabase project with synthetic seed data (schema parity with production). Unit and
Instrumented rows (pure Room/WorkManager/pricing-math, no network) can be run as soon as
MobileDev has a branch with code to test — those are not blocked.

QA will not sign off any feature as done/mergeable based on Unit+Instrumented passing alone
if that feature's row includes an Integration case — the gate needs the real pass, not a
reasoned-through one.

---

## 4b. Addendum 2026-10-12 — realigned to BizAnalyst's AF-SA-FRS-001 v1.0

BizAnalyst delivered the numbered FS-01..09 backlog with concrete pass/fail acceptance criteria
(`attachments/absolute-fresh-sales-app-functional-requirements-v1.0.md`, adopted by Architect
into ADR-001 Decision 4). This is now the acceptance-criteria source of truth, superseding this
plan's §2/§3 wording where they differ. Changes QA is carrying forward:

- **Appendix K (milkman): BizAnalyst recommends OUT of native v1**, deferred to a separately
  approved epic — Swat confirmation still pending (open item 10 in AF-SA-FRS-001). §3's K5 rows
  are **parked, not deleted** — do not test, do not count against any v1 branch sign-off, revisit
  only if Swat approves a milkman epic.
- **Global Acceptance Criteria (GAC 1-16)** in AF-SA-FRS-001 apply across FS-01..06,08,09 and
  are more precise than my §3 offline edge cases (status vocabulary: Pending/Syncing/Synced/
  Failed-retrying/Action required-conflict/Rejected; outbox survival; dependency sync order
  outlet→order→invoice/delivery; audit of every sync/retry/conflict/rejection event). Every
  branch's sign-off (§5) must demonstrate GAC 16's automated evidence set (offline mutation,
  process-death recovery, reconnect, idempotent retry, dependency order, rejection, conflict
  presentation) — not just the feature-specific rows in §2.
- **Open blocker, FS-03 only, flagged by Architect (not me originating it):** AF-SA-FRS-001's
  duplicate-order rule ("both orders persist, flagged for review") contradicts the hard unique
  `(outlet_id, order_date)` DB constraint already locked under ADR-001 Decision 3. QA cannot
  write a pass/fail test for FS-03's duplicate-order case until BizAnalyst resolves which rule
  stands — tracked as **FS-03 test row OS-3/duplicate-order: BLOCKED on business-rule call**,
  everything else in FS-03 proceeds.
- Per-feature acceptance criteria in AF-SA-FRS-001 (e.g. FS-02 AC5 "retry cannot create duplicate
  visits," FS-04 AC5 "possible duplicates shown for review, no silent merge/delete," FS-09 AC5
  "no silent last-write-wins on financial/stock/invoice/delivery/outlet-identity data") are more
  specific than my §2 rows and will be used verbatim as the pass/fail bar once code exists to
  test — §2 is not being rewritten line-by-line today to avoid drift from a still-partially-open
  document (12 unresolved business decisions remain, listed in AF-SA-FRS-001's final section).

## 5. Branch sign-off checklist (apply per feature branch before "close")

1. All Unit + Instrumented rows for the feature: **Pass**, test output attached/logged.
2. All Integration rows for the feature: **Pass** against dev/staging Supabase (not production) — or explicitly **N/A** with reason.
3. Applicable offline-sync edge cases (§3) covering that feature's writes: **Pass**.
4. Any BRD "Partial"/"Gap" status item is either confirmed built-and-tested or explicitly deferred with Orchestrator/BizAnalyst sign-off (not silently skipped).
5. Any bug found is filed to the owning developer (MobileDev/Backend) with repro steps, not fixed by QA.
6. QAEngineer posts sign-off in the mission room referencing this file + run evidence before Orchestrator marks the branch closed.

---

## 6. Build requirements (design-milestone deliverable, added 2026-10-12 per Swat's t10u)

Owner: QAEngineer · Reviewer: Architect + MobileDev (needs their confirmation on module layout/variants before locked)

- **Build variants required:** `debug` (points at isolated dev/staging Supabase once DBEngineer/DevOpsEngineer provision it — never production) and `release` (points at production, signed, only built after a feature's full §5 sign-off). No variant may point Integration-test runs at production under any config.
- **CI commands QA will run per branch, unchanged by this milestone:** `./gradlew testDebugUnitTest` (Unit), `./gradlew connectedDebugAndroidTest` (Instrumented, emulator/device required — API level TBC pending Architect's min-SDK call), Integration rows run manually/scripted against staging once it exists.
- **Test fixtures needed before FS-03/FS-06 Unit rows can run:** concrete tier-price table (T1-T4) and threshold/outlet discount values, and the exact short-delivery-reason validation rule — both still pending from BizAnalyst per my t5s3 ask; AF-SA-FRS-001 should be checked for these before I treat this as resolved.
- **Acceptance criteria for this deliverable:** build variants + CI commands agreed by Architect/MobileDev; no Integration test or CI job is wired to the production Supabase project at any point (QA will check this directly, not take it on trust) before any branch is marked ready for release.
- **Open dependency:** this section assumes Architect's module/package structure (not yet shared with me) — will revise once ADR-002 build-structure details are confirmed, flagging now rather than guessing at Gradle module names.
