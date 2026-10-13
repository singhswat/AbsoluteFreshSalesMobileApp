# Verification run — Sign-In/Beat scaffold (MobileDev's `project-scaffold/`, sent via attach)
QAEngineer · 13 Oct 2026

## What I did
Reconstructed the 21-file tree MobileDev attached into a real Gradle project in my own workspace
(`android-verify/`), using local Gradle 8.13 + JDK 21 (JetBrains Runtime) already cached on this
machine, and ran `./gradlew testDebugUnitTest`.

## Result: **BUILD SUCCESSFUL. 10/10 unit tests pass, 0 failures, 0 errors.**
`SignInViewModelTest` — all 10 cases green (initial state, submit-enable gating, invalid
credentials, account inactive, pending approval, offline-first-login, success-as-rep,
success-as-driver, cached-session skip, no-cached-session-stays-on-form). Confirms the
ViewModel's behaviour matches `sign-in-screen-package-v1.0.md` §2/§3 for every state it claims
to cover.

## Real bugs found — needed 3 fixes before it would build on ANY machine, not just mine
These are missing from MobileDev's tree as sent, not local environment quirks:

1. **Missing `gradle.properties`** — no file existed at all. Without
   `android.useAndroidX=true`, AGP fails at `:app:checkDebugAarMetadata` because the declared
   AndroidX dependencies (lifecycle, compose, navigation) conflict with the legacy-support default.
2. **Missing Compose Compiler Gradle plugin** — `build.gradle.kts` enables `buildFeatures.compose
   = true` under Kotlin 2.0.20 but never applies `org.jetbrains.kotlin.plugin.compose`. Since
   Kotlin 2.0, that plugin is mandatory whenever Compose is enabled, or the build fails outright
   at `VariantTaskManager` with "Compose Compiler Gradle plugin is required."
3. **Missing `res/values/styles.xml`** — `AndroidManifest.xml` references
   `@style/Theme.AbsoluteFreshSalesApp` (twice) but no style of that name was ever defined
   anywhere in the tree. AAPT resource-linking fails without it.

I patched all three locally with minimal placeholders (noted inline in the files) purely to get a
real build/test result — these are environment-equivalent fixes, not design decisions. **MobileDev
needs the same three additions in the actual `project-scaffold/`/future branch**, or anyone else
who tries to build this hits the identical failures.

## What this does NOT cover
- Beat screen / `BeatViewModel` — no test file was written for it yet (only SignIn has tests).
- §2.6b (server-unreachable) and §2.8 (session-expired mid-shift) — deliberately excluded per spec,
  still blocked on Architect's contract per earlier room discussion.
- Compose UI rendering (`SignInScreen.kt`) — only compiled, not instrumented/UI-tested; that needs
  `connectedDebugAndroidTest` on a device/emulator, out of scope for a unit-test pass.
- Still zero Integration-level tests — no staging Supabase exists; this is 100% local/fake-repo
  Unit-test evidence.

## Environment note for whoever builds this for real
Local Android SDK, Gradle 8.13 (cached), and JBR 21 (`C:\Users\singh\.jdks\jbr-21.0.11`) are
present on this machine and now proven sufficient to build AGP 8.6/Kotlin 2.0.20 projects — useful
if MobileDev's Shell/tooling access lands and needs a known-working JAVA_HOME.
