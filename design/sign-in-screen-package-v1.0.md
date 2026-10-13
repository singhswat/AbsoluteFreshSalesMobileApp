# Sign-In Screen Package v1.0 (referred to by Orchestrator as "FS-01 sign-in")
UIUXDesigner · 13 Oct 2026
Status: **DRAFT — prepared ahead of Architect's formal handoff contract**, per Orchestrator priority ask (t20u). Built from what's already locked (ADR-001 Decision 2, AF-SA-FRS-001 GAC 12, my own screen-map v1.0 §2.1). No visual/pixel mockup — still blocked on Stitch (no Playwright/browser clearance) and on the Stitch export zip (no unzip/Shell). This is the build-ready text/wireframe spec; swap in Architect's contract details once received without restructuring.

## 0. Naming flag (read first)
Orchestrator's ask labels this "FS-01." **Per ADR-002's branch table, FS-01 = Beat plan sync (Rep home), not Sign In.** Sign-in is cross-cutting auth (GAC 12), outside the FS-01..09 numbering, and sits logically in `00-project-scaffold` since every other branch depends on it. I'm delivering the Sign-In package as literally requested — just flagging so no one builds a `feature/fs01-sign-in` branch that collides with the already-planned `feature/fs01-beat-plan-sync`. Recommend this ships as part of scaffold, or its own `feature/00b-auth` branch, not folded into Beat.

## 1. User journey
```
App cold start
  → check local encrypted session (Keystore)
      ├─ valid cached session exists → skip Sign In → route by role (Rep→Beat, Driver→Deliveries), regardless of connectivity
      └─ no session / session cleared → Sign In screen
          → user enters phone/email + password → Sign In
              ├─ online + valid → success → route by role → store session
              ├─ online + invalid credentials → inline error, retry on same screen
              ├─ online + account inactive → blocking message, no retry path, contact admin
              ├─ online + pending approval → blocking message, no retry path
              ├─ offline (no network to reach Supabase) at first-ever login → blocking message:
                   first login requires connectivity once; cannot authenticate from nothing cached
              └─ (returning user, mid-shift) token rejected by server while online → forced re-login
                   prompt, NOT a full logout — local pending work is preserved, not cleared
```
Key rule from ADR-001 Decision 2, carried verbatim: **never force logout purely for being offline.** A valid cached session keeps working all day. Re-auth is only forced when online AND the server actively rejects the token.

## 2. Layout / spec per state

### 2.1 Sign In — default (first load / no cached session)
**Region layout (single column, mobile-first):**
- Top: app wordmark/logo (brand mark — see §4, asset TBD), small, centered.
- Language toggle: EN | HI, top-right corner, persists choice (BRD parity with web bilingual labels).
- Field 1: Phone or Email — single input, placeholder "Phone number or email", numeric/email keyboard auto-detect.
- Field 2: Password — masked, show/hide toggle icon.
- Primary button: "Sign In" — full width, disabled until both fields non-empty.
- Secondary link: "Forgot password?" — **open item, see §6.1: confirm this exists/works for Rep/Driver accounts (web parity assumption, not verified against web app directly).**
- Footer: small text, app version number (useful for support calls — standard, not BRD-sourced).
- No bottom nav bar on this screen (pre-auth).

### 2.2 Validating (button tapped, request in flight)
- Primary button → loading spinner inside the button (not a full-screen spinner), label replaced or dimmed. Fields disabled during the call. No skeleton needed — this is a short network call, not a content load.

### 2.3 Error — invalid credentials
- Inline, below password field: red/error-coloured text, icon + message. Wording: mirror whatever the web app uses today for this case (ADR-001 says "mirror web message" — **exact string not yet confirmed, placeholder: "Incorrect phone/email or password."**). Fields stay populated except password (password always clears on any failed attempt — standard practice, not destructive to anything persisted). No lockout logic specified anywhere — none implemented.

### 2.4 Error — account inactive
- Full-width message panel replacing the form (not just inline), since this is a dead end, not a retry: icon (not alarming — informational), message: **mirror web "account inactive" wording exactly (web string not yet sourced — flag to BizAnalyst/Architect, see §6.2)**. No "try again" button — only a "Contact admin" hint or support phone/email (BRD-sourced contact if one exists — **not yet confirmed, open item**).

### 2.5 Error — pending approval
- Same full-width dead-end pattern as 2.4. Message: **mirror web "pending approval" wording exactly (same sourcing gap as 2.4)**.

### 2.6 Offline at first-ever login (no cached session, no network)
- Full-width message, not a generic network-error toast — this is a distinct state because it's a hard blocker, not "will retry automatically": "You need an internet connection the first time you sign in. Please connect and try again." Retry button re-attempts the sign-in call (not a passive wait).

### 2.6b Online, but auth server unreachable/times out — **NEW, flagged by QAEngineer, not yet speced**
- Distinct from 2.6 (device has network, but the Supabase Auth call itself fails/times out — server down, DNS issue, etc.). Not yet given its own visual treatment here — placeholder pattern pending Architect's answer on expected API-contract behaviour (timeout length, retry-automatically vs. user-initiated): tentatively, same full-width dead-end-style panel as 2.6 but with distinct wording ("Couldn't reach the server. Check your connection and try again.") and a Retry button, not merged with "invalid credentials." **Do not build against this sub-section until Architect confirms** — see §6.6.

### 2.7 Success
- Brief, no separate "success" screen — button shows a check tick for <1s (optional, cosmetic) then navigates straight to role home (Beat for Rep, Deliveries for Driver). No "welcome" splash — this is a daily-use utility app, minimize ceremony.

### 2.8 Session-expired-while-online (mid-shift, not a cold start)
- Does **not** interrupt whatever screen the user is on destructively. Pattern: non-blocking banner or dialog — "Your session has expired. Please sign in again to continue syncing." — with a "Sign In" action that opens this same Sign-In screen as an overlay/modal, not a full app restart. **Any unsynced local work stays in the outbox untouched** — re-auth resumes sync, it does not clear queued work (ADR-001 explicit rule, GAC 12).
- After re-auth success here, return the user to the screen they were on, not to role home — this is a resume, not a fresh entry.

### 2.9 Offline / working-from-cache (NOT an error state)
- This isn't a Sign-In screen state at all — it's what happens *instead of* Sign In when a valid cached session exists offline: the user never sees Sign In, they land straight on role home with the standing "Working offline — N pending" banner (per screen-map §3). Noting it here only so it's not mistaken for a Sign-In error state by whoever builds this.

## 3. States summary table
| State | Trigger | Blocking? | Retry path |
| --- | --- | --- | --- |
| Default | cold start, no session | — | — |
| Validating | button tapped | yes (fields locked) | — |
| Invalid credentials | wrong phone/email or password | no | re-enter, resubmit |
| Account inactive | `is_active=false` | yes (dead end) | none — contact admin |
| Pending approval | staff request not yet approved | yes (dead end) | none — wait for approval |
| Offline, first login | no cached session + no network | yes | Retry button once connected |
| Online, server unreachable/timeout | network up, Supabase Auth call fails/times out | yes (tentative) | Retry button (pending Architect confirmation, §6.6) |
| Success | valid creds, online | — | routes away immediately |
| Session-expired (mid-shift) | online + server rejects token | no (non-destructive) | re-auth inline, resumes same screen |
| Offline-from-cache | valid cached session, any connectivity | n/a (never reaches this screen) | — |

## 4. Visual direction ("vibe")
No mobile-specific brand guideline exists yet — basing this on BRD Appendix I (web brand) and the nature of this app, flagged as a recommendation, not a locked decision:
- **Inherit brand recognition, not brand decoration:** deep green as primary (BRD Appendix I), red/gold as sparing accents only (e.g. error states, sync-failed badges) — not decorative. Admin web's red #CC0000 is an *admin* accent, not implied for this rep/driver app.
- **Utility-first, not marketing-first tone.** This is a daily tool for 10–15 staff standing in retail backrooms, often in bright daylight with a weak signal — high-contrast text, large tap targets (min 48dp), no illustration-heavy empty states, no onboarding carousel. Material 3 defaults (per ADR-002's Compose choice) are the right baseline — don't fight the platform for custom chrome on a Sign In screen nobody lingers on.
- **Bilingual by default, not an afterthought** — EN/HI toggle must be visible pre-auth, not buried in settings, since this is how the web app already works (BRD Appendix A1 parity).
- **No stock photography, no hero image on Sign In** — logo/wordmark only. This isn't a consumer acquisition screen.
- Open item: no actual logo/icon asset has been supplied to me — flagging to Swat/Architect (§6.3).

## 5. Reusable components (new, additive to screen-map §4)
- **Auth text field** (phone/email, auto keyboard-type detection) — reusable for any future phone-capture field.
- **Password field** with show/hide toggle.
- **Full-width blocking message panel** (icon + heading + body, no retry action) — reusable for inactive/pending-approval and similar dead-end states elsewhere in the app.
- **Inline field error** (icon + red text under a field) — reusable for all form validation across New Order, Add Outlet, etc. (already implied by screen-map, now given one canonical pattern here).
- **Session-expired re-auth modal** — overlay sign-in, distinct from the full-screen Sign In (does not discard current screen context).
- **Language toggle (EN/HI)** chip, top-corner placement — reusable in More/Settings (screen-map §2.16 already lists a language toggle there; same component).

## 6. Open items / assumptions flagged (not decided by me)
1. **Exact wording for "account inactive" and "pending approval" messages** — ADR-001 says "mirror web message" but I don't have the literal web copy. Need Architect or BizAnalyst to supply the exact strings from the existing web app so native matches, not paraphrases.
2. **"Forgot password?" — does this flow exist/apply for Rep/Driver accounts?** Assumed yes for web parity; not verified. If it requires email/SMS delivery, that needs its own screen/spec — not built here.
3. **No logo/icon/brand asset supplied yet** for this app specifically — vibe direction above is inferred from BRD Appendix I (web/admin brand), not confirmed as the mobile app's identity. Needs Swat/Architect sign-off before MobileDev treats §4 as final.
4. **No lockout/rate-limit UI specified** for repeated invalid-credential attempts — not in ADR-001 or AF-SA-FRS-001; assuming none exists for v1 unless told otherwise (10–15 known internal users, low abuse risk).
5. **Branch/ownership naming conflict** — see §0. Needs Architect/Orchestrator to confirm which branch this ships under before MobileDev starts.
6. **10th state — "online but auth server unreachable/timeout" (§2.6b)** — raised by QAEngineer's review, not originally in this spec. Needs Architect's answer on the API-contract behaviour (timeout length, auto-retry vs. user-initiated) before §2.6b's tentative wording/pattern is build-ready.

## 7. Review requested
@Architect — please confirm or correct: (a) the auth contract above matches what you intended by "FS-01 sign-in" even though it isn't literally FS-01 per your own branch table, (b) §6.1/6.2 wording sourcing, (c) §0 branch placement.
@QAEngineer — please review §2–3 for testability: each state above should map to one acceptance-criterion-sized test case; flag any state you can't write a test against from this spec alone.

## Dependencies (unchanged from screen-map v1.0)
Visual/pixel mockup for this screen is still blocked on Stitch zip extraction and Browser/Playwright clearance — this package is the complete text/wireframe spec MobileDev can build against now; styling (exact spacing/colour tokens) follows once Stitch assets exist.
