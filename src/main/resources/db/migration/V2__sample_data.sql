-- V2__sample_data.sql
-- Representative demo data for SaaS Inventory Platform
-- Generated: 2025-08-31 13:00 UTC

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET time_zone = '+00:00';

-- Assumes V1__init.sql has been applied

START TRANSACTION;

-- Tenants
INSERT INTO tenant (id, code, name, status) VALUES
  (1,'demo','Demo Tenant','ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name), status=VALUES(status);

-- Roles
INSERT INTO role (id, code, name) VALUES
 (1,'ADMIN','Administrator'),
 (2,'MANAGER','Store/Warehouse Manager'),
 (3,'CLERK','Operations Clerk'),
 (4,'VIEWER','Read-only')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Users
INSERT INTO user_account (id, tenant_id, email, display_name, status)
VALUES
 (1,1,'admin@demo.example','Demo Admin','ACTIVE'),
 (2,1,'manager@demo.example','Store Manager','ACTIVE')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), status=VALUES(status);

-- User roles
INSERT IGNORE INTO user_role (user_id, role_id) VALUES (1,1),(2,2);

-- Locations
INSERT INTO location (id, tenant_id, code, name, type, status) VALUES
 (1,1,'WH-DXB','Dubai Warehouse','WAREHOUSE','ACTIVE'),
 (2,1,'ST-MRD','Marina Store','STORE','ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name), type=VALUES(type), status=VALUES(status);

-- Categories
INSERT INTO category (id, tenant_id, code, name, parent_id) VALUES
 (1,1,'APP','Apparel',NULL),
 (2,1,'FTW','Footwear',NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name), parent_id=VALUES(parent_id);

-- Items
INSERT INTO item (id, tenant_id, sku, name, brand, category_id, status) VALUES
 (1,1,'TSHIRT-BLK','T-Shirt Black','Acme',1,'ACTIVE'),
 (2,1,'SNEAKER-01','Sneaker Model 01','Acme',2,'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name), brand=VALUES(brand), category_id=VALUES(category_id), status=VALUES(status);

-- Variants
INSERT INTO item_variant (id, tenant_id, item_id, variant_sku, attributes_json, status) VALUES
 (1,1,1,'TSHIRT-BLK-M','{"size":"M","color":"Black"}','ACTIVE'),
 (2,1,1,'TSHIRT-BLK-L','{"size":"L","color":"Black"}','ACTIVE'),
 (3,1,2,'SNEAKER-01-42','{"size":"42"}','ACTIVE')
ON DUPLICATE KEY UPDATE item_id=VALUES(item_id), attributes_json=VALUES(attributes_json), status=VALUES(status);

-- Supplier
INSERT INTO supplier (id, tenant_id, code, name, contact_email, status) VALUES
 (1,1,'ACME','ACME Supply','supplier@acme.example','ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name), contact_email=VALUES(contact_email), status=VALUES(status);

-- Purchase Order
INSERT INTO purchase_order (id, tenant_id, supplier_id, location_id, code, status, eta_date)
VALUES
 (1,1,1,1,'PO-1001','RECEIVED',CURRENT_DATE())
ON DUPLICATE KEY UPDATE supplier_id=VALUES(supplier_id), location_id=VALUES(location_id), status=VALUES(status), eta_date=VALUES(eta_date);

INSERT INTO purchase_order_line (id, po_id, tenant_id, variant_id, qty_ordered, unit_cost, qty_received) VALUES
 (1,1,1,1,100,20.00,100),
 (2,1,1,3,50,200.00,50)
ON DUPLICATE KEY UPDATE qty_ordered=VALUES(qty_ordered), unit_cost=VALUES(unit_cost), qty_received=VALUES(qty_received);

-- Receipt
INSERT INTO receipt (id, tenant_id, po_id, location_id, received_at, created_by)
VALUES (1,1,1,1, NOW(), 1)
ON DUPLICATE KEY UPDATE po_id=VALUES(po_id), location_id=VALUES(location_id), received_at=VALUES(received_at), created_by=VALUES(created_by);

INSERT INTO receipt_line (id, receipt_id, tenant_id, variant_id, qty_received, lot_id) VALUES
 (1,1,1,1,100,NULL),
 (2,1,1,3,50,NULL)
ON DUPLICATE KEY UPDATE qty_received=VALUES(qty_received), lot_id=VALUES(lot_id);

-- Inventory ledger (receipts)
INSERT IGNORE INTO inventory_ledger (tenant_id, location_id, variant_id, ref_type, ref_id, qty_delta, reason_code, ts, user_id)
VALUES
 (1,1,1,'RECEIPT','PO-1001-L1', 100,'', NOW(), 1),
 (1,1,3,'RECEIPT','PO-1001-L2',  50,'', NOW(), 1);

-- Transfer 30 units of TSHIRT M to Store
INSERT INTO transfer_order (id, tenant_id, code, source_loc_id, dest_loc_id, status)
VALUES (1,1,'TO-2001',1,2,'RECEIVED')
ON DUPLICATE KEY UPDATE source_loc_id=VALUES(source_loc_id), dest_loc_id=VALUES(dest_loc_id), status=VALUES(status);

INSERT INTO transfer_line (id, transfer_id, tenant_id, variant_id, qty)
VALUES (1,1,1,1,30)
ON DUPLICATE KEY UPDATE qty=VALUES(qty);

-- Inventory ledger (transfer out/in)
INSERT IGNORE INTO inventory_ledger (tenant_id, location_id, variant_id, ref_type, ref_id, qty_delta, reason_code, ts, user_id) VALUES
 (1,1,1,'TRANSFER','TO-2001', -30,'', NOW(), 2),
 (1,2,1,'TRANSFER','TO-2001',  30,'', NOW(), 2);

-- Adjustment at store: -2 damaged
INSERT INTO adjustment (id, tenant_id, location_id, reason_code, notes, created_at, created_by)
VALUES (1,1,2,'DAMAGE','Damaged items',NOW(),2)
ON DUPLICATE KEY UPDATE reason_code=VALUES(reason_code), notes=VALUES(notes);

INSERT INTO adjustment_line (id, adjustment_id, tenant_id, variant_id, qty_delta)
VALUES (1,1,1,1,-2)
ON DUPLICATE KEY UPDATE qty_delta=VALUES(qty_delta);

INSERT IGNORE INTO inventory_ledger (tenant_id, location_id, variant_id, ref_type, ref_id, qty_delta, reason_code, ts, user_id)
VALUES (1,2,1,'ADJUSTMENT','ADJ-1', -2,'DAMAGE', NOW(), 2);

-- Stock summary snapshots (reflecting above)
REPLACE INTO stock_summary (tenant_id, location_id, variant_id, on_hand, reserved) VALUES
 (1,1,1,70,0),
 (1,1,3,50,0),
 (1,2,1,28,0);

-- Price list & items
INSERT INTO price_list (id, tenant_id, code, name, currency, valid_from, valid_to) VALUES
 (1,1,'BASE','Base Price List','AED',CURRENT_DATE(), NULL)
ON DUPLICATE KEY UPDATE name=VALUES(name), currency=VALUES(currency), valid_from=VALUES(valid_from), valid_to=VALUES(valid_to);

INSERT INTO price_list_item (id, tenant_id, price_list_id, variant_id, price) VALUES
 (1,1,1,1,55.00),
 (2,1,1,2,55.00),
 (3,1,1,3,399.00)
ON DUPLICATE KEY UPDATE price=VALUES(price);

-- Webhook subscription
INSERT INTO webhook_subscription (id, tenant_id, event_type, target_url, secret, status) VALUES
 (1,1,'InventoryAdjusted','https://webhook.site/example','secret','ACTIVE')
ON DUPLICATE KEY UPDATE target_url=VALUES(target_url), secret=VALUES(secret), status=VALUES(status);

COMMIT;
