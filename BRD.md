# Absolute Fresh – Business Requirements Document (BRD)

Oct 10, 2026 · @Swat

This BRD defines the business needs, users, functional scope, rules and gaps of the complete Absolute Fresh application suite (consumer website, partner portal, admin portal, field-sales app and database), reverse-engineered from the live code (GitHub main) and the production Supabase schema as of 10 Oct 2026. Appendices D to K specify the data, rules, calculations, flows, configuration and acceptance tests in business terms, so the application can be rebuilt from this document without the code.

## 1. Document control and purpose

This document is the single business reference for what the Absolute Fresh application must do, who it serves, and where today's build falls short.

| Item | Detail |
| --- | --- |
| Document | Business Requirements Document, version 1.0 (baseline of the live system) |
| Owner | Swatantra Singh, proprietor, Absolute Fresh |
| Basis | GitHub repo singhswat/AbsoluteFreshFoodWebsite (branch main), Supabase project fbfopgzblqmsflkfejwa (production), and the project specs on operational traceability and chain/company invoicing |
| Status key used below | Built = live in code; Partial = some of it built; Gap = specified or needed but not built |
| Priority key | Must / Should / Could (MoSCoW) |

The requirements describe the business need first. Where the code already meets it, the status says so; where it does not, it becomes a roadmap item in section 10.

## 2. Business background and objectives

Absolute Fresh sells fresh, preservative-free fermented South Indian batter (idli and dosa) with chutney, made in Lucknow, Uttar Pradesh. Shelf life is 10 days, so the business depends on fast, traceable movement from kitchen to shelf.

It sells through two routes: direct to consumers (D2C) through the website, and business-to-business (B2B) through retail shops, chains and, later, distributors and quick-commerce dark stores.

**Business objectives**

1. Take D2C orders online with prepaid or COD payment, limited to serviceable pincodes.
2. Grow D2C through influencer partners with coupons, tracked orders and automatic commission payouts.
3. Run B2B field sales: planned beats, order capture, delivery confirmation, cash and dues collection.
4. Control freshness and waste: every packet traceable from production batch to store, with expiry tracking.
5. Issue GST-compliant, sequentially numbered invoices and know who owes what.
6. Give the owner one admin view of sales, stock, receivables, rep performance and marketing return.
7. Scale to chains (e.g. Family Bazaar), quick-commerce dark stores and distributors on one shared Company, Outlet, Order, Invoice model.

**Current scale (production data, 10 Oct 2026)**

| Measure | Value |
| --- | --- |
| Staff users | 4 (1 admin, 3 reps) |
| Outlets | 37 (14 chain stores, 23 general trade), all on price tier 4 |
| Companies | 24 |
| Website orders | 26 (14 delivered, 5 new, 3 cancelled) |
| B2B invoices | 17, all still in status sent |
| Products | 4 (200 g, 500 g, 1 kg batter; 30 g podi chutney) |
| Production batches logged | 10; allocations 7 |

## 3. Scope

| In scope | Out of scope for now |
| --- | --- |
| Public website, product pages, blog, online ordering with Razorpay or COD | Native Android app (planned, separate project) |
| Influencer/partner portal | Customer login and customer portal |
| Admin portal (D2C orders, finance, marketing, stock, performance) | Marketplace listings (Amazon, Swiggy, Blinkit are not live) |
| Field sales app for reps and drivers | Multi-state GST or multi-entity billing (Uttar Pradesh only) |
| Stock, batch traceability, shop dues, cash register | Automated PO ingestion, e-way bill, bank statement reconciliation |
| B2B invoicing with GST and company/outlet billing model | Promotion engine and dynamic rate contracts |
| WhatsApp alerts to owner; milkman delivery programme (to be built, Appendix K) | ERP integration |

**Deferred in the existing specs:** consolidated multi-outlet invoices, expanded invoice workflow beyond sent / partial / paid, and modelling serviced locations as outlets.

## 4. Stakeholders and user roles

| Role | Who | What they need | Where they work |
| --- | --- | --- | --- |
| Admin / owner | Swatantra Singh; Reena Singh is authorised signatory | Full control: approvals, finance, stock, analytics, team | sales/marketing/admin.html plus admin pages in the sales app |
| Sales rep | Field staff (3 today) | Today's beat, place orders, add outlets, raise invoices, collect cash, log stock | sales/salesrep/ (beat, rep-orders, rep-outlets, stock, cash) |
| Delivery driver | Delivery staff | Pending deliveries, confirm delivered kg and cash | sales/salesrep/pages/deliveries.html |
| Influencer / partner | Content creators | Sign up, create coupons, request samples, submit content, see commission | sales/marketing/dashboard.html |
| D2C customer | Households in serviceable areas | Browse, order, pay, receive on a chosen day | absolutefreshfood.com |
| Retail shop / chain / distributor | 37 outlets | Receive fresh stock, be billed correctly, return expired stock | Not a direct app user today |
| Operations / production | Kitchen operator | Log batches, wastage, allocate stock | Stock dashboard (stock-admin) |

Access is enforced by role at login (admin, rep, driver, influencer; milkmen will be added, Appendix K). Staff (rep, driver) apply through signup and are approved by the admin; influencers are approved automatically at signup.

## 5. Application landscape

The suite is five front ends and one shared Supabase backend. All pages are static HTML and vanilla JavaScript, with no build step, hosted on Hostinger with Git auto-deploy: every push to main goes live immediately.

| Application | Address | Users | Core job |
| --- | --- | --- | --- |
| Brand and order website | absolutefreshfood.com | Customers | Brand, products, 33 blog articles, ordering and payment |
| Unified login and signup | sales.absolutefreshfood.com | Staff and influencers | One sign-in routed by role; staff and partner applications |
| Admin portal | sales.../marketing/admin.html | Admin | D2C orders, coupons, influencers, campaigns, invoices, payouts, stock, performance |
| Partner dashboard | sales.../marketing/dashboard.html | Influencers | Coupons, links, samples, content, earnings |
| Field sales app | sales.../salesrep/ | Reps, drivers, admin | Beats, orders, deliveries, stock, cash, ledger, analytics, team |
| Dispatch worklist | sales.../dispatch.html | Admin, drivers | D2C web orders by serviceable area |

**Backend services (Supabase)**

| Service | Purpose |
| --- | --- |
| Postgres database | 50+ tables and about 20 reporting views; one project serves every app and holds live data despite being named "AbsoluteFresh Dev" |
| Edge function create-order | Creates the website order and the Razorpay order |
| Edge function verify-payment, razorpay-webhook | Confirms and records online payment |
| Edge function create-user, admin-staff-account | Admin-only creation and management of staff accounts |
| Edge function rapid-worker | Deployed; purpose not documented in the repo |
| whatsapp-alert (in repo) | Owner alerts for new order, overdue delivery, late beat, short delivery; not in the deployed function list |

## 6. Functional requirements

### 6.1 Consumer website and online ordering (WEB)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| WEB-01 | Present brand story, product pages (200 g, 500 g, 1 kg batter; podi chutney) and a content blog about fermentation and nutrition to drive organic and ad traffic | Must | Built |
| WEB-02 | Let a customer build a cart, enter name, phone, address and 6-digit pincode, and order | Must | Built |
| WEB-03 | Accept an order only for pincodes listed as active in service\_areas (8 areas today) and show the matching area name | Must | Built |
| WEB-04 | Take payment by Razorpay (UPI, card) or COD; confirm payment server-side before marking the order paid | Must | Built |
| WEB-05 | Apply a coupon code at checkout: percent or flat value, minimum order, usage limits (total and per customer), expiry, first-order-only, welcome and sample coupons | Must | Built |
| WEB-06 | Store order, items, discount, delivery date, payment method, Razorpay ids and status (new, confirmed, out for delivery, delivered, cancelled) | Must | Built |
| WEB-07 | Capture leads (phone, coupon, source) from landing pages | Should | Built |
| WEB-08 | Publish privacy and contact pages; keep each new blog post linked from the blog index and sitemap | Should | Built |
| WEB-09 | Customer account, order history, repeat order | Could | Gap |

### 6.2 Influencer and partner programme (INF)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| INF-01 | Influencers sign up with name, phone, city, email, password, Instagram handle, follower count, content type and a reason, and must accept the 6-month exclusivity terms (no competing fresh-food brands); the account is approved automatically at signup; bank/UPI and PAN are added later in the dashboard | Must | Built |
| INF-02 | Approved influencer creates coupons within an admin-set discount ceiling (default 25%, adjustable 5% to 50% in 5% steps) | Must | Built |
| INF-03 | Share link and QR per coupon; log clicks and conversions | Should | Built |
| INF-04 | Request product samples; admin approves or rejects; sample coupon and order are linked | Should | Built |
| INF-05 | Submit content (reel, post) for review; admin approves with feedback; record live URL and engagement | Should | Built |
| INF-06 | Commission by delivered orders per month: Bronze under 11 orders pays Rs 30, Silver 11 to 30 pays Rs 50, Gold 31 or more pays Rs 75 per order | Must | Built |
| INF-07 | Deduct TDS at 10% when PAN is provided and annual payout exceeds Rs 30,000; admin marks payout paid with method and reference | Must | Built |
| INF-08 | Track COD rejection count and rate per influencer | Should | Partial (fields exist) |
| INF-09 | Campaigns with budget, spend and revenue; ROI view | Should | Built |
| INF-10 | Password reset by email token | Must | Built |

### 6.3 Admin portal: D2C and marketing operations (ADM)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| ADM-01 | Home view of today's orders, pending approvals and key counts | Must | Built |
| ADM-02 | Manage website orders: confirm, assign delivery slot/date, mark out for delivery and delivered, cancel, add notes | Must | Built |
| ADM-03 | Manage coupons, influencers, campaigns, samples, content reviews | Must | Built |
| ADM-04 | Manage products and price tiers; manage service areas by pincode and assigned rep | Must | Built |
| ADM-05 | Approve staff signup requests and create staff accounts through a secured server function | Must | Built |
| ADM-06 | Manage sales reps, outlets (add, edit, tier, custom discount) and per-outlet visit notes | Must | Built |
| ADM-07 | Monthly commission recalculation and payout screen | Must | Built |

### 6.4 Field sales: beats, orders, deliveries (FS)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| FS-01 | Admin builds a monthly beat plan per rep: outlets, visit frequency, preferred days, target kg, visit sequence; publish it; derive weekly plans | Must | Built |
| FS-02 | Rep sees today's outlets in sequence, marks visited or missed with a reason | Must | Built |
| FS-03 | Rep places an order for an outlet in kg per product, with tier price and discount rules (threshold-based or outlet-specific) | Must | Built |
| FS-04 | Rep adds a new outlet in the field; admin reviews prospects | Should | Built |
| FS-05 | Rep can suggest beat changes; admin accepts or rejects | Could | Partial (table exists) |
| FS-06 | Driver sees pending deliveries, confirms delivered kg per item with a short-delivery reason, cash collected and payment mode | Must | Built |
| FS-07 | Admin sees deliveries, completed-today list, orders list with cancellation, team list with approvals | Must | Built |
| FS-08 | Holidays calendar to skip beat days | Could | Partial (table exists) |
| FS-09 | Order edit history | Should | Partial (table exists, unused) |
| FS-10 | Installable web app with offline shell (service worker, manifest) | Should | Built |

### 6.5 Stock, batches and traceability (STK)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| STK-01 | Log each production run with a generated batch number (AF-SKU-DDMMYY-run), operator, start/end time, wastage and net quantity; expiry defaults from the product's 10-day shelf life | Must | Built |
| STK-02 | Allocate batch packets to a store and rep; block if more than the batch's available packets | Must | Built |
| STK-03 | Batch Trail view showing a batch's full lifecycle from production to return | Must | Built |
| STK-04 | Record stock sent to a shop, returned or expired, and payments, with expiry auto-calculated per product (Shop Stock and Dues) | Must | Built |
| STK-05 | Settle with an outlet: delivered, sold and returned quantity, amount due and collected | Must | Built, no live rows yet |
| STK-06 | Packing step linking SKU packets to a batch with packed-quantity validation | Should | Gap (table only; production already records pack size) |
| STK-07 | Dispatch against an allocation, then store receipt confirmation with damaged-on-arrival and shortfall | Must | Gap |
| STK-08 | Physical stock check at each store visit (system count vs counted, nearest expiry) | Must | Gap (table only) |
| STK-09 | Daily reconciliation of ledger stock vs last physical count with mandatory reason on adjustment | Should | Gap |
| STK-10 | Audit log of who, what, when, previous and new value, reason; reversal entries instead of deletes | Must | Gap (table only, not written) |
| STK-11 | Stock is always calculated from events, never typed in directly; remove disguised grid edits | Must | Partial |
| STK-12 | Cash register: rep records cash received per outlet; admin clears it | Must | Built |

### 6.6 Invoicing, billing and receivables (INV)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| INV-01 | Create a GST invoice for a company or outlet: customer search by name or GSTIN, line items, rate excluding GST, GST %, discount % | Must | Built |
| INV-02 | Choose GST responsibility per invoice: buyer pays GST on top, or Absolute Fresh absorbs it | Must | Built |
| INV-03 | Sequential invoice numbers by month, issued by the database function next\_invoice\_number, with seller GSTIN and FSSAI shown | Must | Built |
| INV-04 | Reps can raise an invoice from the beat screen; invoices link to a settlement and outlet | Should | Built |
| INV-05 | Past invoices with search, status and reconciliation panel | Must | Built |
| INV-06 | Invoice is a frozen snapshot of bill-to, ship-to, terms and prices at creation | Must | Partial (company fields stored on invoice) |
| INV-07 | Payments recorded at company level and allocated across many invoices (manual, remittance, oldest-first); overpayment held as customer credit | Must | Gap |
| INV-08 | Invoice status derived: sent, partial, paid; overdue is a report condition, not a status | Must | Gap (all 17 invoices are still sent) |
| INV-09 | Credit notes for returns, damage and expiry that reduce receivables without editing the invoice | Must | Gap |
| INV-10 | Invoice from delivered, not ordered, quantity; batch number, manufacture and expiry on every delivery item | Should | Gap |

### 6.7 Companies, outlets and distributors (CMP)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| CMP-01 | Every customer is a Company (billing, terms, credit limit, GSTIN) with one or more Outlets (delivery points); a standalone shop is a Company with one Outlet | Must | Built (24 companies, outlet\_type set) |
| CMP-02 | Outlet types: standalone shop, general trade, chain store, dark store, distributor, other | Must | Built |
| CMP-03 | Company payment terms (days) and credit limit; available credit = limit minus outstanding, kept separate from unallocated customer credit | Must | Partial (fields stored, not computed) |
| CMP-04 | Distributor discount percent with contract start and end; flag expired contracts for renewal | Should | Partial (fields stored) |
| CMP-05 | Serviced locations: downstream shops a distributor reaches, for coverage reporting only, never invoiced | Could | Partial (table, no rows) |
| CMP-06 | Distributor reports stock as ready for resale with timestamp; shelf-life risk flag | Could | Gap |
| CMP-07 | Company Sales and Collections dashboard: outstanding, overdue, aging buckets (current, 1-15, 16-30, 31-60, 60+), last payment, outlet drill-down | Must | Gap |

### 6.8 Reporting and analytics (RPT)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| RPT-01 | Sales dashboard: revenue by day, product, store and rep | Must | Built |
| RPT-02 | Rep performance and weekly beat coverage | Must | Built |
| RPT-03 | Analytics page combining invoices, campaigns, coupons, orders, production and stock | Should | Built |
| RPT-04 | Sales Intelligence page for outlets and fill rates (ordered vs delivered) | Should | Built |
| RPT-05 | Finished-goods stock and shop stock by SKU views | Must | Built |
| RPT-06 | Influencer leaderboard, campaign ROI and marketing summary | Should | Built |
| RPT-07 | Aging, credit exposure, credit note and distributor coverage reports | Should | Gap |

### 6.9 Notifications (NOT)

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| NOT-01 | Owner WhatsApp alert on new order, overdue delivery, late beat confirmation, short delivery; owner number set in Settings | Should | Partial (function in repo, not in deployed list; whatsapp\_logs empty) |
| NOT-02 | Customer order and delivery confirmation by WhatsApp | Could | Gap (done manually today) |

## 7. Business rules and data requirements

**Core business rules**

| ID | Rule |
| --- | --- |
| BR-01 | A web order is accepted only for an active serviceable pincode. |
| BR-02 | Online payment is marked paid only after server-side verification of the Razorpay signature. |
| BR-03 | Every product has a shelf life (10 days today); batch expiry = production date + shelf life. |
| BR-04 | A batch cannot be allocated beyond its available packets. |
| BR-05 | Nothing moves to the next stage until the previous stage is recorded: produced, packed, allocated, dispatched, received, sold or returned. |
| BR-06 | Stock is a calculated result of recorded events, never a typed current value. History is never deleted; mistakes are reversed with a reason. |
| BR-07 | Influencer commission per delivered order by tier (Rs 30 / 50 / 75); TDS 10% when PAN is held and annual payout exceeds Rs 30,000. |
| BR-08 | Influencer coupon discount may not exceed that influencer's ceiling. |
| BR-09 | Invoice number is unique and sequential per month; the invoice stores bill-to and ship-to as they were on the date. |
| BR-10 | GST is either added on top of the base rate or absorbed by Absolute Fresh, chosen per invoice. |
| BR-11 | Invoice outstanding = net amount minus payment allocations minus credit notes; overdue = outstanding above zero and today past due date. |
| BR-12 | Credit limit (maximum exposure) and customer credit (money received, not yet applied) are never stored in one field. |
| BR-13 | Returns, damage and expiry are corrected by credit note, never by editing the old invoice. |
| BR-14 | A distributor's discount applies only within its contract dates. |
| BR-15 | Scope is Uttar Pradesh only: one UP billing relationship per company. |

**Principal data entities** (all in Supabase Postgres)

| Domain | Entities |
| --- | --- |
| People and access | users (admin, rep, driver; auth link), influencers, milkmen (in scope, not yet built), staff\_requests |
| Customers | companies, outlets (company\_id, outlet\_type, price tier), serviced\_locations, prospects, outlet\_visit\_notes |
| Catalogue and pricing | products (MRP, shelf life, pack weight), product\_prices (per tier), discount\_rules, app\_settings |
| Field sales | monthly and weekly beat plans and items, daily\_beat\_logs, beat\_plan\_suggestions, holidays, orders, order\_items, order\_edits, deliveries, delivery\_items |
| Stock and cash | production\_logs, packing\_logs, allocations, stock\_movements, store\_stock\_checks, settlements, outlet\_payments, cash\_register, audit\_log |
| Billing | b2b\_invoices, invoice\_number\_counters (credit notes, payments and allocations not yet present) |
| D2C | web\_orders, service\_areas, leads, d2c\_orders (legacy) |
| Marketing | coupons, coupon\_usage, influencer\_clicks, campaigns, commissions, sample\_requests, content\_approvals, whatsapp\_logs |
| Reporting views | About 20 views, including v\_batch\_timeline, v\_batch\_available, v\_finished\_goods\_stock, v\_shop\_stock\_by\_sku, v\_revenue\_daily, v\_weekly\_rep\_coverage, v\_campaign\_roi |

**Data retention and quality needs:** all financial and stock records are retained (no hard delete); every create, edit and adjustment carries who, when and reason; phone, GSTIN and pincode formats are validated at entry.

## 8. Integrations and non-functional requirements

**External integrations**

| System | Use | Status |
| --- | --- | --- |
| Razorpay | Online checkout, payment verification, webhook | Live |
| Supabase | Database, authentication, edge functions | Live |
| Hostinger (hPanel Git deploy) | Hosting and automatic deployment from GitHub main | Live |
| WhatsApp Business (Meta Graph API) | Owner alerts | Coded, not confirmed deployed |
| Email | Influencer password reset | Live |
| Planned: Google Ads, Instagram, YouTube | Marketing channels managed outside the app | Out of app scope |

**Non-functional requirements**

| Area | Requirement |
| --- | --- |
| Security | Role-based access enforced by the database, not only the screen; no write access for anonymous users; secrets only in server functions; user-supplied text escaped before display |
| Privacy | Bank, UPI and PAN details of influencers visible only to admin; privacy policy on the site |
| Availability | Customer ordering and payment verification must work at all times; target 99.5% monthly |
| Performance | Pages usable on 4G mobile; order submit confirmed within 3 seconds; field pages work with a weak signal |
| Usability | Mobile-first for reps and drivers; large tap targets; installable web app |
| Auditability | Every stock and money event is attributable and reversible |
| Release safety | A staging environment and branch-based releases, because main deploys straight to production |
| Backup | Daily database backup with a tested restore |
| Compliance | GST invoice format, FSSAI licence number and GSTIN printed; TDS rules honoured; Uttar Pradesh scope |
| Maintainability | Plain, step-by-step code and the existing page structure kept; no framework rewrite |

The availability, performance and backup targets are proposed for sign-off; they are not measured or configured in the current build.

## 9. Current-state gaps, risks and security findings

**Security and platform risks (highest priority)**

| # | Finding | Impact | Required action |
| --- | --- | --- | --- |
| R1 | Most tables allow the anonymous role full read and write; the Supabase key is embedded in client-side files | Anyone with the key can read or change customers, orders, invoices and influencer bank details | Replace open policies with role-scoped policies; move admin writes behind authenticated users or server functions |
| R2 | The GitHub repository is public | Code, schema and key are exposed | Make the repository private |
| R3 | Admin and influencer sign-in use a custom users/influencers table with a stored password hash and a session held in the browser, separate from Supabase Auth | Weaker authentication and no server-side session control | Move all roles to Supabase Auth |
| R4 | One Supabase project is labelled DEV in the code and name but holds production data; the app has a placeholder PROD config | Risk of testing against live data | Rename, and create a separate dev project |
| R5 | Every push to main deploys instantly with no staging | A bad commit takes the site down | Branch, review, then merge; add staging |
| R6 | Retired folders and backup HTML remain in the repo | Confusion and stale code | Remove after confirming nothing links to them |

**Functional gaps against the business objectives**

| Gap | Effect today |
| --- | --- |
| No payment allocation, credit notes or derived invoice status | All 17 invoices show sent; receivables cannot be trusted |
| No dispatch-to-receipt step, store stock check, reconciliation or audit writes | Loss between warehouse and shelf is invisible |
| Stock and settlement tables hold little or no live data (stock\_movements, settlements, deliveries, store checks: 0 rows) | The reporting views have nothing to show; the process is not yet in daily use |
| Company dashboard, aging and credit exposure not built | Collections follow-up is manual |
| Milkman delivery programme (in scope): tables exist, no screens built yet; requirements in Appendix K | Build signup, approval, daily entry and monthly commission (Appendix K) |
| Order edit history, holiday calendar and beat suggestions: tables exist, little or no use | Dead or unfinished features |
| No customer notification automation | Order confirmations handled manually |

**Adoption risk:** several modules are built but unused. Each roadmap phase should end with real daily entries by operators and reps, not only a deployed screen.

## 10. Roadmap, assumptions, dependencies and acceptance

**Recommended phasing** (security first, because every later phase writes to the same database)

| Phase | Goal | Delivers | Done when |
| --- | --- | --- | --- |
| P0 Secure the base | Close R1 to R5 | Private repo, role-scoped database policies, Supabase Auth for all roles, staging project, backups | An anonymous request cannot read or change orders, invoices or influencer data |
| P1 Close the stock loop | STK-07 to STK-11 | Dispatch from allocation, store receipt, physical stock check, reconciliation, audit log writes, reversal entries | One batch can be traced from production to a store count with no gaps |
| P2 Money correctness | INV-06 to INV-10, CMP-03 | Payments and allocations, derived invoice status, credit notes, delivered-quantity invoicing, batch fields on delivery items | Family Bazaar-style payment settles several invoices and outstanding is correct |
| P3 Collections visibility | CMP-07, RPT-07 | Company Sales and Collections dashboard, aging, credit exposure, credit note report | Owner can name the top overdue accounts in under a minute |
| P4 Distributors and dark stores | CMP-04 to CMP-06 | Discount with contract validity, serviced locations, ready report-back, shelf-life risk flag | A distributor scenario runs end to end with batch and expiry tracked |
| P5 Growth | WEB-09, NOT-01, NOT-02, MLK-01 to MLK-11 (milkman programme), Android app | Customer accounts, WhatsApp automation, native Android sales app for 10 to 15 internal users | Reps use the app daily |

**Assumptions**

- Uttar Pradesh only; one billing relationship per company.
- The existing static HTML and Supabase architecture stays; no framework rewrite.
- About 10 to 15 internal users in the next phase.
- Operators and reps will enter data daily once screens are simple enough.

**Dependencies**

- Supabase, Razorpay, Hostinger and (for alerts) Meta WhatsApp Business account access.
- Owner decisions on the open questions below.
- Representative real data for acceptance testing (a standalone shop, a chain, a dark store, a distributor).

**Acceptance approach**

- Each requirement ID is tested against the status column in section 6; a Gap becomes Built only when a user completes it end to end with real data.
- Final sign-off scenarios: a D2C order paid online and delivered; a rep beat day with order, delivery, cash and invoice; a batch traced produced to sold or returned; one payment settling many invoices with a credit note applied.

## 11. Open questions and sources

**Open questions for the owner**

- [x] Milkman delivery programme: confirmed in scope by the owner (10 Oct 2026). Tables exist, screens still to be built (Appendix K).
- [ ] What does the deployed function rapid-worker do, and is it still needed?
- [ ] Is the WhatsApp alert function deployed and configured with the owner's number?
- [ ] Should reps be able to see all outlets or only their own beat? Current policies do not enforce either.
- [ ] What credit limits and payment terms apply to each of the 24 companies?
- [ ] Is the target for stock to be tracked per packet or per kg? Production logs kg, orders use kg, stock movements use packets.
- [ ] Who signs off this BRD, and by when?

**Method and limits:** this BRD was derived from the code and data, not from stakeholder interviews. Screens were identified from page code and database tables; no live user sessions were observed. Appendices A to F and H describe observed behaviour; Appendix G (access), J (rebuild tests) and K (milkman programme) are requirements, not observed behaviour. Screens with a status of Built were not tested.

**Sources**

- GitHub repository singhswat/AbsoluteFreshFoodWebsite, branch main, read 10 Oct 2026, including README, sales/marketing/admin.html, sales/salesrep/pages, order/index.html and the edge function sources.
- Supabase production project fbfopgzblqmsflkfejwa: table list, columns, row counts, views and function list, read 10 Oct 2026.
- Project documents: operational traceability spec, gap analysis, implementation status (12 Sep 2026) and the sales, invoicing and reconciliation final spec (3 Oct 2026).

## Appendix A. Detailed specification: access and partner screens

This appendix and B and C were read directly from the page code of sales.absolutefreshfood.com. Field names and thresholds are as coded.

### A1. Sign-in and signup (sales/login.html, sales/signup.html, role apps)

| Screen | Behaviour |
| --- | --- |
| Unified sign-in | Fields: phone number or email, password (show/hide). Tries staff login through Supabase Auth first (profile read from the users table by auth id), then partner login against the influencers table using a SHA-256 password hash. |
| Role routing after sign-in | admin goes to the admin portal; influencer to the partner dashboard; rep to the field sales home; driver straight to Deliveries. |
| Partner login errors | A partner whose status is not approved sees "pending approval". Inactive staff (is\_active false) cannot continue. |
| Signup, role choice | Applicant first chooses Partner (influencer) or Sales / Delivery staff. |
| Partner signup | Full name, email, password; optional Instagram handle, city, phone; how they plan to promote (Instagram, YouTube, WhatsApp Groups, Society / Apartment, Other). Creates an influencer record. |
| Staff signup ("Request Access") | Full name, email, phone, role (Sales Representative or Delivery Driver), area or zone. Creates a staff\_requests row with status pending; duplicate email is checked. Admin approves it in Reps / Team. |
| Partner-site pages | marketing/login, signup, forgot-password, reset-password, terms. Signup requires: name, phone, city, email, password with confirmation (minimum 6 characters), Instagram handle, follower count, content type (Food and Recipes, Health and Fitness, Home and Kitchen, Lifestyle, Parenting), optional YouTube or Facebook link, why partner, and tick-box for exclusivity. Status is set to approved and the exclusivity date is stored. |
| Forgot / reset password | Email entered; a one-time token and expiry are stored on the influencer; reset page checks the token and expiry, requires new password and confirmation of at least 6 characters, stores the new hash and clears the token. |
| Field-sales home (salesrep/index.html) | Email and password; English / Hindi toggle; Clear Session button. Checks role and is\_active, then redirects by role. |

### A2. Partner dashboard (sales/marketing/dashboard.html)

Tabs: Coupons, Samples, Content, Invoice, Earnings.

| Area | Detailed behaviour |
| --- | --- |
| Header performance | Total coupon clicks, orders, revenue, commission, discount ceiling (default 25%), current tier. |
| Coupons | List of the partner's coupons with discount, minimum order, uses over limit, expiry, status and click count. Actions: pause or activate, show QR, share on WhatsApp, copy link. |
| Create coupon | Fields: code, discount %, valid days, minimum order value (0 means none). Code must be 6 to 12 characters, letters and numbers only, upper-cased. Discount must be 5% to 50% and not above the partner's own ceiling. Type is percentage; short link is af.in/code (display only); status starts active. A duplicate code is rejected with a clear message. |
| QR and sharing | QR code (PNG download) for absolutefreshfood.com?coupon=CODE; WhatsApp share text; copy link. |
| Samples | Request a product sample: product (Idli Batter 1 kg or 2 kg, Dosa Batter 1 kg or 2 kg, Combo Pack), content type (Reel, Post, Story, YouTube Video), estimated posting date, delivery address and pincode. After delivery the partner submits the post URL and posting time. |
| Content | Submit content for review: type, caption or script, hashtags, optional draft link, planned posting date, related coupon. Admin decision statuses: pending, approved, changes\_requested, rejected, posted. Partner records the live URL once posted. |
| Earnings | Orders this month, tier progress bar to the next tier (Bronze under 11, Silver 11 to 30, Gold 31 or more) with orders remaining, payment history (orders delivered, tier, TDS, net payout, paid status), and payment details form: UPI ID, bank account, IFSC, PAN. IFSC and PAN are upper-cased. |
| Invoice (partner invoicing) | A partner can raise a B2B invoice to a company: company search by name or GSTIN, customer details, invoice number, date, due date, line items (description, quantity, rate, discount %, GST %, total), PDF download, share on WhatsApp, saved to b2b\_invoices; history list with a mark paid action. Statuses used: sent, paid, overdue. |

## Appendix B. Detailed specification: admin portal (sales/marketing/admin.html)

Four menu groups: Admin, Performance, Finance, Stock. Admin-only. A Home tab shows summary tiles.

### B1. Home and Admin group

| Screen | Detailed behaviour |
| --- | --- |
| Home | Tiles: weekly visit coverage, reps behind plan, finished goods on hand, store packs expiring within 2 days, influencer count, coupon count, orders via coupon, influenced revenue; Refresh button. |
| Orders (website orders) | Table: ref, time, customer, phone, items, total, status, actions. Filters: All, Today, Yesterday, plus status tiles (Confirmed, Out for Delivery, Delivered Today). Row actions: edit (customer, delivery date and notes), confirm, out for delivery, delivered, cancel. Bulk: Confirm All, delete selected. Order cutoff time stored in app\_settings as order\_cutoff\_time (screen default 20:00; the live value today is 23:55): orders after the cutoff are locked for overnight grinding and delivered the next day. |
| Send Dispatch Messages (Send to Reps) | Takes tomorrow's non-cancelled, non-delivered website orders, groups them by the rep assigned to the order's pincode in Service Areas, and opens WhatsApp for each rep with their orders and a route link. Orders with a pincode that has no rep are reported and must be assigned in Areas first. |
| Coupons | Table: code, influencer, discount, uses, minimum order, expiry, status, action. Create Coupon types: Standard percent discount, Buy 1 Get 1 Free, Influencer Free Sample (100% off, 1 use), Rakhi Gift (buy 1 kg and get 500 g and podi chutney free). Fields: code, influencer name and Instagram handle, discount %, max uses, expiry, first-order-only, and "set as website welcome coupon" (popup for new visitors). Edit: discount, expiry, max uses (blank is unlimited), status (Active, Expired, Paused). |
| Influencers | Filter All / Gold / Silver / Bronze. Columns: name and handle, city, tier, orders, revenue, ceiling, status, actions. Adjust Discount Ceiling modal: 5% to 50% in steps of 5. Tier is derived from order count. |
| Campaigns | Filter Active / Draft / Completed / All. New Campaign: name, type (Influencer, Organic, Paid Ads, Referral, Sampling, Seasonal, Society), status (Draft, Active, Paused, Completed), start and end date, budget, UTM source, notes. |
| Samples | Filter All / Pending / Approved / Delivered. Sample Request modal: approve, reject with optional reason, mark delivered. |
| Content (reviews) | Filter All / Pending / Approved / Live. Review Content modal: Approve, Request Changes, Reject; feedback is mandatory for changes and reject. |
| Products | Table: name, unit, base price, status, added, actions. Add Product: name, unit (kg, gm, litre, pack, pcs), MRP / D2C price used by influencers, and tier prices T1 Enterprise / Chain, T2 Mega Store, T3 Supermarket, T4 Retail (default). |
| Reps (staff) | Pending signup requests (name, email, phone, role, area, requested) with approve or reject; Active Staff table (name, email, role, area, beat days, status, password, actions). Add Rep Manually: name, email, phone, role (Admin, Sales Rep, Driver), area, initial password, created through the admin-staff-account server function. Set Password for any staff member. Remove staff. |
| Outlets | Filters: active/inactive, rep, status. Columns: outlet, area, owner, phone, rep, avg kg/day, status, discount, added. Add Outlet: name, contact person, phone, address, assigned rep, tier (Gold 20 kg/week, Silver 10, Bronze 5), average kg/day. Edit Outlet: name, area, owner, phone, avg kg/day, status, tier, custom discount % (blank means standard tier price). Visit Notes modal: note and next action per outlet. Outlet delete also removes its beat-plan rows. |
| Areas (service areas) | Table: pincode, area name, assigned rep, status, added. Add Pincode: pincode and area name. Customers must enter a listed active pincode to order. |

### B2. Performance group

| Screen | Detailed behaviour |
| --- | --- |
| Sales Dashboard | Period filter (All Time, This Month, This Week, Today); rep performance cards; recent orders table (date, rep, outlet, kg ordered, amount, status). |
| Rep Performance | Refreshable rep scorecard. |
| Beat Plans | Per-rep monthly plan. Views: List or Calendar Heatmap. Filters: area, frequency (Weekly, Fortnightly, Monthly, Adhoc), tier. Columns: sequence, outlet, area, tier, visit days, frequency, target kg, actions. Add Outlet to Plan (visit days Mon to Sun, frequency, target kg per visit, notes). Init All Reps creates draft plans for the month; Publish Plan and Publish All Drafts move plans to published; inline edit of any row. |
| Analytics (tab) | Date range 7, 30 or 90 days. Reports: weekly sales-rep coverage (planned, completed, missed, coverage %, stores visited, click a rep for each visit); finished goods stock (produced, dispatched, estimated on hand, last produced); store stock by SKU and expiry (area filter and expiry window: Expired, next 2 days, next 5 days; click a row for its ledger); revenue attribution; campaign performance (budget, spent, revenue, ROI); top coupons. |

### B3. Finance group

| Screen | Detailed behaviour |
| --- | --- |
| Payouts | Filter All / Pending / Paid; Recalculate button builds each partner's monthly commission from delivered orders. Columns: influencer, month, orders, tier, commission, TDS, net payout, status. Mark Paid: method (Bank Transfer or UPI) and transaction reference. |
| Invoices: New Invoice | Pick an existing store or "create new store from this invoice" (then area or colony is mandatory). Fields: company name, GSTIN, address, phone (WhatsApp), email, invoice number (auto), date, due date, our GSTIN, delivery notes. GST responsibility toggle: Buyer Pays GST, or We Absorb GST. Line items: product, HSN, quantity, rate excluding GST, discount %, GST %, amount; Add Item. Totals show subtotal, GST and grand total. Submit saves; Clear resets; Cancel Edit leaves edit mode. |
| Invoices: Past Invoices and Reconciliation | Search by company or GSTIN. Filters: status (Draft, Sent, Paid, Overdue), store, period (This Month, Last Month, Last 3 Months, All Time). Table: invoice number, date, company and GSTIN, product, HSN, quantity, rate, discount, GST, tax, total, status, actions. Actions: Mark Paid with payment reference and date, download PDF, send by WhatsApp, edit, delete. Export to Excel. |
| Shop Stock and Dues | Table per shop: area, packets in shop, nearest expiry, amount receivable. Log Dispatch (shop, product, packets, rate per packet), Log Return (reason: Unsold returned still good, or Expired / Damaged discarded), Log Payment (amount, date, notes). Expiry is calculated from each product's shelf life (default 10 days). |

### B4. Stock group

| Screen | Detailed behaviour |
| --- | --- |
| Production | Log one run as a batch; add several pack sizes under one batch number (for example 35 x 500 g plus 20 x 1 kg). Fields: product and pack size, packets, wastage, batch number (auto, editable), expiry (auto from shelf life), operator, notes. History filter: This Month, Last Month, Last 3 Months, All Time; edit a row in place (no delete, so history stays complete); Export to Excel. |
| Allocate | Choose a batch (shows packets still available), shop, rep and packets. Cannot exceed what is available (produced minus wastage minus already allocated). History table: batch, product, store, rep, packets, date. |
| Batch Trail | Pick a batch number and see its recorded lifecycle (produced, allocated, dispatched, received, returned). |

## Appendix C. Detailed specification: field sales app (sales/salesrep)

Admin pages use data tables with filters and CSV export. Rep and driver pages are mobile-first. Most pages have English and Hindi labels and a Sign Out button.

### C1. Rep and driver screens

| Screen | Who | Detailed behaviour |
| --- | --- | --- |
| Beat (beat.html) | Rep | Day view with previous and next date and refresh. Loads the day's published monthly-plan outlets in visit sequence and saves them as that day's beat log (one per rep, date and outlet). Grid columns: sequence, outlet, area, target, product, kg, invoice, status (planned, active, completed), actions. |
| New Order | Rep | Fields: outlet, product, kg ordered, delivery date, notes; Add Item for more lines. Price = the product's price for the outlet's tier (T1 Enterprise / Chain, T2 Mega Store, T3 Supermarket, T4 Retail default) minus the outlet's discount rule if any; the invoice value is shown live. Saves an order with status pending, its items, and marks the beat visit completed with the order id. An existing order can be edited (items replaced, notes updated). |
| Add Outlet (field) | Rep | Business name, area (both required), owner, phone, price tier, optional GSTIN, address. Creates an outlet assigned to the rep. |
| Create Invoice (from beat) | Rep | Buyer details (name, GSTIN, phone, address), line items with MRP and discount, GST mode (Buyer pays or Included), Submit Invoice. Saves a B2B invoice with status sent, date and due date set to today. |
| My Orders (rep-orders) | Rep | Own orders with filters All, Today, This Week, Pending, Delivered. |
| My Outlets (rep-outlets) | Rep | Searchable list; add outlet with name, area, price tier (T1 Wholesale, T2 Distributor, T3 Retailer, T4 MRP), owner, phone, address or landmark. |
| Stock (stock.html) | Rep | Date-based grid of packets picked up per store and product; Log Pickup (store, product, packets, notes); grid edits are saved as warehouse\_out movements. Counters show totals and settled outlets. Settle: packets sold, packets returned now, cash collected, payment method (Cash, UPI, Pending / Credit). Settle and Generate Invoice saves a settlement, a return movement, and a B2B invoice for the amount due; Share Invoice on WhatsApp. |
| Cash (cash.html) | Rep | Tabs Currently Held and History. Record Cash Received: outlet (required), amount (required), notes. Entry is saved with status held. |
| More (rep-more) | Rep | List of own invoices (number, date, company, total, status, notes) with share, and sign out. |
| Deliveries (deliveries.html) | Driver | Tabs Pending and Done Today. Open an order, enter delivered kg per item (a reason is mandatory when delivered is less than ordered), cash or UPI collected, payment mode (Cash, Cheque, Credit, UPI). Confirm creates the delivery and its items and sets the order delivered. |
| Done Today (done-today.html) | Driver, Admin | Day view: time, outlet, area, product, ordered, delivered, fill %, invoice value, collected, mode, note. |

### C2. Admin screens in the field-sales app

| Screen | Detailed behaviour |
| --- | --- |
| Dashboard | Rep overview and recent orders (time, outlet, area, rep, kg, invoice, status). |
| Beat Plans (beatplan.html) | Month navigation. Init Plans for All Reps; Add Row (rep, outlet, frequency Weekly / Fortnightly / Monthly / Adhoc, target kg, preferred days Mon to Sat, visit sequence); edit and delete rows; Publish All Drafts. Plan status draft or published. |
| Orders | Filters by rep, status (Pending, Delivered, Cancelled) and date; columns include rate and invoice value; Cancel Order with confirmation; Export CSV. |
| Outlets | Search; Add Outlet with business name, area, average kg/day, owner, phone, assigned rep, notes; edit; toggle status (active or prospect). |
| Deliveries (admin) | Per delivery: ordered kg, delivered kg, fill %, payment, collected, short reason; filter by driver; Export CSV. |
| Collections | Per order: invoiced, collected, outstanding, payment mode, status (Outstanding when invoiced exceeds collected); filters by rep and payment mode; Export CSV. |
| Ledger | Tabs Outlet Receivables (invoiced, collected, outstanding, last order, days, status), Rep Payouts, Full Statement (date, type, party, description, debit, credit, running balance). Rep payout inputs: commission rate per kg delivered, base salary per month, fill-rate bonus (default Rs 1,000 when the rep's fill rate is 95% or more). Total payable = base salary + commission per kg delivered + bonus. Recalculate, Mark as Paid, Export. |
| Cash Register (admin) | Filters by rep and status (Held, Cleared); Mark Cleared stamps who and when. |
| Stock Dashboard | Tabs Overview, Production, Allocate, Batch Trail, Settlements, and Alerts. Alerts list outlets whose stock has been out 7 days or more (amber at 7 to 8 days, red at 9 days or more, against the 10-day shelf life). Production form: product, packets, date, auto batch number, auto expiry, wastage, operator, notes. |
| Analytics | Month filter and area / rep filters; daily revenue chart, SKU performance, rep and outlet scorecards, heatmap, a production recommendation derived from the month's kg by SKU and order days, and Export Report. |
| Sales Intelligence | Tabs: Rep Performance (active outlets, kg, change vs last month, fill rate, new outlets, revenue; conversion of visits to orders, missed visits, average order size), Cash Floor (invoiced vs collected, days outstanding, risk High over 7 days, Medium over 3; cash/UPI/credit/cheque mix per rep with credit % flagged High over 30% and Monitor over 15%), Channel and Penetration (outlets, ordering outlets, penetration %, opportunity), Repeat Rate (this vs last month kg; outlets tagged At Risk when down more than 30% and Hunt when they stopped ordering; days silent and next action). |
| Team | Pending approval list, Active Team table with activate or deactivate, Add User (name, email, password of at least 8 characters, role Admin / Driver / Sales Rep, area) created through the create-user server function. |
| Settings | Beat confirm deadline (default 09:00), overdue flag time (default 16:00), manager WhatsApp number; product list with add and activate / deactivate. Stored in app\_settings. |

### C3. Behaviours and rules found in the code that differ from the stated business rules

| # | Finding | Why it matters |
| --- | --- | --- |
| F1 | The admin can delete invoices, outlets, website orders, service areas and staff records. | Conflicts with BR-06 and BR-13 (no hard deletes; correct by reversal or credit note). |
| F2 | An invoice is marked Paid with one click and an optional reference; no payment record or amount is kept. | Part payments, overpayment and reconciliation cannot be represented (INV-07, INV-08). |
| F3 | Invoices can be raised by admin, reps and partners, each with its own number generator in the page. | Sequential numbering (BR-09) is only guaranteed where the database function is used. |
| F4 | Stock page grid edits write movements by typing a quantity. | Conflicts with BR-06 (events, not typed totals). |
| F5 | The order cutoff hour is kept in browser storage as well as in app\_settings. | A setting changed in one browser may not apply for others. |
| F6 | Influencer signup auto-approves with no check. | Fraud and brand-risk exposure; the business must decide whether approval should be manual. |
| F7 | Partner short links (af.in/code) are display text only; the link that works is absolutefreshfood.com?coupon=CODE. | Printed or shared af.in links would not resolve. |
| F8 | Rep payout is computed per kg delivered, while the partner programme pays per order. | Two payout models exist; both need documented sign-off. |

## Appendix D. Data dictionary (what the system must store)

This is the complete information model, in business terms. Every entity below must exist in a rebuilt system with the listed fields and rules. "Required" means the system must refuse a record without it. Money is in Indian rupees; dates are calendar dates unless a time is stated.

### D1. People and access

| Entity | Fields | Rules |
| --- | --- | --- |
| Staff user | name (required), email (unique), phone, role (admin, rep or driver), extra roles (a person can hold several; the first is the main role), area, beat days (Monday to Saturday), active flag (default yes), link to the sign-in account | Only an admin may change roles. An inactive user cannot use the app. |
| Staff request | name, email, phone, role requested (default rep), area, status (default pending) | Created by public signup; admin approves into a staff user. |
| Influencer (partner) | name, email (unique), phone, password (stored hashed), Instagram, YouTube, Facebook, follower count, content category, city, address, pincode, status (pending, approved, rejected or inactive; default approved), verified flag and date, discount ceiling (default 25%), running totals of orders, revenue and commission earned, COD rejection count and rate, bank account, IFSC, UPI, PAN, applied, approved and last-active dates, exclusivity agreement date, admin notes, reset token and expiry, sample reel links | Email is unique. Totals are updated automatically when a coupon-linked order is delivered. |
| Milkman (in scope, not yet built) | name, phone (unique), email, route area, approximate households, UPI, PAN, bank account, PIN (hashed), status (default pending), commission per unit (default 10), totals; daily delivery records (one per milkman per day, items, units, commission) and monthly commissions (units, rate, gross, TDS, net, payment status) | In scope; tables exist but no screens or calculations yet (Appendix K). |

### D2. Customers, outlets and pricing

| Entity | Fields | Rules |
| --- | --- | --- |
| Company | name (required), GSTIN, billing address, phone, email, payment terms in days (default 0), credit limit, status (default active; active, inactive or on hold), distributor discount %, contract start and end dates, notes | One company can own many outlets. |
| Outlet | name and area (required), owner, phone, assigned rep, average kg per day (default 0), status (prospect, active or inactive; default active), added by, notes, price tier 1 to 4 (default 4), grade (gold, silver or bronze; default bronze), company link, outlet type (standalone shop, general trade, chain store, dark store, distributor, other; default general trade) | Deleting a company leaves its outlets in place. Deleting a rep leaves the outlet unassigned. |
| Serviced location | distributor outlet, location name (required), area, notes | Reporting only; never billed. Removed with its distributor outlet. |
| Prospect | name and area (required), owner, phone, notes, added by, status (new, reviewed, converted, rejected) | Converting a prospect creates an active outlet for a chosen rep with a starting average kg per day, and marks the prospect converted. |
| Product | name (required), unit (kg, gm, litre, pack, pcs), base price, MRP, shelf life in days (default 10), pack weight, product code, active flag | Only active products appear for ordering. |
| Product price | product, tier 0 to 4, price (one per product and tier) | Tier 0 = consumer MRP; 1 = Enterprise / Chain; 2 = Mega Store; 3 = Supermarket; 4 = Retail (default). |
| Discount rule | type (volume or percentage), product (optional), outlet (optional), threshold kg, value, valid from and until, active flag | Applies when its product and outlet match, or are blank, and the date is in range. When several apply, the highest value is offered first. |
| Service area | pincode (unique, required), area name, active flag, assigned rep | Customers can order only from active pincodes. The assigned rep receives that pincode's orders. |
| Holiday | date (unique), name | Skipped when weekly beat plans are generated. |

### D3. Orders, delivery and cash

| Entity | Fields | Rules |
| --- | --- | --- |
| Field order | outlet, rep (both required), order date (default today), status (pending, delivered, cancelled), notes, delivery date, coupon, discount amount, customer details, first-order flag | Only one order per outlet per day. |
| Order item | order, product, kg ordered (must be above zero), unit price, discount rule, discount amount | Removed with its order. |
| Order edit history | order, who, old values, new values, when | Intended audit of order edits; not currently written. |
| Delivery | order (one delivery per order), driver, cash collected (default 0), payment mode (cash, UPI, credit, cheque), delivered time | Creating a delivery marks the order delivered automatically. |
| Delivery item | delivery, order item, kg delivered (zero or more), short reason | A reason is required by the screen when delivered is below ordered. |
| Website order | reference (WO- plus a running number), customer name, phone and address (required), pincode, items (name, quantity, price), coupon, subtotal, discount, total, status (new, confirmed, locked, out for delivery, delivered, cancelled), delivery date and slot, admin notes, source (default website), payment method (COD or Razorpay), payment status (pending, paid, COD pending, failed), Razorpay order, payment and signature ids, amount in paise, confirmed, delivered and last-updated times, delivered by | Reference is unique. Updated time refreshes on every change. |
| Legacy D2C order, lead | Older simple order (name, phone, address, pack, coupon, price) and popup lead (phone, coupon, source) | Lead capture is still used; the legacy order table is retired. |
| Cash register entry | rep, outlet, amount (above zero), status (held or cleared), collected time, notes, cleared time and by | Admin clears held cash. |
| Outlet payment | outlet, amount, date, notes | Payments against shop dues. |

### D4. Beat planning

| Entity | Fields | Rules |
| --- | --- | --- |
| Monthly beat plan | rep, month (one plan per rep per month), status (draft, published, archived), published time and by, notes | Only published plans drive the rep's day. |
| Monthly beat item | plan, outlet (an outlet appears once per plan), visit frequency (weekly, fortnightly, monthly, adhoc), preferred days, target kg, visit sequence, notes |  |
| Weekly beat plan and item | rep and week start (one per rep per week), source monthly plan, modified flag, confirmed time; items of plan, source item, outlet, day, sequence, target kg, added-by-admin flag, notes | An outlet appears once per day in a week. Present but not used by the live screens today. |
| Daily beat log | rep, date, outlet (one per rep, date and outlet), sequence, target kg, status (planned, completed, missed), linked order, visited time, missed reason | Becomes completed when the rep places an order. |
| Beat suggestion | rep, outlet, proposed day, reason, status (default pending), admin note | Reserved for rep change requests. |
| Visit note | outlet, rep, time, note (required), next action |  |

### D5. Stock and production

| Entity | Fields | Rules |
| --- | --- | --- |
| Production log | date, product, packets produced (above zero), logged by, notes, batch number, expiry date, start and end time, operator, wastage, net quantity, reversal flag, reason, who and when | Reversal marks a row void without deleting. |
| Packing log | batch, product, packets (zero or more), packet weight kg, packing wastage kg, logged by, reversal fields | Not yet wired to a screen. |
| Allocation | batch, product, outlet, rep, packets (above zero), date, reversal fields | Cannot exceed a batch's available packets. |
| Stock movement | date, rep, outlet, product, packets (above zero), type (warehouse out, delivered, returned, discarded), notes, expiry date, rate, allocation link, batch, quantity received, damaged on arrival | Shop stock is delivered minus returned minus discarded. |
| Settlement | rep, outlet, delivered, sold and returned packets, amount due, amount collected, payment method (cash, UPI, pending), time, links to the stock movements and the invoice it produced, notes |  |
| Store stock check | outlet, rep, product, system quantity, counted quantity, damaged, nearest expiry, time, difference (counted minus system) | Table exists; no screen yet. |
| Audit log | table, row, action, actor, previous and new value, reason, time | Table exists; nothing writes to it yet. |

### D6. Invoicing

| Entity | Fields | Rules |
| --- | --- | --- |
| B2B invoice | number (unique, required), date (required), due date, company name (required), GSTIN, address, phone, email, subtotal, GST total, grand total, line items (name, product, HSN, quantity, rate, discount %, GST %, GST mode), our GSTIN (default 09TULPS3880J1ZB), delivery notes, terms, status (draft, sent, paid, overdue, cancelled; default sent), payment date and reference, notes, created by, rep name, settlement link, outlet link | Number format is shown in section E3. |
| Invoice number counter | year-month key and last number used | Gives gap-free numbers per month. |

### D7. Marketing

| Entity | Fields | Rules |
| --- | --- | --- |
| Coupon | code (unique, required), influencer, short link, type (percentage, flat or BOGO), value, minimum order, total uses allowed, uses per customer (default 1), times used, valid from and expiry, stackable, first-order-only, status (active, paused, expired, depleted), campaign, QR link, maximum discount cap, applicable zones, sample flag, product, welcome flag, max uses, coupon kind (default standard), influencer name and handle | Becomes depleted when uses reach the limit; set to expired after its end date. |
| Coupon usage | coupon, order, customer email and phone, order value, discount, source, referrer, device, IP, time | Each use raises the coupon's counter. |
| Influencer click | coupon, influencer, time, referrer, device, IP, browser, converted flag, order, converted time |  |
| Campaign | name, type (influencer, paid ads, organic, sampling, referral, seasonal, society), budget, spend, revenue, start and end dates, UTM source, status (draft, active, paused, completed), notes | ROI % = (revenue minus spend) divided by spend. |
| Commission | influencer, month (one per influencer per month), orders delivered and rejected, revenue, tier (bronze, silver, gold), rate per order, commission, TDS, net payout, payment status (pending, processing, paid, failed), method, reference, paid time | TDS and net are calculated automatically. |
| Sample request | influencer, product, quantity (default 1), address and pincode (required), content type, estimated post date, status (pending, approved, rejected, delivered, posted), reviewer, rejection reason, linked order and coupon, delivered time, post URL and time, sample cost |  |
| Content approval | influencer, coupon, type, caption, hashtags, media link, planned post date, status (pending, approved, rejected, changes requested, posted), reviewer and feedback, auto-approved flag, live URL, engagement counts, submitted and expiry times |  |
| WhatsApp log | trigger, recipient phone and type, message, status, time, influencer, error |  |
| App setting | key and value with who and when | Keys in use: order cutoff time, beat confirm deadline, overdue flag time, manager WhatsApp number. |

## Appendix E. Business logic and calculations

### E1. Pricing and discounts

| Rule | Detail |
| --- | --- |
| Tier price list (Rs per pack) | 200 g: MRP 45, T1 26, T2 30, T3 33, T4 37. 500 g: MRP 75, T1 44, T2 50, T3 52, T4 55. 1 kg: MRP 140, T1 85, T2 95, T3 105, T4 110. Podi chutney 30 g: MRP 49, T1 36, T2 39.60, T3 41.85, T4 45. |
| Which price applies | A rep's order uses the outlet's tier price for the product; if none is set, the product's base price. Consumers pay MRP. |
| Outlet discount | If an active discount rule applies to the outlet (two percentage rules, 40 and 32, exist today), the order line's discount amount is the rule percentage of the line value. Line value after discount = kg times price minus discount. |
| Grades | Outlet grade by weekly volume: Gold 20 kg or more, Silver 10 to 19 kg, Bronze 5 to 9 kg. Used for planning and filtering. |

### E2. Orders and delivery

- A rep can create only one order per outlet per day; a second attempt is refused with "order already exists".
- Placing an order marks that day's beat visit completed and records the order and visit time.
- Confirming a delivery creates the delivery with its items and marks the order delivered in one step.
- Fill rate = kg delivered divided by kg ordered, shown per delivery, rep and month.
- Collected amount per order = cash or UPI collected at delivery; outstanding = invoiced minus collected; an order with outstanding above zero shows as Outstanding.
- Days outstanding drive credit risk: High above 7 days, Medium above 3, otherwise Low.

### E3. Invoicing calculations

| Rule | Detail |
| --- | --- |
| Invoice number | AFF, a dash, year and month as YYMM, a dash, and a three-digit running number that restarts each month, for example AFF-2610-007. The number is issued by the database so two users never get the same one. If the database cannot be reached the page shows a warning and uses a random number, which must be checked for duplicates. |
| Line rate | Discounted rate = rate times (1 minus discount %). |
| GST mode: Buyer pays GST | The rate is excluding GST. Line total = quantity times discounted rate times (1 plus GST %). Subtotal = sum of quantity times discounted rate. GST = subtotal times GST %. |
| GST mode: We absorb GST | The rate is MRP including GST. Line total = quantity times discounted rate. Base = line total divided by (1 plus GST %); GST = line total minus base. The invoice total equals the entered amount. |
| Defaults | GST 5% on every product. HSN code 10063090 for batter; 09109100 for chutney and podi. A "Custom Item" line lets the user type name, HSN, rate and GST. Default rate = product MRP. |
| Dates | Invoice date and due date default to today. Payment is due on delivery unless terms are typed. |
| Terms printed | "Payment due on delivery. All disputes to be discussed with Absolute Fresh and liability is limited to the total invoice value." unless the user enters other terms. |
| Document | A4 invoice titled with Absolute Fresh, seller GSTIN and FSSAI number 22726747000407, bill-to details, line table (item, HSN, quantity, rate, discount, GST, total), subtotal, GST, grand total, terms, and the footer "This is a system-generated invoice. No signature is required." Can be downloaded as PDF, sent through WhatsApp, and exported to Excel. |
| Store link | Every admin invoice must be linked to a store. Choosing "create new store from this invoice" requires an area and creates an active store (Bronze grade) with the company name, the phone, and a note naming the invoice. |
| Editing | An invoice can be reopened, changed and saved again; a banner shows it is being edited. |
| Payment | Marking paid sets status paid, today's date as payment date, and an optional reference. |
| From stock settlement | Settling a store creates an invoice for packets sold at the store's rate and links it to the settlement. |

### E4. Stock, batches and freshness

- Batch number format: AF, the product code, the date as DDMMYY, and the run number of the day, for example AF-IDB-130926-01. A production run with several pack sizes shares one batch number.
- Expiry date = production date plus the product's shelf life (10 days by default).
- Available packets of a batch = packets produced minus wastage minus packets already allocated (reversed allocations do not count).
- Shop stock = packets delivered minus returned minus discarded; nearest expiry is the earliest expiry among delivered packets; amount receivable = value delivered at rate minus payments.
- Freshness alerts: a shop holding stock for 7 or more days shows amber; 9 or more days shows red.
- Finished goods on hand = total produced minus total dispatched, per product.

### E5. Beat plans

- A monthly plan lists, per outlet: how often (weekly, fortnightly, monthly, adhoc), on which weekdays, how many kg to target, and in what order to visit.
- Today's beat for a rep: take the rep's published plan for the month; keep outlets whose preferred days include today's weekday (or whose days are blank); save them as today's visit list. If none match, all plan outlets are shown. If there is no published plan, the rep's assigned outlets are listed.
- Weekly generation rule (available in the database): weekly outlets every week; fortnightly outlets in weeks 1 and 3 of the month; monthly outlets in week 1; no visits on holidays. Week number = the day of the month of the week start divided by 7, rounded up.
- Coverage % = visits completed divided by visits planned, per rep per week; missed = planned visits marked missed. "Reps behind plan" on Home uses this.
- Monthly plans for all active reps can be created in one action as drafts; publishing makes them live. Plans cannot be duplicated for the same rep and month.

### E6. Marketing and money

- Influencer tier from delivered coupon orders in the month: 0 to 10 Bronze Rs 30 per order; 11 to 30 Silver Rs 50; 31 or more Gold Rs 75.
- Commission = delivered orders times rate. The monthly run looks at the previous month, counts delivered orders that used the partner's coupons, adds the order value, and creates or updates that month's commission as pending.
- TDS: 10% of the commission, rounded to a whole rupee, only when a PAN is on file and the partner's paid net payout for the year plus this commission exceeds Rs 30,000. Net payout = commission minus TDS.
- A delivered order that used an influencer's coupon adds 1 to that influencer's order total and its value to revenue.
- Coupon rules at checkout: valid dates, active status, minimum order, total and per-customer limits, first-order-only, cap on discount, applicable zones, and one coupon per order unless stackable.
- Campaign ROI % = (revenue minus spend) divided by spend times 100.
- Rep payout (Ledger): base salary + commission per kg delivered times kg delivered + a fill-rate bonus (default Rs 1,000) when fill rate is 95% or more. Inputs are editable before payment; Mark as Paid records the payout.

### E7. Website orders

- Orders are placed for the next delivery; orders after the cutoff time (live value 23:55) become locked and are processed overnight (batter is ground fresh).
- Online payment: the order is created first with the Razorpay order id; after payment the signature is verified on the server before status becomes paid; a webhook confirms the same.
- Admin moves an order new, confirmed, out for delivery, delivered, or cancelled; delivered records time and who delivered.
- Tomorrow's orders are grouped by the rep assigned to each pincode and sent to that rep through WhatsApp with a route link.

## Appendix F. Detailed flows: invoicing, outlets and beat plans

### F1. Invoicing flows (admin, sales.absolutefreshfood.com)

1. **Start.** Admin opens Finance, Invoices, New Invoice. They pick an existing store, or choose "create new store from this invoice" (area or colony is then mandatory; an active Bronze-grade store is created with the company name, phone and a note naming the invoice). Every admin invoice must be linked to a store.
2. **Customer details.** Company name, GSTIN, address, phone (WhatsApp), email, invoice date, due date, our GSTIN and delivery notes. The invoice number (AFF-YYMM-NNN) is issued automatically by the database.
3. **GST mode.** Buyer Pays GST (tax added on top of an ex-GST rate) or We Absorb GST (the rate is MRP including GST and the tax is backed out). See E3.
4. **Lines.** Pick a product (rate defaults to the product's MRP, HSN and GST 5% fill in) or add a custom item; set quantity, rate, discount % and GST %. The rate is not taken from the outlet's price tier; it can be changed per line. Subtotal, GST and grand total update live.
5. **Save.** The invoice is stored with status sent. Clear resets the form; Cancel Edit leaves edit mode.
6. **Output.** Download as PDF, send through WhatsApp, export the list to Excel. The document shows seller GSTIN 09TULPS3880J1ZB, FSSAI 22726747000407, terms and the footer in E3.
7. **Edit.** Any invoice can be reopened from Past Invoices, changed and saved; a banner shows edit mode and the number stays the same.
8. **Mark paid.** One action with an optional payment reference; status becomes paid and the payment date is today. No amount is recorded, so part payments are not supported (finding F2; requirement INV-07).
9. **Delete.** Admin can delete an invoice after a confirmation. This is a hard delete and conflicts with BR-06 and BR-13; the target design is cancel or credit note instead.
10. **Other invoice sources.** A rep raises an invoice from the beat screen (status sent, dated today); the rep's stock settlement ("Settle and Generate Invoice") creates one for the amount due and links it to the settlement; a partner can raise a B2B invoice to a company from the partner dashboard. Rep and partner pages carry their own number generators (finding F3).
11. **Past Invoices and Reconciliation.** Search by company or GSTIN; filter by status (Draft, Sent, Paid, Overdue), store and period (This Month, Last Month, Last 3 Months, All Time); the table shows number, date, company, product, HSN, quantity, rate, discount, GST, tax, total, status and actions.

### F2. Outlet flows

1. **Add outlet (admin).** Name, contact person, phone, address, assigned rep, grade (Gold 20 kg a week, Silver 10, Bronze 5) and average kg a day. In the field-sales app admin can also add notes. New outlets are active with price tier 4 unless changed.
2. **Add outlet (rep, in the field).** Business name and area are required; owner, phone, price tier (T1 Wholesale to T4 MRP), optional GSTIN and address or landmark. The outlet is assigned to that rep.
3. **Edit outlet.** Name, area, owner, phone, average kg a day, status (active, inactive, or prospect in the field app), grade, and custom discount %. A blank custom discount means the standard tier price. Price tier (1 to 4) applies to future orders only.
4. **Company and type.** An outlet may belong to a company (the billing identity, GSTIN and terms). Outlet type is one of standalone shop, general trade, chain store, dark store, distributor or other (default general trade).
5. **Prospects.** Reps log prospects (name and area required, owner, phone, notes). Admin reviews them; converting creates an active outlet for a chosen rep with a starting average kg a day and marks the prospect converted.
6. **Rep assignment.** Each outlet has one assigned rep; if that rep is deleted the outlet becomes unassigned.
7. **Visit notes.** Admin opens the Visit Notes window for an outlet and records a note (required) and the next action, with who and when.
8. **Removing an outlet.** Admin delete also removes the outlet's beat-plan rows. This is a hard delete (finding F1); marking the outlet inactive is the safe alternative.

### F3. Beat plan flows

1. **Init.** Admin chooses a month and runs Init Plans for All Reps; one draft plan per active rep per month is created (never two for the same rep and month).
2. **Build.** Admin adds rows (rep, outlet, frequency weekly, fortnightly, monthly or adhoc, preferred weekdays, target kg, visit sequence), edits any row inline, filters by area, frequency and tier, and can switch between List and Calendar Heatmap views.
3. **Publish.** Publish Plan or Publish All Drafts makes plans live. Only published plans drive a rep's day.
4. **Rep's day.** When the rep opens Beat for a date, the app takes the published plan, keeps outlets whose preferred days include that weekday (blank means any day; if none match, all plan outlets), and saves them as that day's visit list in sequence. With no published plan it lists the rep's assigned outlets. The log is created when the rep opens the screen, not by a scheduler.
5. **Execute.** Placing an order for an outlet marks that visit completed and links the order. A visit can be marked missed with a reason.
6. **Coverage.** Admin sees planned, completed and missed visits per rep per week, coverage %, stores visited, and can click a rep to list each visit. Home shows reps behind plan.
7. **Not live yet.** Weekly plans generated by the database (weekly outlets every week, fortnightly in weeks 1 and 3, monthly in week 1, skipping holidays), the holiday calendar and rep change suggestions exist in the data model but the live screens do not use them (FS-05, FS-08).

## Appendix G. Security and roles matrix

| Capability | Admin | Sales rep | Driver | Influencer / partner | Milkman | Public visitor |
| --- | --- | --- | --- | --- | --- | --- |
| Browse website, blog, products | Yes | Yes | Yes | Yes | Yes | Yes |
| Place website order and pay online | Yes | Yes | Yes | Yes | Yes | Yes |
| Partner dashboard (own coupons, samples, content, earnings) | No | No | No | Own only | No | No |
| Raise a B2B invoice | All | Own | No | Own | No | No |
| Sign up (partner, staff request, milkman) | n/a | n/a | n/a | n/a | n/a | Yes |
| Admin portal (all screens) | Yes | No | No | No | No | No |
| Create and manage staff accounts, approve requests | Yes | No | No | No | No | No |
| Outlets, prices, discounts | Manage | Add outlets and prospects, view own | No | No | No | No |
| Invoices, mark paid, delete | Yes | Own invoices (view and share) | No | Own | No | No |
| Beats, visits, field orders | View all | Own only | No | No | No | No |
| Deliveries and cash collection | View all | Record cash received | Own deliveries | No | No | No |
| Stock pickups and settlements | Yes | Own | No | No | No | No |
| Production, allocation, batch trail | Yes | No | No | No | No | No |
| Commission and payout approval | Yes | No | No | No | No | No |
| Milkman daily entry and own earnings | Approve, review, pay | No | No | No | Own only | No |

**Intended principles.** Each user sees only their own records unless they are admin. Financial data (invoices, payouts, TDS) is admin-only. Partners see only their own referrals and earnings. Secrets (payment keys, WhatsApp token, service key) are never exposed to browsers.

**Current state versus intent.** Today many tables allow access with the public (anon) key, so these restrictions are enforced mainly by the screens, not by the database. A rebuild must enforce the matrix above in the database itself (per-role access policies), move partner login to the standard auth service, and keep a separate staging environment. See risks R1–R6 in section 9.

## Appendix H. Configuration and rebuild parameters

**Domains.** absolutefreshfood.com (public site and order page) and sales.absolutefreshfood.com (admin, partner and field-sales apps), both on shared static hosting with automatic deploy from the main branch of the code repository.

**Services required.** Static web hosting with Git deploy; a Postgres database with auth and serverless functions (Supabase); Razorpay account for payments and webhooks; Meta WhatsApp Business account (optional, for alerts); a source repository (GitHub), kept private.

**Secrets (names only, values held by the owner).** Razorpay key id, Razorpay key secret, Razorpay webhook secret, database service-role key, WhatsApp access token and phone-number id. Only the public (anon) key is allowed in browser code.

**Server functions to recreate.** create-order, verify-payment, razorpay-webhook, create-user, admin-staff-account, rapid-worker, and (not yet deployed) whatsapp-alert.

**Settings keys and defaults.**

| Setting | Default / live value | Meaning |
| --- | --- | --- |
| Order cutoff time | Screen default 20:00; live 23:55 | After this time orders roll to the next delivery day |
| Product shelf life | 10 days | Used for batch expiry |
| Freshness alerts | 7 and 9 days | Warn on batches nearing expiry |
| Default GST rate | 5% | Applied to invoice lines |
| TDS | 10% when PAN on file and annual payout above ₹30,000 | Applied to partner payouts |
| Commission tiers | Bronze ₹30 (0–10 orders), Silver ₹50 (11–30), Gold ₹75 (31+) | Per delivered order |
| Rep bonus | ₹1,000 at 95% fill rate | Monthly rep payout |
| Invoice number format | AFF-YYMM-NNN | Sequential per month |
| Batch number format | AF-SKU-DDMMYY-run | Per production run |

**Business identifiers.** Company GSTIN 09TULPS3880J1ZB; FSSAI licence 22726747000407; HSN 10063090 (batter) and 09109100 (chutney).

**Seed data to load.** Products (200 g ₹37 base / ₹45 MRP; 500 g ₹55 / ₹75; 1 kg ₹110 / ₹140; podi 30 g ₹45 / ₹49), tier price list for tiers 0–4, the 8 service areas, the discount rules (percentage 40 and 32), commission tiers, and the order cutoff setting.

## Appendix I. Public website content inventory and brand

**Pages.** Home; products index plus three product pages (1 kg batter, 500 g batter, podi chutney); order page; about; contact; privacy policy; and a blog index with 33 articles. There is no FAQ, terms or shipping page today.

**Brand.** Deep green as the primary colour, red and gold as accents; admin portal uses red (#CC0000) as its accent. Tone: fresh, homemade, South-Indian batter and chutney, short shelf life as a quality promise (made fresh, 10 days).

**Content to preserve separately.** The blog articles, product photography and other static images (several MB: roughly 4.3 MB of images, 1.5 MB of site graphics and 0.6 MB of blog pages) are content, not logic. This document describes them but does not embed them; keep a copy of the repository (private) and of the image folder as the content backup.

**Social / marketing.** Instagram captions and blog posts are drafted ahead (see the pending blog and caption drafts in this project); partner referral links and coupon codes drive attribution.

## Appendix J. Rebuild sequence and acceptance tests

**Suggested order.**

1. Create the database and auth service; load the data dictionary (Appendix D) with the access matrix (Appendix G).
2. Load seed data and settings (Appendix H).
3. Build the pricing, order, delivery and stock rules (Appendix E1, E2, E4).
4. Build server functions for website orders and payments; connect Razorpay and test with a small order.
5. Build the public site and order page.
6. Build the admin portal: outlets, orders, invoices, stock, beats, marketing.
7. Build the field-sales app for reps and drivers.
8. Build the partner signup, dashboard and commission/TDS logic.
9. Add notifications (WhatsApp) and reporting.
10. Set up staging, backups and a private repository before going live.

**Acceptance tests (must all pass).**

| # | Test | Expected result |
| --- | --- | --- |
| 1 | Place a website order after cutoff | Order is dated for the next delivery day |
| 2 | Pay with Razorpay, then replay the webhook | One paid order; no duplicates |
| 3 | Create an invoice for an existing store | Lines default to product MRP and can be edited; number AFF-YYMM-001 for the first of the month |
| 4 | Create two invoices at once | Different, consecutive numbers |
| 5 | Invoice with "we absorb GST" at 5% | Total equals entered price; tax backed out correctly |
| 6 | Create invoice for a new store | Outlet created with note naming the invoice |
| 7 | Mark invoice paid, then view outstanding | Invoice drops from outstanding |
| 8 | Mark an order delivered | Order delivered; the coupon's influencer gets +1 order and the revenue; commission appears in the next monthly run |
| 9 | Partner reaches 11 delivered orders | Commission moves to Silver rate |
| 10 | Partner payout above ₹30,000 with PAN | 10% TDS deducted |
| 11 | Coupon reaches its usage limit | Coupon marked depleted |
| 12 | Generate weekly plan with a holiday | Visit on the holiday is skipped; fortnightly outlets only in weeks 1 and 3 |
| 13 | Rep logs in | Sees only own beat and outlets |
| 14 | Driver logs in | Sees only deliveries |
| 15 | Anonymous user tries to read invoices or payouts | Access denied |
| 16 | Batch older than 7 and 9 days | Freshness warnings appear |

## Appendix K. Milkman delivery programme (in scope)

**Status.** Confirmed in scope by the owner on 10 Oct 2026. Three tables exist in the database (milkmen, milkman\_deliveries, milkman\_commissions), all empty. No screens, server rules or automatic calculations exist yet, so everything below is a requirement to build, shaped by the existing tables.

**Purpose.** Local milkmen already do daily household rounds. The programme lets them carry and deliver Absolute Fresh batter and chutney to the households on their route and earn a commission per unit, instead of the company running its own delivery.

### K1. Roles

| Role | What they do |
| --- | --- |
| Milkman | Registers, logs in with phone and PIN, records daily deliveries, sees own earnings |
| Admin | Approves or rejects milkmen, sets commission rate, reviews deliveries, runs and pays monthly commission |

### K2. Requirements

| ID | Requirement | Priority | Status |
| --- | --- | --- | --- |
| MLK-01 | Milkman signup: name, phone (unique), email, route area, approximate households, UPI id, PAN, bank account, PIN. Starts as pending. | Must | Gap |
| MLK-02 | Admin approves or rejects a milkman, with admin notes. Only approved milkmen can log in and record deliveries. (Unlike influencers, milkmen are not auto-approved.) | Must | Gap |
| MLK-03 | Milkman login by phone and PIN; PIN stored hashed; forgot-PIN handled by admin reset. Use the standard auth service rather than a custom hash. | Must | Gap |
| MLK-04 | Daily delivery entry: one record per milkman per day, with quantity per product (200 g, 500 g, 1 kg batter, chutney, podi) and total units. Editable for the same day only. | Must | Gap |
| MLK-05 | Commission per unit defaults to ₹10 and can be changed per milkman by admin. The rate in force on the day is stored on the day's record, so later rate changes do not alter history. | Must | Gap |
| MLK-06 | Day commission = total units x the day's unit rate. | Must | Gap |
| MLK-07 | Milkman dashboard: today's units, month-to-date units and commission, lifetime totals, payout status of past months. | Should | Gap |
| MLK-08 | Monthly commission run (admin, or automatic at month end): per milkman, sum units, rate, gross, TDS, net payout. | Must | Gap |
| MLK-09 | TDS follows the same rule as partners: 10% when a PAN is on file and the annual payout passes ₹30,000. | Must | Gap |
| MLK-10 | Payment tracking: pending or paid, with method (UPI or bank), reference and paid date. Admin marks paid. | Must | Gap |
| MLK-11 | Admin list and detail screens: milkmen by status, route area, units delivered, commission earned, notes. | Should | Gap |
| MLK-12 | Supply and stock: units handed to milkmen should draw down stock and tie to batches (freshness rules in Appendix E4). Whether supply is recorded per milkman per day is an open question. | Should | Open |
| MLK-13 | Household orders: how end customers order through a milkman (subscription, walk-up, or via the website with a milkman referral code) is not defined yet. | Could | Open |
| MLK-14 | Milkman payout statement or invoice, numbered like other documents (AFF-YYMM-NNN). | Could | Open |

### K3. Data (as it exists today)

- **Milkman:** name, phone, email, route area, approximate households, UPI id, PAN, bank account, hashed PIN, status (default pending), commission per unit (default 10), total units delivered, total commission earned, admin notes, created and updated times.
- **Daily delivery:** milkman, delivery date, items (quantity per product), total units, unit commission (default 10), total commission, notes, created and updated times. One per milkman per day.
- **Monthly commission:** milkman, month, total units, unit rate (default 10), gross commission, TDS amount, net payout, payment status (default pending), payment method, payment reference, paid time, updated time. One per milkman per month.

### K4. Flows to build

1. **Onboard.** Milkman signs up, status pending, admin reviews and approves, milkman is told by phone or WhatsApp.
2. **Daily.** Milkman opens the app, enters units delivered per product, saves. The day's commission is calculated and lifetime totals update.
3. **Month end.** Admin runs the commission calculation, checks the figures, applies TDS where due, pays by UPI or bank, records the reference, and marks paid.
4. **Dispute.** Admin can correct a day's record with a note; corrections re-flow into the month's commission until it is marked paid.

### K5. Acceptance tests

| # | Test | Expected result |
| --- | --- | --- |
| 1 | Milkman signs up | Status pending; cannot log in until approved |
| 2 | Admin approves | Milkman can log in |
| 3 | Enter 40 units at ₹10 | Day commission ₹400 |
| 4 | Admin changes rate to ₹12 | New days use ₹12; earlier days stay at ₹10 |
| 5 | Two entries for the same day | Second edits the first; no duplicate |
| 6 | Month-end run | One commission record per milkman with correct units, gross, TDS, net |
| 7 | Payout above ₹30,000 a year with PAN | 10% TDS deducted |
| 8 | Mark paid | Status paid, with method, reference and date |
| 9 | Milkman views another milkman's data | Access denied |
