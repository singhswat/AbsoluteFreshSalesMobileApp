# QA Verification — Full Tree (Sign-In + Beat + Stock + Nav), Round 3

**Date:** 2026-10-13
**Tester:** QAEngineer
**Scope:** MobileDev's full 3-batch resend of `project-scaffold` — this is the first time Sign-In, Beat, Stock and the RootScaffold nav wrapper have all compiled and tested together as one tree.

## What was integrated
- Batch 1/3: `settings.gradle.kts`, root `build.gradle.kts`, app `build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`, `ui/theme/{Color,Type,Theme}.kt`, `navigation/{Screen,NavGraph}.kt
- Batch 2/3: `navigation/RootScaffold.kt` (bug-2 fix — missing `getValue` import), `MainActivity.kt`, full `feature/auth/*` (AuthRepository, SignInUiState, SignInViewModel, SignInScreen), full `feature/beat/*` (BeatUiState, BeatRepository, BeatViewModel, BeatScreen)
- Batch 3/3: full `feature/stock/*` (StockScreen, StockUiState, StockViewModel — bug-1 and bug-3 fixes: color-token names aligned with Color.kt, `material-icons-extended` dependency added), `SignInViewModelTest.kt`

`gradle.properties` and `res/values/styles.xml` were left as previously verified (not resent this round, no changes expected there).

## Command run
```
gradle clean compileDebugKotlin testDebugUnitTest --rerun-tasks
```
(Gradle 9.6.0 invoked directly with JBR JAVA_HOME — see standing note below on wrapper/JDK mismatch.)

## Result
**BUILD SUCCESSFUL.** 23/23 tasks executed, 0 failures.

Only 2 deprecation warnings (non-blocking, cosmetic):
- `BeatScreen.kt:353` — `Icons.Filled.ArrowForward` deprecated, suggests `Icons.AutoMirrored.Filled.ArrowForward`
- `BeatScreen.kt:431` — `Icons.Filled.ReceiptLong` deprecated, suggests `Icons.AutoMirrored.Filled.ReceiptLong`

### Unit tests: 10/10 pass, 0 failures, 0 errors
`SignInViewModelTest` — confirmed from JUnit XML (`app/build/test-results/testDebugUnitTest/`), not console-eyeballed:
- successful sign-in as rep routes with REP role — PASS
- initial state is an empty disabled form — PASS
- submit disabled until both fields non-empty — PASS
- no cached session on cold start stays on empty form — PASS
- account inactive routes to blocking dead-end state — PASS
- invalid credentials clears password, keeps identifier, shows inline error — PASS
- offline first login routes to offline dead-end with retry — PASS
- pending approval routes to blocking dead-end state — PASS
- successful sign-in as driver routes with DRIVER role — PASS
- cached session on cold start skips form and routes by role directly — PASS

## Prior bugs confirmed fixed
All 3 bugs filed against the previous Stock+theme batch are confirmed resolved in this tree:
1. Color-token name mismatch (StockScreen vs Color.kt) — resolved, compiles clean.
2. Missing `androidx.compose.runtime.getValue` import in RootScaffold.kt — resolved.
3. Missing `material-icons-extended` dependency for `LocalShipping` icon — resolved, present in `libs.versions.toml` + `app/build.gradle.kts`.

## Standing items (unchanged)
- Gradle wrapper pinned to 8.13, incompatible with JDK 25 (only JDK on this machine) — had to invoke cached Gradle 9.6.0 directly with JBR JAVA_HOME. Needs DevOps/Architect to settle wrapper/JDK pairing before CI.
- No `androidTest` source yet — Instrumented tier still blocked.
- No isolated dev/staging Supabase project yet — Integration tier still blocked; not testing against production per room agreement.

## Not yet verified
- RootScaffold's 4-tab bottom nav wiring into NavGraph was flagged as not wired in the previous round — not re-checked this round since it's a navigation/UX concern, not a compile/test failure. Worth a manual/instrumented check once androidTest exists.
