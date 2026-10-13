---
name: Absolute Fresh
colors:
  surface: '#fbf8ff'
  surface-dim: '#dad9e3'
  surface-bright: '#fbf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f2fd'
  surface-container: '#eeedf7'
  surface-container-high: '#e8e7f1'
  surface-container-highest: '#e3e1ec'
  on-surface: '#1a1b22'
  on-surface-variant: '#404940'
  inverse-surface: '#2f3038'
  inverse-on-surface: '#f1effa'
  outline: '#707a6f'
  outline-variant: '#bfc9bd'
  surface-tint: '#1f6c3a'
  primary: '#004c22'
  on-primary: '#ffffff'
  primary-container: '#166534'
  on-primary-container: '#93e0a2'
  inverse-primary: '#8bd79b'
  secondary: '#904d00'
  on-secondary: '#ffffff'
  secondary-container: '#fe932c'
  on-secondary-container: '#663500'
  tertiary: '#88000a'
  on-tertiary: '#ffffff'
  tertiary-container: '#b40011'
  on-tertiary-container: '#ffbfb8'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#a6f4b5'
  primary-fixed-dim: '#8bd79b'
  on-primary-fixed: '#00210b'
  on-primary-fixed-variant: '#005226'
  secondary-fixed: '#ffdcc3'
  secondary-fixed-dim: '#ffb77d'
  on-secondary-fixed: '#2f1500'
  on-secondary-fixed-variant: '#6e3900'
  tertiary-fixed: '#ffdad6'
  tertiary-fixed-dim: '#ffb4ab'
  on-tertiary-fixed: '#410002'
  on-tertiary-fixed-variant: '#93000b'
  background: '#fbf8ff'
  on-background: '#1a1b22'
  surface-variant: '#e3e1ec'
  admin-crimson: '#CC0000'
  amber-warning: '#F59E0B'
  surface-canvas: '#F8FAFC'
  surface-card: '#FFFFFF'
  border-subtle: '#E2E8F0'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '600'
    lineHeight: 38px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system serves an artisan South Indian culinary manufacturing and distribution ecosystem. Balancing natural, natural fermentation integrity with high-velocity FMCG operations, the visual character is grounded, dependable, and precision-driven.

The interface aesthetic leans into a modern corporate and operational utility style:
- **Clean Tactical Utility**: Uncluttered data presentation optimized for field sales personnel operating in bright sunlight, delivery drivers handling rapid dispatch handoffs, and central plant operators tracking perishable inventory.
- **Natural Reliability**: Anchored by deep culinary botanical greens that signal pure ingredients and traditional fermentation methods, reinforced by warm saffron accents that reference culinary craft.
- **Operational Precision**: Critical alerts, shelf-life counters (10-day decay window), and cutoff timers demand crisp, unmistakable visual cues without decorative excess.

## Colors

The color architecture is built to withstand high-stress operational environments and deliver unambiguous functional signaling across both web dashboards and field mobile screens.

### Palette Hierarchy
- **Primary (`#166534`)**: Deep herbal botanical green. Represents core brand trust, active production states, standard action triggers, and validated status tags.
- **Secondary (`#D97706`)**: Warm culinary saffron. Reserved for secondary interactions, medium-urgency warnings, and 7–8 day batch aging notices.
- **Tertiary (`#DC2626`)**: Tactical crimson alert. Dictates hard deadlines, order cutoff countdowns (23:55), and safety limits.
- **Neutral (`#71717A`)**: Balanced slate-gray base yielding crisp structural boundaries, dividers, and neutral body labels.

### Specific Functional & Named Colors
- **`admin-crimson` (`#CC0000`)**: Dedicated operational override tone for administrative panels, active invoice editing flags, and critical discrepancies.
- **`amber-warning` (`#F59E0B`)**: High-visibility status badge indicator signaling shelf-life decay thresholds on day 7 and 8 batches.
- **Surface Foundations**: Light-mode canvas set at `#F8FAFC` to reduce glare, with pristine `#FFFFFF` surfaces for data cards, invoices, and tabular registers.

## Typography

Inter serves as the singular typography family across display, body, and tabular instances. Its neutral architecture and open apertures guarantee immediate legibility across varied ambient environments—from harsh outdoor sales field use to high-density desktop invoice workflows.

- **Tabular Numerics**: All numeric outputs—such as batch codes, countdown timers, price totals, and inventory weights—must use tabular figures (`font-variant-numeric: tabular-nums`) to preserve optical alignment across data tables.
- **Scale Hierarchy**: Headlines maintain compact line heights and slight negative tracking at large display sizes to preserve screen economy on mobile devices.
- **Micro-Labels**: Specialized `label-sm` sizing is deployed for batch expiration flags, legal invoice notes, and metric tracking tags.

## Layout & Spacing

The layout model adapts across distinct hardware and operational use cases:

- **Mobile Viewports (Field Sales & Logistics)**: Fluid single-column system designed for one-handed operation. Elements span 100% of the viewport width minus outer safe margins (`1rem`). Interactive touch targets conform to a minimum height of 48px to support fast route logging.
- **Desktop Dashboards (Plant Management & Accounts)**: 12-column grid system featuring a `1.5rem` gutter and `2rem` screen margins. Supports high data density with multi-column tabular records, split-pane ledger reviews, and modal forms.
- **Invoice & Export Canvas**: Conforms strictly to fixed proportional ratios (standard print A4 specs) within desktop document previewers, enforcing consistent margins and column alignments between digital workflows and thermal/laser physical outputs.

## Elevation & Depth

Visual hierarchy relies on structural containment and crisp boundaries rather than heavy drop shadows:

- **Surface Tiers**: Canvas background sits at `#F8FAFC`, while actionable cards, modular tiles, and table rows rest on pristine `#FFFFFF` surfaces.
- **Low-Contrast Outlines**: Cards, list items, and summary widgets use explicit 1px borders (`#E2E8F0`) to establish definite structural perimeters.
- **Restrained Elevation**: Floating action bars, active mobile sheets, and dialog overlays employ a subtle, high-diffusion ambient shadow: `0 4px 12px rgba(15, 23, 42, 0.08)`. Heavy or colored drop shadows are avoided to maintain data clarity.
- **Modal Depth**: System modals utilize a 40% opacity neutral dark scrim (`rgba(15, 23, 42, 0.40)`) to focus attention on critical operational actions without obscuring context entirely.

## Shapes

The interface implements a Soft geometric treatment (`roundedness: 1`), utilizing a base border-radius of `0.25rem` (4px).

- **Standard Elements (`0.25rem` / 4px)**: Buttons, text inputs, table wrappers, check targets, and standard inline badges.
- **Container Elements (`rounded-lg` - `0.5rem` / 8px)**: Summary cards, order panels, and operational workflow modals.
- **Overlays (`rounded-xl` - `0.75rem` / 12px)**: Mobile bottom action drawers and dispatch confirmation bottom-sheets.
- **Geometric Strictness**: Avoid full pills and circular shapes for operational components; concise corners ensure data tables and dense metric cards stack efficiently without wasting visual space.

## Components

### Buttons
- **Primary**: Solid botanical green background (`#166534`), white typography, 4px border radius. Minimum height 48px on mobile for field accessibility; 40px in desktop admin toolbars. Hover transitions darken by 10%.
- **Secondary / Operational**: Saffron background (`#D97706`) or outline-green (`#166534` 1px border over transparent surface) for secondary batch exports or re-order triggers.
- **Destructive / Admin Overrides**: Solid `#CC0000` or crimson border for batch discards, voiding invoices, or emergency stock locks.

### Metric Badges & Freshness Chips
- **Standard**: Soft neutral gray surface (`#F1F5F9`) with slate label (`#475569`).
- **Fresh (Day 1–6)**: Soft green background (`#DCFCE7`), text `#166534`, dot indicator.
- **Aging Alert (Day 7–8)**: Amber warning background (`#FEF3C7`), text `#B45309`, displaying shelf countdown timer.
- **Critical Expiry (Day 9+)**: Crimson background (`#FEE2E2`), text `#B91C1C`, bold pulsing badge.

### Input Fields & Selectors
- Flat `#FFFFFF` surface with a 1px border (`#CBD5E1`) and 4px radius. Focus state shifts to `#166534` with a 1px ring. 
- Integrated numeric incrementers and clear tap targets for field reps recording pouch unit counts.

### Data Tables (Desktop Admin)
- Fixed compact cell padding (10px vertical, 16px horizontal). Header rows use `#F8FAFC` background with uppercase `label-sm` text. 
- Alternating subtle row striping or 1px bottom border (`#F1F5F9`). Monospaced alignment for batch codes, inventory volumes, and price figures.

### Mobile Field Cards
- White surface bounded by a 1px `#E2E8F0` border, featuring a 2-column key-value ledger (e.g., Shop Name, Pouch SKU, Payment Status).
- Direct call-to-action footers spanning the full width of the card base for immediate route check-ins and invoice generation.