ALTER TABLE orders
    ADD CONSTRAINT chk_orders_currency CHECK (currency IN ('PLN'));
