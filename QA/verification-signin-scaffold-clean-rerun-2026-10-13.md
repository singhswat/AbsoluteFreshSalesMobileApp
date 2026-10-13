# Clean Verification Rerun — Sign-In/Beat Scaffold (2026-10-13, round 2)

**Requested by:** Orchestrator ("clean rerun" ask) / MobileDev (confirm build + 10/10 pass with zero local patches)
**Run by:** QAEngineer
**Build tool:** Gradle 9.6.0 (direct invocation — see note on wrapper below), JDK: Android Studio bundled JBR 25.0.3

## What changed since the first verification run (verification-signin-scaffold-2026-10-13.md)
MobileDev applied the two genuinely-new fixes from that first report as canonical files and sent them:
- `project-scaffold/gradle.properties` (android.useAndroidX=true, android.nonTransitiveRClass=true, org.gradle.jvmargs)
- `project-scaffold/app/src/main/res/values/styles.xml` (defines `Theme.AbsoluteFreshSalesApp`, fixes the AAPT resource-linking failure)

The third fix (Compose Compiler Gradle plugin) was confirmed already present in MobileDev's canonical tree from an earlier parallel-draft fix — verified directly in `build.gradle.kts` / `app/build.gradle.kts`, both already had `id("org.jetbrains.kotlin.plugin.compose")`.

## What I did for this rerun
1. Took my own verify copy (`android-verify/`), **deleted my own local patch files** (my prior `styles.xml`/`gradle.properties` versions written during the first verification pass).
2. Wrote in MobileDev's two attached files **byte-for-byte**, no edits.
3. Ran `gradle clean testDebugUnitTest --rerun-tasks` — a full clean build, not incremental, so nothing is riding on cached state from the earlier run.

## Wrapper note
MobileDev's tree ships no `gradlew`/wrapper files (acknowledged in their README — Shell is `deny` for them, so they can't generate one). I used a locally cached **Gradle 9.6.0** distribution directly instead of generating/committing a wrapper on their behalf, per my own earlier offer. Separately: the project's own `gradle-wrapper.properties` pins Gradle 8.13, which does **not** support JDK 25 (this machine's only available JDK, via Android Studio's bundled JBR) — 8.13 fails at configuration with "Dependency requires at least JVM runtime version 11. This build uses a Java 8 JVM" / JDK mismatch errors. Gradle 9.6.0 runs clean on JDK 25. This is a real build-environment constraint worth DevOps/Architect knowing about before CI is wired up (§ below).

## Result
**BUILD SUCCESSFUL in 45s. 10/10 unit tests passed, 0 failures, 0 errors, 0 skipped.**

```
successful sign-in as rep routes with REP role : PASS
initial state is an empty disabled form : PASS
submit disabled until both fields non-empty : PASS
no cached session on cold start stays on empty form : PASS
account inactive routes to blocking dead-end state : PASS
invalid credentials clears password, keeps identifier, shows inline error : PASS
offline first login routes to offline dead-end with retry : PASS
pending approval routes to blocking dead-end state : PASS
successful sign-in as driver routes with DRIVER role : PASS
cached session on cold start skips form and routes by role directly : PASS
```
(Source: `app/build/test-results/testDebugUnitTest/*.xml`, parsed directly — not eyeballed from console.)

## Flag for DevOpsEngineer / Architect (not a blocker on this sign-off)
Whatever CI runner eventually runs `./gradlew testDebugUnitTest` for real needs a JDK that matches the **Gradle wrapper version actually committed to the repo**, not whatever JBR happens to be on a dev machine. Recommend: either (a) bump the committed wrapper to Gradle 8.9+/8.10+ with a JDK 17/21 target (standard, well-supported combo for AGP 8.6), or (b) if JDK 25 is intentional, bump the wrapper to 9.x and pin that project-wide. Mixing "whatever JDK is on the box" with a pinned 8.13 wrapper is exactly the kind of thing that passes on one dev's machine and fails in CI.

## Sign-off
Sign-In + Beat scaffold Unit tier: **tested and passing**, verified twice now (first pass with my own interim patch, this pass with MobileDev's canonical fix files, clean build, zero local patches). Safe for MobileDev to open the real `feature/00-project-scaffold` branch on this baseline.

Still blocked, unchanged: Instrumented tier (no androidTest source yet), Integration tier (no isolated staging Supabase yet).
