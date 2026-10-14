-- Absolute Fresh Staging Database Setup
-- Compatible with PostgreSQL 15+ / Supabase
-- Contains: Schema, Idempotency constraints, Invoice sequence function, and Synthetic test seed data.

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

---------------------------------------------------------
-- 1. PRODUCTS & PRICING
---------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    unit TEXT DEFAULT 'kg',
    base_price NUMERIC(10, 2) DEFAULT 0.00,
    mrp NUMERIC(10, 2) DEFAULT 0.00,
    shelf_life INT DEFAULT 10,
    pack_weight TEXT,
    code TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS product_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id TEXT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tier INT NOT NULL CHECK (tier BETWEEN 0 AND 4),
    price NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

---------------------------------------------------------
-- 2. COMPANIES & OUTLETS
---------------------------------------------------------
CREATE TABLE IF NOT EXISTS companies (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    gstin TEXT,
    billing_address TEXT,
    phone TEXT,
    email TEXT,
    payment_terms_days INT DEFAULT 0,
    credit_limit NUMERIC(10, 2) DEFAULT 0.00,
    status TEXT DEFAULT 'active',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS outlets (
    id TEXT PRIMARY KEY,
    client_id TEXT UNIQUE,
    company_id TEXT REFERENCES companies(id),
    name TEXT NOT NULL,
    area TEXT NOT NULL,
    owner TEXT,
    phone TEXT,
    assigned_rep TEXT,
    average_kg_per_day NUMERIC(10, 2) DEFAULT 0.00,
    status TEXT DEFAULT 'active',
    price_tier INT DEFAULT 4 CHECK (price_tier BETWEEN 1 AND 4),
    outlet_type TEXT DEFAULT 'general_trade',
    gstin TEXT,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    sync_version BIGINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

---------------------------------------------------------
-- 3. BEAT PLANNING & VISITS
---------------------------------------------------------
CREATE TABLE IF NOT EXISTS daily_beat_logs (
    id TEXT PRIMARY KEY DEFAULT ('visit-' || gen_random_uuid()::text),
    rep TEXT NOT NULL,
    date DATE NOT NULL,
    outlet_id TEXT NOT NULL REFERENCES outlets(id),
    outlet_name TEXT,
    area TEXT,
    sequence INT NOT NULL,
    target_kg NUMERIC(10, 2) DEFAULT 15.00,
    status TEXT DEFAULT 'PLANNED',
    linked_order TEXT,
    visited_time TIMESTAMPTZ,
    missed_reason TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

---------------------------------------------------------
-- 4. ORDERS & ORDER ITEMS
---------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id TEXT PRIMARY KEY DEFAULT ('ord-' || gen_random_uuid()::text),
    client_id TEXT UNIQUE NOT NULL,
    outlet_id TEXT NOT NULL REFERENCES outlets(id),
    outlet_name TEXT,
    rep_id TEXT NOT NULL,
    order_date DATE NOT NULL DEFAULT CURRENT_DATE,
    delivery_date DATE,
    status TEXT DEFAULT 'PENDING',
    notes TEXT,
    invoice_number TEXT,
    subtotal NUMERIC(10, 2) DEFAULT 0.00,
    tax_amount NUMERIC(10, 2) DEFAULT 0.00,
    total_amount NUMERIC(10, 2) DEFAULT 0.00,
    payment_mode TEXT DEFAULT 'CASH',
    payment_status TEXT DEFAULT 'UNPAID',
    sync_version BIGINT DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id TEXT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id TEXT NOT NULL REFERENCES products(id),
    sku_name TEXT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    kg_ordered NUMERIC(10, 2) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    discount_amount NUMERIC(10, 2) DEFAULT 0.00,
    subtotal NUMERIC(10, 2) NOT NULL,
    tax_rate NUMERIC(4, 2) DEFAULT 0.05
);

---------------------------------------------------------
-- 5. DELIVERIES & PROOF OF DELIVERY
---------------------------------------------------------
CREATE TABLE IF NOT EXISTS deliveries (
    id TEXT PRIMARY KEY DEFAULT ('del-' || gen_random_uuid()::text),
    client_id TEXT UNIQUE,
    order_id TEXT REFERENCES orders(id),
    driver_id TEXT NOT NULL,
    cash_collected NUMERIC(10, 2) DEFAULT 0.00,
    payment_mode TEXT DEFAULT 'CASH',
    delivered_time TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS delivery_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id TEXT NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    product_id TEXT,
    kg_delivered NUMERIC(10, 2) NOT NULL,
    short_reason TEXT
);

---------------------------------------------------------
-- 6. HOLIDAYS
---------------------------------------------------------
CREATE TABLE IF NOT EXISTS holidays (
    id TEXT PRIMARY KEY DEFAULT ('hol-' || gen_random_uuid()::text),
    date DATE NOT NULL UNIQUE,
    name TEXT NOT NULL
);

---------------------------------------------------------
-- 7. INVOICE COUNTER & SEQUENTIAL NUMBER GENERATOR
---------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_number_counters (
    year_month TEXT PRIMARY KEY,
    last_seq INT NOT NULL DEFAULT 0
);

CREATE OR REPLACE FUNCTION next_invoice_number()
RETURNS TEXT AS $$
DECLARE
    ym TEXT;
    seq INT;
BEGIN
    ym := TO_CHAR(CURRENT_DATE, 'YYMM');
    INSERT INTO invoice_number_counters (year_month, last_seq)
    VALUES (ym, 1)
    ON CONFLICT (year_month) DO UPDATE
    SET last_seq = invoice_number_counters.last_seq + 1
    RETURNING last_seq INTO seq;

    RETURN 'AFF-' || ym || '-' || LPAD(seq::text, 3, '0');
END;
$$ LANGUAGE plpgsql;

---------------------------------------------------------
-- 8. SYNTHETIC SEED DATA FOR STAGING ENVIRONMENT
---------------------------------------------------------
-- Products
INSERT INTO products (id, name, unit, base_price, mrp, shelf_life, pack_weight, code, active)
VALUES 
    ('p1', 'Fresh Idli Dosa Batter 500g', 'pack', 40.50, 60.00, 10, '500g', 'BAT-500', true),
    ('p2', 'Fresh Idli Dosa Batter 1kg', 'pack', 76.50, 110.00, 10, '1000g', 'BAT-1000', true),
    ('p3', 'Gunpowder Podi Chutney 100g', 'pack', 25.20, 40.00, 30, '100g', 'PODI-100', true),
    ('p4', 'Traditional Filter Coffee Decoction 200ml', 'pack', 55.80, 80.00, 15, '200ml', 'COF-200', true)
ON CONFLICT (id) DO NOTHING;

-- Product Tier Prices (Tier 1: Chain, Tier 2: Mega Store, Tier 3: Supermarket, Tier 4: Retail)
INSERT INTO product_prices (product_id, tier, price)
VALUES
    ('p1', 1, 38.00), ('p1', 2, 40.00), ('p1', 3, 42.00), ('p1', 4, 45.00),
    ('p2', 1, 72.00), ('p2', 2, 75.00), ('p2', 3, 80.00), ('p2', 4, 85.00),
    ('p3', 1, 24.00), ('p3', 2, 25.00), ('p3', 3, 26.00), ('p3', 4, 28.00)
ON CONFLICT DO NOTHING;

-- Sample Outlets (Lucknow East territory matching app)
INSERT INTO outlets (id, client_id, name, area, owner, phone, assigned_rep, average_kg_per_day, status, price_tier, outlet_type, address, latitude, longitude)
VALUES
    ('o1', 'out-o1', 'Sharma Kirana', 'Indira Nagar', 'Anil Sharma', '9876543201', 'rep-1', 15.0, 'active', 3, 'general_trade', '12 Indira Nagar Sector 14', 26.8800, 80.9900),
    ('o2', 'out-o2', 'Gupta Dairy & More', 'Vikas Nagar', 'Ramesh Gupta', '9876543202', 'rep-1', 20.0, 'active', 4, 'general_trade', '45 Vikas Nagar Sector 4', 26.8900, 80.9600),
    ('o3', 'out-o3', 'Krishna Sweets', 'Hazratganj', 'K. Prasad', '9876543203', 'rep-1', 30.0, 'active', 2, 'standalone_shop', '7 Hazratganj Main Market', 26.8500, 80.9400),
    ('o4', 'out-o4', 'New Delhi Canteen', 'Gomti Nagar', 'S. Malhotra', '9876543204', 'rep-1', 25.0, 'active', 2, 'chain_store', '22 Gomti Nagar Patrakarpuram', 26.8467, 80.9982)
ON CONFLICT (id) DO NOTHING;

-- Sample Today's Beat Visits
INSERT INTO daily_beat_logs (id, rep, date, outlet_id, outlet_name, area, sequence, target_kg, status)
VALUES
    ('v1', 'rep-1', CURRENT_DATE, 'o1', 'Sharma Kirana', 'Indira Nagar', 1, 15.0, 'PLANNED'),
    ('v2', 'rep-1', CURRENT_DATE, 'o2', 'Gupta Dairy & More', 'Vikas Nagar', 2, 20.0, 'PLANNED'),
    ('v3', 'rep-1', CURRENT_DATE, 'o3', 'Krishna Sweets', 'Hazratganj', 3, 25.0, 'PLANNED'),
    ('v4', 'rep-1', CURRENT_DATE, 'o4', 'New Delhi Canteen', 'Gomti Nagar', 4, 30.0, 'PLANNED')
ON CONFLICT (id) DO NOTHING;

-- Upcoming Holidays
INSERT INTO holidays (id, date, name)
VALUES
    ('h1', '2026-10-24', 'Dussehra / Vijayadashami'),
    ('h2', '2026-11-01', 'Diwali / Deepawali'),
    ('h3', '2026-11-15', 'Guru Nanak Jayanti')
ON CONFLICT (date) DO NOTHING;

---------------------------------------------------------
-- 9. ROW LEVEL SECURITY (RLS) OPEN FOR STAGING DEVELOPMENT
---------------------------------------------------------
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_prices ENABLE ROW LEVEL SECURITY;
ALTER TABLE companies ENABLE ROW LEVEL SECURITY;
ALTER TABLE outlets ENABLE ROW LEVEL SECURITY;
ALTER TABLE daily_beat_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE deliveries ENABLE ROW LEVEL SECURITY;
ALTER TABLE delivery_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE holidays ENABLE ROW LEVEL SECURITY;

-- Allow anon read/write for staging development testing
CREATE POLICY "Staging full access products" ON products FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access product_prices" ON product_prices FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access companies" ON companies FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access outlets" ON outlets FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access daily_beat_logs" ON daily_beat_logs FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access orders" ON orders FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access order_items" ON order_items FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access deliveries" ON deliveries FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access delivery_items" ON delivery_items FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Staging full access holidays" ON holidays FOR ALL USING (true) WITH CHECK (true);
