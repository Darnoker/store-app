ALTER TABLE stock_reservations
    ADD COLUMN request_id UUID NOT NULL;

ALTER TABLE stock_reservations
    ADD CONSTRAINT uq_stock_reservations_order_product UNIQUE (order_id, product_id);

CREATE INDEX idx_stock_reservations_order_request_id
    ON stock_reservations(order_id, request_id);
