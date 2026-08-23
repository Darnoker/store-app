ALTER TABLE stock_reservations
    ADD COLUMN request_id UUID;

UPDATE stock_reservations
SET request_id = id
WHERE request_id IS NULL;

ALTER TABLE stock_reservations
    ALTER COLUMN request_id SET NOT NULL;

ALTER TABLE stock_reservations
    ADD CONSTRAINT uq_stock_reservations_order_product UNIQUE (order_id, product_id);

CREATE INDEX idx_stock_reservations_order_request_id
    ON stock_reservations(order_id, request_id);
