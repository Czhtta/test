-- 清空旧数据 (可选, 方便每次重启都有干净的数据)
-- 注意：必须按照外键依赖关系的逆序删除
DELETE FROM order_items;  -- 先删除有外键依赖的子表
DELETE FROM orders;       -- 再删除订单表
DELETE FROM stocks;       -- 删除库存表
DELETE FROM products;     -- 删除商品表
DELETE FROM warehouses;   -- 删除仓库表
DELETE FROM users;        -- 删除用户表

-- 插入商品 (Products)
-- 我们创建三种商品: 笔记本电脑, 键盘, 鼠标
INSERT INTO products (id, name, description, price, active, created_at, updated_at) VALUES
                                                       (1, 'Laptop', 'High-performance laptop', 1500.00, true, NOW(), NOW()),
                                                       (2, 'Keyboard', 'Ergonomic mechanical keyboard', 120.00, true, NOW(), NOW()),
                                                       (3, 'Mouse', 'Wireless gaming mouse', 80.00, true, NOW(), NOW());

-- 插入仓库 (Warehouses)
-- 我们创建两个仓库: 悉尼仓库和墨尔本仓库
INSERT INTO warehouses (id, name, location) VALUES
                                               (101, 'Sydney Warehouse', 'Sydney, NSW'),
                                               (102, 'Melbourne Warehouse', 'Melbourne, VIC');

-- 插入库存信息 (Stock)
-- 这是最关键的一步，它定义了哪个仓库有多少件什么商品
-- 悉尼仓库的库存
INSERT INTO stocks (id, product_id, warehouse_id, quantity) VALUES
                                                               (1001, 1, 101, 10), -- 悉尼仓库有 10 台 Laptop
                                                               (1002, 2, 101, 50), -- 悉尼仓库有 50 个 Keyboard

-- 墨尔本仓库的库存
                                                               (1003, 1, 102, 5),  -- 墨尔本仓库有 5 台 Laptop
                                                               (1004, 2, 102, 30), -- 墨尔本仓库有 30 个 Keyboard
                                                               (1005, 3, 102, 100);-- 墨尔本仓库有 100 个 Mouse

-- 重置自增序列到当前最大ID，避免后续插入时主键冲突
SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE((SELECT MAX(id) FROM products), 1), true);
SELECT setval(pg_get_serial_sequence('warehouses', 'id'), COALESCE((SELECT MAX(id) FROM warehouses), 1), true);
SELECT setval(pg_get_serial_sequence('stocks', 'id'), COALESCE((SELECT MAX(id) FROM stocks), 1), true);
