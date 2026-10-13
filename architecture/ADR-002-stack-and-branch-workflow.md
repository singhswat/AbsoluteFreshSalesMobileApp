# ADR-002: Tech Stack LOCKED + Branch-per-Feature Workflow — Field Sales Android App

Status: Decision LOCKED by Architect, per Orchestrator's explicit request (t0s1, 10 Oct 2026)
following Swat's "build this / branch per feature, test, close" instruction (t0u).
**Confirmed directly by Swat 10 Oct 2026 10:33** ("yes native android") — fully signed off,
not just Architect/Orchestrator internal.
Author: Architect · 10 Oct 2026
Supersedes: `tech-stack-comparison-DRAFT.md` (the two open blockers on that draft are now resolved,
see below). Builds on ADR-001 (offline-first, Supabase Auth, same project/schema, hard
dev/staging gate).

## Decision: Kotlin + Jetpack Compose + Room (native Android, v1 Android-only)

**Ruling: native Kotlin + Jetpack Compose**, with Room for local persistence (per ADR-001's
offline design) and the official Supabase Kotlin SDK for Auth/Postgrest/Realtime. No Flutter,
no cross-platform framework for v1.

**Why this resolves the two blockers the draft was waiting on:**
1. **iOS timeline** — resolved, not "near-term." Scope (confirmed BRD.md + ADR-001) is the
   internal field-sales app for ~10-15 Reps/Drivers/Admin, who are issued or use Android
   devices for this role; I searched BRD.md for any iOS requirement tied to this app (FS-01…
   FS-09, §6.4) and found none — iOS only ever comes up for the separate D2C consumer site,
   which is out of scope here. With no near-term iOS commitment for *this* app, Flutter's
   main advantage (single codebase for two platforms) doesn't apply, and native Compose's
   advantages (tighter Material3 fidelity, no cross-platform runtime overhead, direct Room/
   WorkManager integration already specified in ADR-001) win outright.
2. **BizAnalyst requirements** — resolved via BRD.md §6.4 (FS-01…FS-09) and ADR-001: a modest,
   well-bounded feature set (beat plans, orders, deliveries, cash, invoices, outlet
   add/edit), offline-first, 10-15 users. This is squarely within native Android's comfort
   zone; nothing in the scope needs Flutter's cross-platform reach.

**Other factors, for the record:**
- Free/open-source only, per standing instruction: Kotlin, Compose, Room, WorkManager,
  Supabase Kotlin SDK (Apache-2.0/MIT, open source) — no paid service or library introduced.
- Reuses existing backend/schema as-is (ADR-001) — stack choice doesn't change the API
  contract, so this was never a deciding factor either way.
- If iOS becomes a real near-term requirement later for this same app (not just the D2C
  site), Compose Multiplatform is the natural next step from this codebase rather than a
  rewrite — flagging as a future option, not a current plan.

**If Swat wants this re-opened:** say so and I'll revisit — per standing instructions this
was flagged to Orchestrator before being locked in (Orchestrator explicitly requested the
lock at t0s1), and no paid tooling is involved, so it does not need separate sign-off beyond
that — but it's still a major architecture call, so flagging the rationale here for visibility.

## Branch-per-feature workflow — github.com/singhswat/AbsoluteFreshSalesMobileApp

Reading Swat's "for every feature create a new branch, test and close" (t0u) together with
Orchestrator's framing (t0s1): one feature = one branch, implemented, tested against a sign-off
gate, then merged and the branch closed. Not a single big-bang branch.

**I cannot action this myself** — I have no Shell/git access, so I cannot clone, branch, or
push to that repo. This section is the policy MobileDev executes with their Shell access;
QAEngineer's per-feature test plan (requested by Orchestrator) is the sign-off gate below.

**Branch unit = one BRD FS item** (from BRD.md §6.4), matching Orchestrator's ask to
BizAnalyst for a numbered backlog:
| Branch | Feature (BRD ref) |
| --- | --- |
| `feature/fs01-beat-plan-sync` | FS-01 beat plan (read-only on device; built by admin on web) |
| `feature/fs02-beat-visit-list` | FS-02 today's outlets, visited/missed |
| `feature/fs03-order-capture` | FS-03 place order, tier price/discount |
| `feature/fs04-add-outlet` | FS-04 add outlet in field |
| `feature/fs05-beat-change-suggest` | FS-05 suggest beat changes (Could — later branch) |
| `feature/fs06-delivery-confirm` | FS-06 driver delivery confirmation, cash collected |
| `feature/fs07-admin-lists` | FS-07 — web-side per BRD scope note (admin stays on web per ADR-001); exclude from mobile backlog unless BizAnalyst says otherwise |
| `feature/fs08-holiday-calendar` | FS-08 (Could — later branch) |
| `feature/fs09-order-edit-history` | FS-09 (Should — later branch) |
| `feature/00-project-scaffold` | Project setup: Compose skeleton, Room schema, Supabase SDK wiring, CI — must land *before* any FS branch, as its own branch/PR |

Sequencing note: `00-project-scaffold` first; `fs01`/`fs02` next — **clarified 10 Oct 2026
per QAEngineer's question (t1s1):** "not blocked" means these branches can be opened and
*coded* now (Compose UI, Room schema, mock/seed data) without waiting on staging. It does
**not** mean Integration-level tests for fs01/fs02 may run reads against the real Supabase
project. ADR-001's rule as relayed by Orchestrator is literal — "no architecture, integration,
dev or test work touches the real Supabase project directly, at any point" — "touches" covers
reads, not just writes; the whole reason is BRD R1 (anon key currently has broad read/write on
real tables) plus R4 (the project everyone's been calling "DEV" is actually production). A
read-only Integration test still opens a live connection to that project with that same
under-secured key. So: fs01/fs02 Unit + Instrumented tests can run today against MobileDev's
build (no backend needed); their Integration-test rows queue behind the staging project
exactly like every write-path feature, with no read/write carve-out. Order/delivery/cash/
invoice write-path branches (`fs03`, `fs06`) are blocked the same way, for both dev and test.

**Per-branch workflow:**
1. Branch from `main`: `feature/<name>` above.
2. Implement against this ADR + ADR-001 + BizAnalyst's acceptance criteria for that FS item.
3. **Test gate before merge** — QAEngineer's per-feature test plan must pass, specifically
   including: offline capture + queued sync for any write-path feature, idempotent-retry
   (duplicate submit after timeout does not double-write), and the conflict scenarios ADR-001
   lists for that feature if applicable.
4. **Hard gate carried over from ADR-001 — do not skip:** no branch that performs a real
   write (order, delivery, cash, invoice, outlet edit) against Supabase runs its tests or
   any dev work against the production project. It targets the isolated dev/staging project
   DBEngineer is provisioning. Read-only branches (fs01, fs02, scaffold) are not blocked by
   this, everything else is, until that staging project exists.
5. Merge to `main`, close the branch (delete the remote branch; do not leave stale branches
   open) once merged and tests are green.
6. One branch open at a time per feature — do not stack unrelated changes into a feature
   branch.

## Still open
- [ ] I cannot see the current contents of github.com/singhswat/AbsoluteFreshSalesMobileApp
      myself (no Shell; WebFetch on github.com has failed/timed out twice — likely a
      JS-rendered page my web-read tool can't execute). If that repo already contains a
      scaffold (different stack, existing structure), that changes `00-project-scaffold`
      above — MobileDev, please confirm what's actually in the repo before starting it.
- [ ] Hard gate from ADR-001: isolated dev/staging Supabase project, still pending DBEngineer.
- [ ] BizAnalyst's numbered backlog/acceptance criteria (Orchestrator requested) will refine
      the branch table above — treat it as a confirmed starting point, not final.
