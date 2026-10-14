# Feature Task Ledger: FS-SUPABASE-SYNC (Supabase Network Sync & Outbox Integration)

- **Feature ID:** FS-SUPABASE-SYNC
- **Branch:** `feature/supabase-network-sync`
- **Base Revision:** `920a868` (`main`)
- **Owner:** dotnet-api-developer / solution-architect / flutter-developer / qa-engineer
- **Source Requirements:**
  - `BRD.md` §3 / §5 / §6.4 / §7 (Entities: `outlets`, `daily_beat_logs`, `orders`, `order_items`, `products`, `deliveries`, `holidays`)
  - `architecture/ADR-001-offline-strategy-and-auth.md` (Decision 1: Offline-first with auto-sync/push on reconnect; Decision 2: Supabase Kotlin SDK)
  - `architecture/ADR-002-stack-and-branch-workflow.md` (Stack: Kotlin, Compose, Room, WorkManager, Supabase Kotlin SDK)
  - `AGENTS.md` (Backend & Data Safety: Dev/staging isolation, synthetic test data, `client_id` idempotency side-column, `sync_version` optimistic concurrency)

## Objective
Implement the real Supabase Kotlin SDK integration, network data models, and background synchronization engine:
1. Configure Supabase Kotlin SDK 3.0.3 with Postgrest, Auth, Realtime, Ktor 3.x, and Kotlinx Serialization.
2. Provide safe `BuildConfig` environment plumbing (`SUPABASE_URL`, `SUPABASE_ANON_KEY`) with local fallback.
3. Define Kotlin Serializable DTOs corresponding to the PostgreSQL schema (`OutletDto`, `BeatVisitDto`, `ProductDto`, `FieldOrderDto`, `DeliveryDto`, `HolidayDto`).
4. Implement `SupabaseNetworkDataSource` with network fetch/push operations using client UUIDs for idempotency.
5. Upgrade `OutboxSyncWorker` to drain the Room outbox against `SupabaseNetworkDataSource` with error handling and retry exponential backoff.
6. Connect repositories (`RoomBeatRepository`, `RoomOutletRepository`, `RoomOrderRepository`) with refresh-from-network capabilities.
7. Verify all unit tests and offline replay behavior.

## Acceptance Criteria
- [x] AC1: Supabase Kotlin SDK 3.0.3 and Ktor 3.x integrated into `build.gradle.kts` and compilation succeeds cleanly.
- [x] AC2: `SupabaseClientProvider` creates client from configurable URL and Anon key via `BuildConfig` (reading from `local.properties`).
- [x] AC3: Network DTOs correctly serialize and deserialize PostgreSQL schemas (`OutletDto`, `BeatVisitDto`, `FieldOrderDto`, `FieldOrderItemDto`, `ProductDto`, `HolidayDto`).
- [x] AC4: `SupabaseNetworkDataSource` supports fetching beats, outlets, products, and pushing orders/outlets with `client_id` and strict `isReadOnlyMode = true` safety lock.
- [x] AC5: `OutboxSyncWorker` replays outbox mutations through network data source and updates local Room statuses upon sync.
- [x] AC6: Repositories seamlessly provide offline reads and outbox-backed writes with automatic push on network return.
- [x] AC7: Comprehensive unit tests pass across 18 test suites (101/101 tests pass, 0 failures, 0 errors).
- [x] AC8: Application class `AbsoluteFreshApp` and `AppContainer` injected into `MainActivity`, driving ViewModels with Room repositories and auto-refresh on sign in.
- [x] AC9: Debug APK assembled (`app-debug.apk`).

## Verification Evidence
- **Build Status:** `BUILD SUCCESSFUL in 1m 23s` (unit tests) and `BUILD SUCCESSFUL in 2m 39s` (`assembleDebug`).
- **Unit Test Results:**
  - `com.absolutefresh.sales.SupabaseDtoTest`: 5/5 tests passing (serialization, PostgreSQL column keys, JSON roundtrip).
  - `com.absolutefresh.sales.SyncRepositoryTest`: 2/2 tests passing (master data pull, Room upsert, local pending outlet retention).
  - `com.absolutefresh.sales.RoomRepositoryTest`: 3/3 tests passing.
  - `com.absolutefresh.sales.SignInViewModelTest`: 10/10 tests passing.
  - Total: **101/101 unit tests passing across 18 test suites**.
- **Live Supabase Environment Validation:**
  - Endpoint `https://fbfopgzblqmsflkfejwa.supabase.co` validated.
  - Tables `outlets`, `daily_beat_logs`, `products`, `orders`, `users` confirmed existing with Postgres RLS (`42501 permission denied` for unauthenticated requests, requiring Supabase Auth session).
  - Supabase Auth endpoint verified active (`/auth/v1/token?grant_type=password`).

## Status
- **Status:** Complete (Feature Branch Ready for Live Credential Testing)

