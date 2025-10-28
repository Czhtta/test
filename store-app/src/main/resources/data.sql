-- 清空旧数据 (可选, 方便每次重启都有干净的数据)
-- 注意：必须按照外键依赖关系的逆序删除
DELETE FROM order_warehouse_allocation;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM stocks;
DELETE FROM products;
DELETE FROM warehouses;
-- DELETE FROM users;        -- 删除用户表

-- 插入商品 (Products)
INSERT INTO products (id, name, description, price, active, created_at, updated_at) VALUES
                                (1, 'Laptop', 'High-performance laptop', 1500.00, true, NOW(), NOW()),
                                (2, 'Keyboard', 'Ergonomic mechanical keyboard', 120.00, true, NOW(), NOW()),
                                (3, 'Mouse', 'Wireless gaming mouse', 80.00, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 插入仓库 (Warehouses)
INSERT INTO warehouses (id, name, location) VALUES
                                               (101, 'Sydney Warehouse', 'Sydney, NSW'),
                                               (102, 'Melbourne Warehouse', 'Melbourne, VIC')
ON CONFLICT (id) DO NOTHING;

-- 插入库存信息 (Stock)
INSERT INTO stocks (id, product_id, warehouse_id, quantity) VALUES
                               (1001, 1, 101, 10),
                               (1002, 2, 101, 50),
                               (1003, 1, 102, 5),
                               (1004, 2, 102, 30),
                               (1005, 3, 102, 100)
ON CONFLICT (id) DO NOTHING;


-- 插入用户 (Users)
-- INSERT INTO users (id, username, password, role, address) VALUES
--                                               (1, 'admin', 'password','ROLE_ADMIN','1 Admin Road, Sydney'),
--                                               (2, 'user', 'password','ROLE_USER','100 User Ave, Melbourne'),
--                                               (3, 'customer', 'password', 'ROLE_USER','500 Demo St, Sydney');

-- 重置自增序列到当前最大ID，避免后续插入时主键冲突
SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE((SELECT MAX(id) FROM products), 1), true);
SELECT setval(pg_get_serial_sequence('warehouses', 'id'), COALESCE((SELECT MAX(id) FROM warehouses), 1), true);
SELECT setval(pg_get_serial_sequence('stocks', 'id'), COALESCE((SELECT MAX(id) FROM stocks), 1), true);
SELECT setval(pg_get_serial_sequence('orders', 'id'), COALESCE((SELECT MAX(id) FROM orders), 1), true);
SELECT setval(pg_get_serial_sequence('order_items', 'id'), COALESCE((SELECT MAX(id) FROM order_items), 1), true);
SELECT setval(pg_get_serial_sequence('order_warehouse_allocation', 'id'), COALESCE((SELECT MAX(id) FROM order_warehouse_allocation), 1), true);

