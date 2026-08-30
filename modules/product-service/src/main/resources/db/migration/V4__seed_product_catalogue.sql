INSERT INTO products (id, name, description, price, product_type, details, created_at, updated_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'The Last Wish', 'A collection of stories introducing Geralt of Rivia.', 49.99, 'BOOK',
     '{"isbn":"978-0-575-07783-7","pages":288,"author":"Andrzej Sapkowski","publisher":"Gollancz","language":"en"}'::jsonb,
     '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    ('10000000-0000-0000-0000-000000000002', 'Steel Longsword', 'A balanced two-handed steel longsword.', 299.99, 'SWORD',
     '{"damage":42,"weight":3.5,"length":115.0,"material":"STEEL"}'::jsonb,
     '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    ('10000000-0000-0000-0000-000000000003', 'Silver Sword', 'A silver sword suited to hunting monsters.', 449.99, 'SWORD',
     '{"damage":55,"weight":2.8,"length":105.0,"material":"SILVER"}'::jsonb,
     '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');

INSERT INTO inventory (product_id, quantity, reserved_quantity, updated_at)
VALUES
    ('10000000-0000-0000-0000-000000000001', 20, 0, '2026-01-01T00:00:00Z'),
    ('10000000-0000-0000-0000-000000000002', 10, 0, '2026-01-01T00:00:00Z'),
    ('10000000-0000-0000-0000-000000000003', 5, 0, '2026-01-01T00:00:00Z');
