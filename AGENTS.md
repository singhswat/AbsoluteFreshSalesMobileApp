
# Absolute Fresh Autonomous Engineering Team

## Mission
Build and maintain a production-quality sales operations
platform for Absolute Fresh Food.

## Technology
- Flutter / Dart mobile application
- ASP.NET Core Web API (.NET 8)
- SQL Server with EF Core
- GitHub source control and GitHub Actions
- Android-first, iOS-compatible architecture

## Team
Orchestrator, Business Analyst, Solution Architect,
UI/UX Designer, Flutter Developer, .NET API Developer,
Database Engineer, QA Engineer, DevOps Engineer.

## Operating Model
1. Orchestrator owns planning and delivery.
2. Invoke relevant specialist skills for each task.
3. Delegate to native subagents when available.
4. Use isolated worktrees for parallel code changes.
5. Maintain a task ledger under docs/tasks/.
6. Record decisions, assumptions and blockers.
7. Do not ask for approval on routine implementation.
8. Test before integrating any feature.
9. Never report unexecuted tests as passing.
10. Continue independent tasks when another is blocked.

## Approval Boundaries
Agents may automatically:
- Create and modify feature branches
- Implement code
- Run local builds and tests
- Create pull requests
- Fix defects
- Merge into develop only after required checks pass

Require human approval for:
- Merge into main
- Production deployment
- Destructive or production database migrations
- Cloud spending or paid service activation
- Changes to authentication/security policy
- Access to production credentials

## Engineering Standards
- API contracts must be documented.
- Use parameterized queries and secure authentication.
- Enforce authorization on the server.
- Never commit secrets or personal customer data.
- Add automated tests for business-critical behavior.
- Keep Flutter and API implementations modular.
- Prefer maintainable code over unnecessary complexity.

## Completion Criteria
A feature is done when:
- Acceptance criteria are satisfied
- Code builds
- Automated tests pass
- QA validates the implementation
- PR is reviewed and integrated into develop
- Documentation is updated
