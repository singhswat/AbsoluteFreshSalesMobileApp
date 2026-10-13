# ⚠️ VERIFICATION BUILD — FAKE AUTH / PLACEHOLDER STYLING / NOT FOR PRODUCTION

**Files:**
- `signin-verification-debug-2026-10-13.apk` — debug APK, installable on any Android device/emulator
  (minSdk 24+) via `adb install signin-verification-debug-2026-10-13.apk`, or drag onto a running
  emulator window.
- `signin-verification-screenshot-2026-10-13.png` — screenshot of the Sign-In screen (§2.1 default
  state) taken from this APK running on a Pixel 10 Pro XL emulator.

**What this is:** QAEngineer's local build of MobileDev's Sign-In/Beat scaffold
(`attachments` sent 2026-10-13), with 3 build-blocker fixes applied on QA's side (missing
`gradle.properties`, missing Kotlin Compose Compiler plugin, missing `res/values/styles.xml` —
see `QA/verification-signin-scaffold-2026-10-13.md`). Built and run for real: `BUILD SUCCESSFUL`,
10/10 SignInViewModel unit tests pass, app launches and renders on an emulator.

**What this is NOT:**
- Not MobileDev's canonical `project-scaffold/` — that tree still needs the Compose Compiler
  plugin fix confirmed before it builds clean (unresolved as of this writing).
- Not wired to any real backend — `FakeAuthRepository` only, no Supabase/network calls possible.
- Not final visual design — colours/spacing are placeholders per spec §4 ("not yet signed off"),
  no Stitch assets applied.
- Not on a git branch — no version control exists for this code yet.
- Not QA-signed-off or release-ready in any sense. This is a UI-renders-and-logic-passes
  verification artifact only, produced to answer "where do I go to see this screen" (t23u).

**Owner:** QAEngineer. **Do not install on a production/distribution channel.**
