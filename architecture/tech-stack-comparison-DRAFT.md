# Tech Stack Candidate Comparison — Draft (Swat's "Absolute Fresh" Mobile App)
Status: SUPERSEDED 10 Oct 2026 — decision LOCKED (Kotlin + Jetpack Compose + Room). See
`ADR-002-stack-and-branch-workflow.md` for the final decision and rationale. Kept here for
the historical comparison only.

## Why this draft exists
Prepping the comparison now so we aren't idle while BizAnalyst/Swat requirements land. Nothing here is a commitment.

## Candidates
1. **Kotlin + Jetpack Compose** (native Android, single-platform now, Compose Multiplatform as future iOS path)
2. **Flutter (Dart)** (cross-platform from day one, single codebase for Android + iOS)

## Decision factors to weigh once requirements exist
- **iOS timeline**: Program brief says "future iOS support." If iOS is near-term (<6-12mo post-Android launch), Flutter's single-codebase story is a strong pull. If iOS is a distant maybe, native Compose + later Compose Multiplatform avoids cross-platform overhead now.
- **Offline capability needs**: Both support local persistence well (Room/SQLDelight for Kotlin; sqflite/drift or Isar for Flutter). No clear winner until we know sync complexity (simple cache vs. conflict-resolving offline-first sync).
- **UI complexity / custom design from Stitch mockups**: Stitch exports are typically Figma-like static designs — both stacks can implement arbitrary custom UI; Compose has tighter native Material3 fidelity, Flutter has strong custom-widget control too.
- **Team/tooling**: RESOLVED (checked antigravity.google, 2026-10-10) — Google **Antigravity** is a general-purpose agentic IDE/dev platform (VS Code-family editor + AI agent manager), not an Android Studio variant and not tied to any one language or framework. It works over any codebase. So it does **not** favour Kotlin/Compose over Flutter or vice versa — this factor is neutral and drops out of the decision.
- **Integration/backend contracts**: Stack choice doesn't materially change API contract design (REST/GraphQL either way), so this isn't a deciding factor.
- **Team size / maintenance**: Single codebase (Flutter) reduces long-term maintenance if iOS is committed; native Compose is usually preferred if Android-only for a long while and performance/platform-fidelity matters more.

## What I still need before recommending
- [ ] BizAnalyst requirements / user stories (functional scope, offline requirements, integrations)
- [ ] Swat's product description: purpose, core features, target users, iOS timeline
- [x] Confirmation of Antigravity's Flutter vs native Android tooling support — checked 2026-10-10, Antigravity is framework-agnostic, not a deciding factor (see above).

## Next step
Once the above lands, I will produce a final recommendation with rationale, write it to this folder, and flag it to Orchestrator for escalation to Swat before locking it in — per standing instructions, no paid third-party service/library will be selected without Swat's explicit authorisation.
