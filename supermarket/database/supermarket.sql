-- OPTIONAL: the app creates these tables automatically (ddl-auto=update).
-- Run this file in MySQL Workbench only if you want the tables + sample products ready first.

CREATE DATABASE IF NOT EXISTS supermarket_db;
USE supermarket_db;

CREATE TABLE IF NOT EXISTS products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    category    VARCHAR(255),
    price       DECIMAL(38,2) NOT NULL,
    stock       INT NOT NULL,
    gst_percent DECIMAL(38,2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS bills (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_date      DATETIME(6),
    customer_name  VARCHAR(255),
    customer_phone VARCHAR(255),
    payment_mode   VARCHAR(255),
    subtotal       DECIMAL(38,2),
    gst_amount     DECIMAL(38,2),
    discount       DECIMAL(38,2),
    grand_total    DECIMAL(38,2)
);

CREATE TABLE IF NOT EXISTS bill_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id      BIGINT,
    product_id   BIGINT,
    product_name VARCHAR(255),
    price        DECIMAL(38,2),
    gst_percent  DECIMAL(38,2),
    quantity     INT NOT NULL,
    line_total   DECIMAL(38,2),
    FOREIGN KEY (bill_id) REFERENCES bills(id)
);

INSERT INTO products (name, category, price, stock, gst_percent) VALUES
('Basmati Rice 1kg',   'Grocery',   120.00, 50, 5),
('Toor Dal 1kg',       'Grocery',   150.00, 40, 5),
('Sunflower Oil 1L',   'Grocery',   140.00, 30, 5),
('Amul Milk 500ml',    'Dairy',      30.00, 80, 0),
('Amul Butter 100g',   'Dairy',      58.00, 25, 12),
('Britannia Biscuits', 'Snacks',     20.00, 100, 18),
('Lays Chips',         'Snacks',     20.00, 8,  12),
('Colgate Toothpaste', 'Personal Care', 95.00, 35, 18),
('Dettol Soap',        'Personal Care', 45.00, 5, 18),
('Tata Salt 1kg',      'Grocery',    28.00, 60, 0);
