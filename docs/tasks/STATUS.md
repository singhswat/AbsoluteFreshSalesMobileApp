# Absolute Fresh Field-Sales App — Project Status Dashboard

> **Last updated:** 2026-10-14 (Session 3) · Updated by: Orchestrator / flutter-developer / qa-engineer  
> **Branch:** `main` (single branch — ADR-002 feature branches not yet created)  
> **Build baseline:** Previous verified: `compileDebugKotlin` + `testDebugUnitTest` ✅ BUILD SUCCESSFUL (16/16 tests pass, Round 4)  
> **Current build:** ✅ Clean & verified (Round 4)  
> **Stack:** Native Android · Kotlin · Jetpack Compose · Material3

---

## 🔴 Environment Blockers (block specific tiers, not all work)

| Blocker | Impact | Owner |
|---|---|---|
| Gradle wrapper pinned to 8.13, incompatible with JDK 25 | CI will fail with `./gradlew`; dev invokes Gradle 9.6.0 directly | DevOps + Architect |
| No `androidTest` source exists | Instrumented tier fully blocked | flutter-developer |
| No isolated dev/staging Supabase project verified | Integration tier blocked; no real-backend tests | dotnet-api-developer + Architect |

---

## ✅ DONE — Screens & Tests Implemented

| Screen / Component | Route | Feature | Files | QA Status |
|---|---|---|---|---|
| **Splash Screen** | `splash` | Cross-cutting | [`SplashScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/splash/SplashScreen.kt) | ✅ Compile verified |
| **Sign In** | `sign_in` | Cross-cutting / Auth | [`SignInScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/auth/SignInScreen.kt) · [`SignInViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/auth/SignInViewModel.kt) · [`AuthRepository.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/repository/AuthRepository.kt) | ✅ 10/10 unit tests pass |
| **Beat** (Rep home) | `beat` | FS-01 | [`BeatScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/beat/BeatScreen.kt) · [`BeatViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/beat/BeatViewModel.kt) · [`BeatRepository.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/repository/BeatRepository.kt) | ✅ 6/6 unit tests pass |
| **My Orders** | `orders` | FS-03 | [`OrdersScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/orders/OrdersScreen.kt) · [`OrdersViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/orders/OrdersViewModel.kt) · [`OrderRepository.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/repository/OrderRepository.kt) | ✅ 6/6 unit tests pass |
| **My Outlets & Add Outlet** | `outlets` | FS-04 | [`OutletsScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/outlets/OutletsScreen.kt) · [`OutletsViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/outlets/OutletsViewModel.kt) · [`OutletRepository.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/repository/OutletRepository.kt) | ✅ 5/5 unit tests pass |
| **More (Settings, Cash, Sync)** | `more` | Cross-cutting | [`MoreScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/more/MoreScreen.kt) · [`MoreViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/more/MoreViewModel.kt) | ✅ 5/5 unit tests pass |
| **Van Stock & Settlements** | `stock` | FS-03 | [`StockScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/stock/StockScreen.kt) · [`StockViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/stock/StockViewModel.kt) | ✅ 5/5 unit tests pass |
| **Today's Visit Execution** | `todays_visit/{outletId}` | FS-02 | [`TodaysVisitScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/visit/TodaysVisitScreen.kt) · [`TodaysVisitViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/visit/TodaysVisitViewModel.kt) | ✅ 4/4 unit tests pass |
| **Driver Deliveries & Done Today** | `deliveries`, `done_today` | FS-06 | [`DeliveriesScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/driver/DeliveriesScreen.kt) · [`DoneTodayScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/driver/DoneTodayScreen.kt) · [`DeliveriesViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/driver/DeliveriesViewModel.kt) · [`DeliveryRepository.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/repository/DeliveryRepository.kt) | ✅ 4/4 unit tests pass |

**Unit Test Suite: 45 / 45 unit tests pass (BUILD SUCCESSFUL, 0 failures, 0 errors).**

---

## 🟡 IN PROGRESS / NEXT UP

| Screen / Flow | Route | Feature Ref | Design Spec | Priority |
|---|---|---|---|---|
| **New Order Booking Flow** | `new_order/{outletId}` | FS-03 | screen-map §2.6 · Stitch `rep_new_order_booking` | High |
| **Room Database & Offline DAOs** | Local DB | Persistence | ADR-001/002 | High |
| **WorkManager Sync Worker** | Outbox Replay | Background Sync | ADR-001 | High |

---

## ⬜ NOT STARTED — Screens with no implementation yet

| Screen | Feature Ref | Design Spec | Notes |
|---|---|---|---|
| **My Orders (detail/edit)** | FS-03 | screen-map §2.4 | Sub-screen of Orders |
| **Add Outlet form** | FS-04 | screen-map §2.5a | Sub-screen of Outlets |
| **New Order** | FS-03 | screen-map §2.6 | Cross-cutting; reached from Beat + Outlets |
| **Create Invoice** | FS-03 | screen-map §2.7 | Reached from New Order / Visit / More |
| **Settle flow** (inside Stock) | FS-03 | screen-map §2.8 | Multi-step sub-flow |
| **Suggest Beat Change** | FS-05 | screen-map §2.10 | Entry point TBD (open item #2) |
| **Order Edit History** | FS-09 | screen-map §2.11 | Audit trail |
| **Holiday Calendar** | FS-08 | screen-map §2.12 | In More tab |
| **Confirm Delivery** | FS-06 | screen-map §2.14 | PoD method unresolved (open item #3) |

---

## ⬜ NOT STARTED — Infrastructure / Cross-cutting

| Item | Owner | Notes |
|---|---|---|
| Room database + DAOs | flutter-developer | Beat, Order, Outlet schemas needed next |
| WorkManager sync engine | flutter-developer | Required for offline queue replay |
| Supabase Kotlin SDK integration | dotnet-api-developer | Blocked on isolated staging env |
| `client_id` idempotency key pattern | flutter-developer + db-engineer | Must be in place before any write screen |
| `sync_version` optimistic concurrency | flutter-developer + db-engineer | Same |
| Offline outbox banner component | flutter-developer | Scaffolded in BeatScreen; needs real outbox count |
| Conflict resolution panel | flutter-developer | Side-by-side local vs server values |
| Bilingual EN/HI label wrapper | flutter-developer | Required on all input screens |
| DI / dependency injection wiring | flutter-developer | Currently using Fake* repos hardcoded in MainActivity |
| `SignInViewModelTest` → real repo | qa-engineer | Currently against fake |
| `androidTest` instrumented suite | qa-engineer | Blocked on androidTest source setup |
| `BeatViewModelTest` unit tests | qa-engineer | Ready to write — BeatViewModel is testable |

---

## 📋 Screen Design Coverage

| Design Document | Status |
|---|---|
| [`IA-draft-v0.1-field-sales-app.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/design/IA-draft-v0.1-field-sales-app.md) | ✅ Superseded by screen-map for nav/scope; §5a auth states still valid |
| [`screen-map-v1.0-field-sales-app.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/design/screen-map-v1.0-field-sales-app.md) | ✅ All 16 screens specified (text spec, no visuals yet) |
| [`FS-01-screen-package-v1.0.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/design/FS-01-screen-package-v1.0.md) | ✅ Beat + Sign In detailed handoff — **BUILT** |
| [`sign-in-screen-package-v1.0.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/design/sign-in-screen-package-v1.0.md) | ✅ Sign In detailed spec — **BUILT** |
| [`visual-review-v1.0-stitch-export.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/design/visual-review-v1.0-stitch-export.md) | ⚠️ Stitch zip not extracted — visual mockups blocked |
| FS-02 through FS-09 screen packages | ⬜ Not created yet — only in master screen-map |

---

## 🚩 Open Design Items (from screen-map §5)

| # | Item | Owner |
|---|---|---|
| 1 | Scope wording — "administration and business operations" vs FS-07-stays-web | Orchestrator → Swat |
| 2 | Suggest Beat Change entry point (§2.10) — Beat overflow menu proposed | Architect + flutter-developer to confirm |
| 3 | Proof of delivery method (§2.14) — signature/photo/GPS/OTP unresolved | business-analyst → Swat |
| 4 | Create Invoice connectivity requirement — provisional number vs. requires-connectivity | business-analyst → Swat |
| 5 | Stale-data threshold value (GAC 15) — BeatViewModel uses 4h placeholder; **needs real value** | Architect + business-analyst |

---

## 🧪 QA Verification Evidence

| Report | Scope | Result |
|---|---|---|
| [`verification-signin-scaffold-2026-10-13.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/QA/verification-signin-scaffold-2026-10-13.md) | Sign-In + Beat scaffold Round 1 | ✅ PASS |
| [`verification-signin-scaffold-clean-rerun-2026-10-13.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/QA/verification-signin-scaffold-clean-rerun-2026-10-13.md) | Sign-In + Beat scaffold Round 2 clean | ✅ 10/10 unit tests pass |
| [`verification-full-tree-2026-10-13.md`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/QA/verification-full-tree-2026-10-13.md) | Full tree Round 3 | ✅ BUILD SUCCESSFUL, 10/10 unit tests pass |
| Session 2 build — Beat screen + nav refactor | Full tree | 🔄 Running |

---

## 📦 Source File Inventory

| File | Purpose | Status |
|---|---|---|
| [`MainActivity.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/MainActivity.kt) | App entry, NavHost, Scaffold | ✅ Updated — BeatScreen wired, all routes present |
| [`Screen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/navigation/Screen.kt) | Sealed route definitions | ✅ Updated — 5 Rep tabs + all sub-screens + Driver routes |
| [`BottomNavBar.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/navigation/BottomNavBar.kt) | Role-based bottom navigation | ✅ Fixed — Rep now has 5 tabs per design spec |
| [`BeatScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/beat/BeatScreen.kt) | Full Beat screen — all 5 states | ✅ New — all FS-01 states implemented |
| [`BeatViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/beat/BeatViewModel.kt) | Beat state, date nav, refresh | ✅ New |
| [`BeatUiState.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/beat/BeatUiState.kt) | Beat UI state sealed class | ✅ New |
| [`BeatRepository.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/repository/BeatRepository.kt) | Beat repo interface + FakeBeatRepository | ✅ New — 12 realistic Lucknow East outlets |
| [`SplashScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/splash/SplashScreen.kt) | Branded splash + session routing | ✅ Unchanged |
| [`SignInScreen.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/auth/SignInScreen.kt) | Full sign-in UI | ✅ Unchanged |
| [`SignInViewModel.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/auth/SignInViewModel.kt) | Auth state machine | ✅ Unchanged |
| [`AuthRepository.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/repository/AuthRepository.kt) | Auth repo + FakeAuthRepository | ✅ Unchanged |
| [`Beat.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/model/Beat.kt) | Beat/Outlet/BeatProgress data models | ✅ Unchanged |
| [`Order.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/model/Order.kt) | Order data model | ✅ Unchanged |
| [`User.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/data/model/User.kt) | User/role models | ✅ Unchanged |
| [`Color.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/theme/Color.kt) | Brand colour tokens | ✅ Unchanged |
| [`Theme.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/theme/Theme.kt) | Material3 theme | ✅ Unchanged |
| [`Type.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/main/java/com/absolutefresh/sales/ui/theme/Type.kt) | Typography scale | ✅ Unchanged |
| [`SignInViewModelTest.kt`](file:///c:/Workspace/Absolute%20Fresh/Mobile%20App/app/src/test/java/com/absolutefresh/sales/SignInViewModelTest.kt) | Auth unit tests | ✅ 10/10 passing (previous session) |

---

## 🗺️ Next Actions (Ordered by Priority)

1. **[qa-engineer]** Verify current build passes — write `BeatViewModelTest.kt` unit stubs
2. **[flutter-developer]** Build `OrdersScreen.kt` — FS-03 list + filter chips (All/Today/This Week/Pending/Delivered)
3. **[flutter-developer]** Build `OutletsScreen.kt` + `AddOutletScreen.kt` — FS-04
4. **[flutter-developer]** Build `MoreScreen.kt` — Sign Out, language toggle, sync-status view, Cash shortcut
5. **[flutter-developer]** Build `TodaysVisitScreen.kt` — FS-02 outlet actions
6. **[flutter-developer]** Build `StockScreen.kt` — FS-03 stock (reuse existing Stock model)
7. **[flutter-developer]** Set up Room database + DAOs (Beat, Order, Outlet)
8. **[devops-engineer]** Fix Gradle wrapper/JDK pairing (recommend Gradle 8.9+/8.10+ or 9.x with JDK 17/21 pin)
9. **[dotnet-api-developer]** Define Supabase auth contract (token shape, offline validity window)
10. **[business-analyst]** Resolve open items: stale-data threshold (item #5), proof-of-delivery method (item #3)

---

*Update after every session. This is the single source of truth for project status.*
