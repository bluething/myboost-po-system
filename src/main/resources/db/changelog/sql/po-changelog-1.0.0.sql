--liquibase formatted sql

--changeset habib.machpud:create-table-users
--comment: Create users table with proper constraints and indexes
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(500) NOT NULL,
    last_name VARCHAR(500) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_datetime TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_datetime TIMESTAMPTZ
);

-- Create unique constraint on email
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_datetime ON users(created_datetime);
CREATE INDEX idx_users_full_name ON users(first_name, last_name);

--changeset habib.machpud:create-table-items
--comment: Create items table with proper constraints and indexes
CREATE TABLE items (
    id SERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    price BIGINT NOT NULL,
    cost BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_datetime TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_datetime TIMESTAMPTZ
);

-- Add check constraints
ALTER TABLE items ADD CONSTRAINT chk_items_price_positive CHECK (price >= 0);
ALTER TABLE items ADD CONSTRAINT chk_items_cost_positive CHECK (cost >= 0);

-- Create indexes for better performance
CREATE INDEX idx_items_name ON items(name);
CREATE INDEX idx_items_price ON items(price);
CREATE INDEX idx_items_cost ON items(cost);
CREATE INDEX idx_items_created_datetime ON items(created_datetime);

--changeset habib.machpud:create-table-po_h
--comment: Create purchase order header table with proper constraints and indexes
CREATE TABLE po_h (
    id SERIAL PRIMARY KEY,
    datetime TIMESTAMPTZ NOT NULL,
    description VARCHAR(500),
    total_price BIGINT NOT NULL DEFAULT 0,
    total_cost BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_datetime TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_datetime TIMESTAMPTZ
);

-- Add check constraints
ALTER TABLE po_h ADD CONSTRAINT chk_po_h_total_price_positive CHECK (total_price >= 0);
ALTER TABLE po_h ADD CONSTRAINT chk_po_h_total_cost_positive CHECK (total_cost >= 0);

-- Create indexes for better performance
CREATE INDEX idx_po_h_datetime ON po_h(datetime);
CREATE INDEX idx_po_h_total_price ON po_h(total_price);
CREATE INDEX idx_po_h_total_cost ON po_h(total_cost);
CREATE INDEX idx_po_h_created_datetime ON po_h(created_datetime);
CREATE INDEX idx_po_h_created_by ON po_h(created_by);

--changeset habib.machpud:create-table-po_d
--comment: Create purchase order detail table with foreign keys and constraints
CREATE TABLE po_d (
    id SERIAL PRIMARY KEY,
    poh_id INTEGER NOT NULL,
    item_id INTEGER NOT NULL,
    item_qty INTEGER NOT NULL,
    item_cost BIGINT NOT NULL,
    item_price BIGINT NOT NULL
);

-- Add check constraints
ALTER TABLE po_d ADD CONSTRAINT chk_po_d_item_qty_positive CHECK (item_qty > 0);
ALTER TABLE po_d ADD CONSTRAINT chk_po_d_item_cost_positive CHECK (item_cost >= 0);
ALTER TABLE po_d ADD CONSTRAINT chk_po_d_item_price_positive CHECK (item_price >= 0);

-- Add foreign key constraints
ALTER TABLE po_d 
ADD CONSTRAINT fk_po_d_poh_id 
FOREIGN KEY (poh_id) REFERENCES po_h(id) 
ON DELETE CASCADE;

ALTER TABLE po_d 
ADD CONSTRAINT fk_po_d_item_id 
FOREIGN KEY (item_id) REFERENCES items(id) 
ON DELETE RESTRICT;

-- Create indexes for better performance
CREATE INDEX idx_po_d_poh_id ON po_d(poh_id);
CREATE INDEX idx_po_d_item_id ON po_d(item_id);
CREATE INDEX idx_po_d_item_qty ON po_d(item_qty);

-- Unique constraint to prevent duplicate item in same PO
ALTER TABLE po_d 
ADD CONSTRAINT uk_po_d_poh_item UNIQUE (poh_id, item_id);

--changeset habib.machpud:init-data-users
--comment: Insert sample users for testing and development
INSERT INTO users (first_name, last_name, email, phone, created_by, created_datetime) VALUES
('John', 'Doe', 'john.doe@example.com', '+628123456789', 'system', CURRENT_TIMESTAMP),
('Jane', 'Smith', 'jane.smith@example.com', '+628987654321', 'system', CURRENT_TIMESTAMP),
('Michael', 'Johnson', 'michael.johnson@example.com', '+628555666777', 'system', CURRENT_TIMESTAMP),
('Sarah', 'Williams', 'sarah.williams@example.com', '+628444333222', 'system', CURRENT_TIMESTAMP),
('David', 'Brown', 'david.brown@example.com', '+628111222333', 'system', CURRENT_TIMESTAMP);

--changeset habib.machpud:init-data-items
--comment: Insert sample items for testing and development
INSERT INTO items (name, description, price, cost, created_by, created_datetime) VALUES
('Laptop Dell Inspiron 15', '15-inch laptop with Intel Core i5, 8GB RAM, 512GB SSD', 12000000, 10000000, 'system', CURRENT_TIMESTAMP),
('Wireless Mouse Logitech', 'Wireless optical mouse with USB receiver', 350000, 250000, 'system', CURRENT_TIMESTAMP),
('USB-C Hub Multiport', '7-in-1 USB-C hub with HDMI, USB 3.0, and card reader', 750000, 500000, 'system', CURRENT_TIMESTAMP),
('External Hard Drive 1TB', 'Portable external hard drive 1TB capacity', 1200000, 950000, 'system', CURRENT_TIMESTAMP),
('Mechanical Keyboard RGB', 'Gaming mechanical keyboard with RGB backlight', 1800000, 1400000, 'system', CURRENT_TIMESTAMP),
('Monitor 24 inch', '24-inch Full HD IPS monitor with HDMI and DisplayPort', 2500000, 2000000, 'system', CURRENT_TIMESTAMP),
('Webcam HD', 'Full HD 1080p webcam with built-in microphone', 800000, 600000, 'system', CURRENT_TIMESTAMP),
('Bluetooth Speaker', 'Portable Bluetooth speaker with 12-hour battery life', 450000, 350000, 'system', CURRENT_TIMESTAMP),
('Smartphone Samsung', 'Samsung Galaxy A54 128GB with dual camera', 5500000, 4800000, 'system', CURRENT_TIMESTAMP),
('Tablet iPad', 'Apple iPad 10.9-inch Wi-Fi 64GB', 7500000, 6800000, 'system', CURRENT_TIMESTAMP);

--changeset habib.machpud:init-data-po_h
--comment: Insert sample purchase order headers
INSERT INTO po_h (datetime, description, total_price, total_cost, created_by, created_datetime) VALUES
(CURRENT_TIMESTAMP - INTERVAL '10 days', 'Office Equipment Purchase - Q4 2024', 16850000, 14150000, 'john.doe@example.com', CURRENT_TIMESTAMP - INTERVAL '10 days'),
(CURRENT_TIMESTAMP - INTERVAL '7 days', 'IT Accessories Procurement', 3250000, 2500000, 'jane.smith@example.com', CURRENT_TIMESTAMP - INTERVAL '7 days'),
(CURRENT_TIMESTAMP - INTERVAL '5 days', 'Mobile Device Purchase', 13000000, 11600000, 'michael.johnson@example.com', CURRENT_TIMESTAMP - INTERVAL '5 days'),
(CURRENT_TIMESTAMP - INTERVAL '2 days', 'Audio Equipment Order', 1250000, 950000, 'sarah.williams@example.com', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(CURRENT_TIMESTAMP, 'Development Team Equipment', 10300000, 8600000, 'david.brown@example.com', CURRENT_TIMESTAMP);

--changeset habib.machpud:init-data-po_d
--comment: Insert sample purchase order details
-- PO #1 Details (Office Equipment)
INSERT INTO po_d (poh_id, item_id, item_qty, item_cost, item_price) VALUES
(1, 1, 1, 10000000, 12000000),  -- Laptop
(1, 2, 3, 750000, 1050000),     -- Mouse x3 (250000*3, 350000*3)
(1, 5, 2, 2800000, 3600000),    -- Keyboard x2 (1400000*2, 1800000*2)
(1, 7, 1, 600000, 800000);      -- Webcam

-- PO #2 Details (IT Accessories)
INSERT INTO po_d (poh_id, item_id, item_qty, item_cost, item_price) VALUES
(2, 3, 3, 1500000, 2250000),    -- USB-C Hub x3 (500000*3, 750000*3)
(2, 4, 1, 950000, 1200000);     -- External HDD

-- PO #3 Details (Mobile Devices)
INSERT INTO po_d (poh_id, item_id, item_qty, item_cost, item_price) VALUES
(3, 9, 2, 9600000, 11000000),   -- Smartphone x2 (4800000*2, 5500000*2)
(3, 10, 1, 6800000, 7500000);   -- iPad

-- PO #4 Details (Audio Equipment)
INSERT INTO po_d (poh_id, item_id, item_qty, item_cost, item_price) VALUES
(4, 8, 2, 700000, 900000),      -- Bluetooth Speaker x2 (350000*2, 450000*2)
(4, 2, 1, 250000, 350000);      -- Mouse

-- PO #5 Details (Development Equipment)
INSERT INTO po_d (poh_id, item_id, item_qty, item_cost, item_price) VALUES
(5, 6, 3, 6000000, 7500000),    -- Monitor x3 (2000000*3, 2500000*3)
(5, 3, 2, 1000000, 1500000),    -- USB-C Hub x2 (500000*2, 750000*2)
(5, 4, 1, 950000, 1200000),     -- External HDD
(5, 7, 1, 600000, 800000);      -- Webcam

--rollback DROP TABLE po_d; DROP TABLE po_h; DROP TABLE items; DROP TABLE users;