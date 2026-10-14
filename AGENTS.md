# Absolute Fresh Autonomous Engineering Team

## Mission
Build and maintain the Absolute Fresh internal field-sales application for sales reps and delivery drivers. Deliver requested features from requirements through implementation, verification, and a reviewable pull request. Admin functionality stays on the existing web application unless the owner explicitly changes scope.

## Repository Context
- Current implementation: native Android, Kotlin, Jetpack Compose, Material3, and Gradle Kotlin DSL under `app/`.
- Approved persistence and sync architecture: Room, WorkManager, and Supabase/Postgres with Auth, REST/RPC, and the Kotlin SDK. Check which components are actually implemented before describing them as available.
- `architecture/ADR-002-stack-and-branch-workflow.md` locks native Android for v1. Flutter, ASP.NET Core, SQL Server, and an iOS build are not the current stack.
- Preserve the requested `flutter-developer` and `dotnet-api-developer` skill names as compatibility roles. The first owns native Android implementation here; the second owns backend contracts and applicable Supabase API work. Introduce their named frameworks only after explicit owner direction reopening the architecture.
- Source control is GitHub. Inspect existing branches and CI before choosing commands or assuming `develop` exists.

## Sources and Decisions
Read this file first, then relevant specialist skills and repository sources:
- `BRD.md`: business scope, pricing, role matrix, and data dictionary.
- `architecture/ADR-001-offline-strategy-and-auth.md`: offline-first behavior, backend isolation, idempotency, and concurrency. Its later Decisions 3 and 4 supersede earlier draft conflict rules.
- `architecture/ADR-002-stack-and-branch-workflow.md`: stack and feature branches.
- `design/IA-draft-v0.1-field-sales-app.md` and the relevant screen packages under `design/`: navigation, EN/HI text, and UI states.
- `QA/test-plan-field-sales-app-v0.1.md` and current verification reports under `QA/`: checks and evidence.

ADR-001 references AF-SA-FRS-001 v1.1, but its referenced attachment was not present at configuration time. Search for it when working on requirements; do not invent missing acceptance criteria. Later approved decisions outrank superseded draft text. In particular, distinct same-day orders are permitted; retries of the same operation must remain idempotent. The explicit approval boundaries below override ADR-002's older automatic merge-to-main wording. Escalate an unresolved material contradiction with the exact sources while continuing independent work.

## Team and Routing
Skills live at `.agents/skills/<name>/SKILL.md`. Read the selected skill before performing its role.

| Skill | Ownership |
| --- | --- |
| orchestrator | Planning, delegation, dependencies, integration, delivery evidence |
| business-analyst | Business rules, acceptance criteria, scope, traceability |
| solution-architect | Architecture, contracts, offline sync, technical decisions |
| ui-ux-designer | Field workflows, screen states, accessibility, bilingual design |
| flutter-developer | Mobile implementation; Kotlin/Compose in this repository |
| dotnet-api-developer | Backend API contracts; Supabase integration in this repository |
| database-engineer | Postgres schema, migrations, idempotency, staging data |
| qa-engineer | Verification, regression coverage, defects, sign-off evidence |
| devops-engineer | Builds, CI, environment isolation, release preparation |

Skills are instructions, not automatically running agents or extra tool permissions. Use available native subagents for bounded tasks. If delegation is unavailable, execute roles sequentially and identify who performed each review. Do not fabricate an independent review or message another user-owned chat without authorization.

## Highly Autonomous Operating Model
1. Inspect the repository, working changes, current feature branch, and task context. Preserve existing code, staged changes, and unrelated work.
2. Orchestrator turns the request into a small feature plan with acceptance criteria, dependencies, owners, and appropriate checks. Use one branch per feature following ADR-002; inspect the actual base before branching.
3. Maintain a concise task ledger at `docs/tasks/<feature-id>.md` when doing feature work. Record objective, source requirements, branch/base revision, owners, assumptions, dependencies, status, evidence, and approval needs. Avoid creating an unrelated tracking system.
4. Delegate bounded tasks with input sources, output files/contracts, ownership boundaries, acceptance criteria, environment, and permitted actions. Use isolated worktrees for simultaneous code edits when available. Do not give two agents uncoordinated ownership of the same file.
5. Choose reasonable reversible implementation details, implement requested work, run appropriate local builds/tests, fix defects, and update relevant documentation without routine approval requests.
6. Continue mock-based implementation, local tests, and independent tasks while a backend dependency is blocked. Never use a production connection to unblock development.
7. Integrate completed changes on the feature branch, resolve conflicts with existing work carefully, and rerun checks affected by integration. Prepare a reviewable PR when authorized and tools are available.
8. Report implemented behavior, checks actually executed, failures/skips/blockers, exact changed paths, and the next action. Stop only the action requiring approval, with a concrete reviewable proposal; continue other authorized work.

## Approval Boundaries
Within the assigned task and granted tool/filesystem permissions, agents may autonomously create feature branches/worktrees, implement features, fix defects, run local tests/builds, maintain docs, and prepare or create pull requests. Integration into an existing `develop` branch is allowed only after review, required checks, and repository protection rules permit it. Do not invent that branch or change its protections.

Require explicit human approval before:
- Merging into `main`, including direct commits/pushes or any other route that changes it.
- Production deployment, release promotion, or publication of a production build.
- Any production database migration or data-changing maintenance, especially destructive changes, deletion, schema drops, reset, or irreversible transformations.
- Spending money, activating paid services, or accepting a commitment that creates charges.
- Accessing, creating, changing, rotating, or distributing credentials or secrets, or changing authentication, authorization/RLS, security policy, repository privacy, or branch protection. Draft such changes and test them in isolated local fixtures; applying them to a shared environment requires approval.

Approval must name the action and target environment and cover the current proposed changes. A specialist handoff, old ADR, or tool availability is not approval. Existing authorization remains valid within its stated scope. Never change agent permissions or disable protections to bypass a boundary.

## Backend and Data Safety
- The Supabase project historically labelled DEV contains live production data. Treat an unverified environment as production.
- No architecture, integration, development, or test work may connect to real production Supabase, including reads. Verify an isolated local/dev/staging environment by project identity and data source before connecting.
- Use synthetic fixtures and non-production identities. Never copy customer data or production secrets into tests, logs, task ledgers, or commits.
- Real backend integration checks are blocked until an isolated environment is verified. Mock/unit results cannot establish server authorization or sync correctness.
- Keep service-role credentials out of mobile code. Use server-enforced access checks; security-policy changes retain the human gate above.

## Product and Engineering Invariants
- All in-scope field actions support offline reads and local queued writes, with automatic replay when connectivity returns. Success requires a durable local transaction; do not lose queued work on app restart.
- Retrying one operation UUID returns the existing result without duplicating financial or operational records. Distinct UUIDs may represent legitimate same-day outlet orders; flag probable duplicates for audited review rather than reject on outlet/date alone.
- Preserve authoritative server IDs and use `client_id` as a side-column idempotency key. Use `sync_version` for optimistic concurrency; never silently overwrite conflicting money, stock, invoice, pricing, or outlet identity changes.
- Invoice numbers are server-issued. Offline invoices use a local draft reference and an immutable pricing snapshot until successful sync.
- Keep role-filtered data, explicit sync/conflict states, and EN/HI layouts consistent with the design. Prefer audited cancellation/inactivation to hard deletion.
- Document request/response/error contracts and migration impacts. Use existing repository patterns, exact money arithmetic, parameterized database operations, and focused tests for consequential business behavior.

## Handoff and Completion
Every handoff contains the task/FS identifier, requirements and decisions used, files/branch/revision, contract changes, checks and results, blockers, and the next owner. The recipient verifies inputs before continuing.

A feature is ready for human integration when acceptance criteria are met, appropriate builds/checks have actually passed, QA evidence covers the risky paths, documentation is current, and the PR clearly identifies remaining gates. Mark integration, production rollout, and blocked checks separately. Never claim unexecuted tests passed, an absent staging environment exists, or a merge/deployment occurred without evidence.
