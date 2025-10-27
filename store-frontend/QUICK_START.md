# 快速开始指南

## 1. 安装依赖

```bash
cd store-frontend
npm install
```

## 2. 启动开发服务器

```bash
npm run dev
```

前端应用将在 http://localhost:3000 启动

## 3. 确保后端正在运行

后端服务应该在 http://localhost:8080 上运行

## 4. 测试登录

当前只实现了前端的框架，具体功能待实现

## 项目特性

### 主要页面

- **首页** (`/`): 欢迎页面
- **产品列表** (`/products`): 浏览所有产品
- **产品详情** (`/products/:id`): 查看产品详情
- **登录** (`/login`): 用户登录
- **我的订单** (`/orders`): 查看订单（需登录）

### API 集成

所有API调用通过以下服务：
- `authService`: 登录/登出
- `productService`: 产品相关操作
- `orderService`: 订单相关操作

### 项目结构

```
src/
├── components/      # 组件
│   ├── Layout.tsx          # 布局组件
│   └── PrivateRoute.tsx     # 私有路由保护
├── pages/          # 页面
│   ├── Home.tsx
│   ├── Login.tsx
│   ├── ProductList.tsx
│   ├── ProductDetail.tsx
│   └── OrderList.tsx
├── services/       # API服务
│   ├── api.ts              # Axios配置
│   ├── authService.ts
│   ├── productService.ts
│   └── orderService.ts
├── context/        # React Context
│   └── AuthContext.tsx     # 认证状态管理
├── types/          # TypeScript类型
└── utils/          # 工具函数
```

