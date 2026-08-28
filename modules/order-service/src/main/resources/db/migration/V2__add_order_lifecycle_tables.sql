ALTER TABLE orders
    ADD COLUMN total_amount NUMERIC(19, 2),
    ADD COLUMN currency VARCHAR(3),
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

UPDATE orders
SET total_amount = price * quantity,
    currency = 'PLN',
    updated_at = created_at
WHERE total_amount IS NULL;

ALTER TABLE orders
    ALTER COLUMN customer_id TYPE UUID USING (
        ('00000000-0000-0000-0000-' || LPAD(TO_HEX(customer_id), 12, '0'))::UUID
    ),
    ALTER COLUMN product_id DROP NOT NULL,
    ALTER COLUMN quantity DROP NOT NULL,
    ALTER COLUMN price DROP NOT NULL,
    ALTER COLUMN total_amount SET NOT NULL,
    ALTER COLUMN currency SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL,
    product_type VARCHAR(32) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL CHECK (unit_price > 0),
    quantity INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);

CREATE TABLE order_status_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    status VARCHAR(32) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    destination VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_events_unpublished ON outbox_events(created_at) WHERE published = FALSE;
