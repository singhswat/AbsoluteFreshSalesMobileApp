# FS-01 Screen Package — Beat (Rep Home) v1.0
UIUXDesigner · 13 Oct 2026
Status: **Text spec, ready for MobileDev build.** Visual mockup not included — blocked on Stitch access (see `screen-map-v1.0-field-sales-app.md` Dependencies). Pulled out of the full screen map as the first requirement handoff per Swat's t19u / Orchestrator's assignment.

## Scope
FS-01 = beat planning/assignment, as delivered by BizAnalyst (AF-SA-FRS-001 v1.0). Covers the two screens a rep touches before FS-01 is "done": **Sign In** (prerequisite, cross-cutting) and **Beat** (the FS-01 home screen itself). Both detailed below in full; this supersedes the terser §2.1/§2.2 entries in the master screen map for build purposes — master map still holds for everything downstream (FS-02 onward).

## Screen A: Sign In
**Purpose:** authenticate, route by role (Sales Rep / Delivery Driver). Entry point, no screen before it.

**Layout elements:**
- App logo/header
- Phone/email field + password field, both with inline validation
- Bilingual EN/HI label toggle (visible pre-auth, not buried in settings)
- Sign In button (primary, full-width)
- Forgot-password link — **open item: confirm this exists on web before building** (not in AF-SA-FRS-001 explicitly)

**States:**
| State | Trigger | UI behaviour |
|---|---|---|
| Empty (first load) | App cold start | Fields blank, Sign In disabled until both filled |
| Validating | Submit pressed | Spinner on button, fields locked |
| Invalid credentials | Server rejects | Inline error under password field, fields re-enabled |
| Account inactive | Server flags inactive | Full-screen blocking message, no retry, no dismiss — contact admin |
| Pending approval | Server flags pending | Full-screen blocking message, no retry |
| Offline, no cached session | No network, first-ever login | "No internet connection — sign in requires connectivity the first time" (cannot cache credentials with no prior session) |
| Offline, cached session | Previously signed in, token valid locally | Auto-route to Home without re-prompting (GAC 12) |
| Success | Valid creds | Route to Beat (Rep) or Deliveries (Driver) by role |

**Nav:** none in (entry point). Out → Beat (Rep) / Deliveries (Driver).

**Dependency on Architect:** auth contract (Supabase Auth session shape, token refresh/offline-validity rule, role field) needed before MobileDev wires the real request — screen layout/states above don't change once that lands, only the plumbing does.

## Screen B: Beat (Rep Home) — FS-01
**Purpose:** show today's assigned visit sequence, cached for offline use, the rep's daily starting point.

**Layout elements:**
- Date navigator: prev / today / next arrows, current date prominent
- Pull-to-refresh gesture
- Visit list, each row showing: sequence number, outlet name, area, target (visit purpose/quota if applicable), product focus, kg target, invoice status badge, visit state (Planned / Active / Completed)
- "Last refreshed: [timestamp]" persistent label (GAC 15)
- Working-offline banner (cross-cutting component, shown app-wide when outbox non-empty — not Beat-specific, see master map §3)

**States:**
| State | Trigger | UI behaviour |
|---|---|---|
| Loading | Screen open, no cache yet | Skeleton list (5-6 placeholder rows) |
| Empty | No beat assigned for selected date | Centered message: "No beat assigned for this date" + illustration slot |
| Populated | Beat data present (cached or fresh) | Full list as above |
| Stale-data warning | Cache older than threshold | Banner above list: "Beat data may be out of date — refresh when online" **(threshold value = open item #5 below, placeholder only)** |
| Revoked/reassigned with local activity | Server reassigns beat after rep already acted locally | Affected row flagged distinctly (icon + "Reassigned — review" label), per FS-01 AC5; row remains tappable so rep can see what they did before reassignment |

**Nav:** tap outlet row → Today's Visit / Outlet actions (next screen in the FS-02 flow, outside this package). Bottom tab bar persistent (Beat · Orders · Outlets · Stock · More).

**Reusable components used here (see master map §4):** sync-status badge, offline outbox banner, empty-state pattern, bilingual label wrapper, date-range/filter-style navigator.

## Acceptance criteria (for MobileDev + QA, mapped to FS-01 ACs in AF-SA-FRS-001)
1. Beat list renders fully from local cache with zero network calls once previously synced (offline-first, GAC 1).
2. Date nav and pull-to-refresh both work offline against cache; refresh only triggers a real fetch when online.
3. Visit states (Planned/Active/Completed) persist correctly across app restart.
4. Stale-data banner appears/disappears correctly against the threshold once Architect/BizAnalyst confirm its value (currently unimplementable precisely — placeholder logic only).
5. Reassigned-beat-with-local-activity row flag renders per FS-01 AC5 and never silently drops the rep's prior local action.
6. Sign In routes correctly by role and handles all 7 states in the table above, including the offline-cached-session auto-route (GAC 12).

## Open items specific to this package (carried from master map, restated here for the handoff)
1. **Stale-data threshold (GAC 15)** — no number yet. @Architect/@BizAnalyst — need this before item 4 above can be built/tested precisely, not just placeholder text.
2. **Forgot-password link** — confirm it exists on web parity before I finalize whether it's in scope for v1 Sign In.
3. **Auth/session contract** — @Architect, this package is ready for your API/auth contract; screen layout/states won't change once it lands, just the data-binding specifics (token shape, offline-validity window).

## Status
Ready to hand to **MobileDev** for scaffold/build now (text spec complete). Ready for **QAEngineer** to turn into concrete Unit-test stubs once Architect's auth contract lands (QA already has FS-01's GAC criteria from the test plan). Visual mockup to follow once Stitch access is resolved — not a blocker for starting the build.
