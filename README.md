# 照片信息管理系统

一个功能完善的照片信息管理系统，采用前后端分离架构开发。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- MySQL 8.0
- JWT认证
- Maven

### 前端
- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router
- Axios

## 功能特性

### 核心功能
- ✅ 用户注册/登录/登出
- ✅ 用户信息管理
- ✅ 照片上传（单张/批量）
- ✅ 照片信息编辑
- ✅ 照片删除（软删除/永久删除）
- ✅ 相册管理（创建/编辑/删除）
- ✅ 标签管理
- ✅ 照片收藏
- ✅ 回收站功能
- ✅ 照片搜索与筛选
- ✅ EXIF信息自动提取

### 待开发功能
- 📝 照片分享功能
- 📝 系统管理后台
- 📝 数据统计报表
- 📝 AI智能功能

## 项目结构

```
Photo-message/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/photo/
│   │   │   │   ├── common/      # 公共类
│   │   │   │   ├── config/      # 配置类
│   │   │   │   ├── controller/  # 控制器
│   │   │   │   ├── dto/         # 数据传输对象
│   │   │   │   ├── entity/      # 实体类
│   │   │   │   ├── mapper/      # 数据访问层
│   │   │   │   ├── service/     # 服务层
│   │   │   │   ├── util/        # 工具类
│   │   │   │   └── vo/          # 视图对象
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── schema.sql
│   │   └── test/
│   └── pom.xml
│
└── frontend/                # 前端项目
    ├── src/
    │   ├── api/             # API接口
    │   ├── assets/          # 资源文件
    │   ├── components/      # 组件
    │   ├── layouts/         # 布局
    │   ├── pages/           # 页面
    │   ├── router/          # 路由
    │   ├── stores/          # 状态管理
    │   ├── types/           # 类型定义
    │   ├── utils/           # 工具函数
    │   ├── App.vue
    │   └── main.ts
    ├── index.html
    ├── package.json
    ├── vite.config.ts
    └── tsconfig.json
```

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

1. 创建数据库
```sql
CREATE DATABASE photo_message DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入数据库脚本
```bash
mysql -u root -p photo_message < backend/src/main/resources/schema.sql
```

3. 修改数据库配置
编辑 `backend/src/main/resources/application-dev.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/photo_message?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

4. 启动后端服务
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 安装依赖
```bash
cd frontend
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

3. 构建生产版本
```bash
npm run build
```

## 默认账号

系统默认创建了一个管理员账号：
- 用户名：admin
- 密码：123456

## API文档

### 认证接口
- `POST /api/v1/auth/register` - 用户注册
- `POST /api/v1/auth/login` - 用户登录
- `GET /api/v1/auth/profile` - 获取用户信息
- `PUT /api/v1/auth/profile` - 更新用户信息
- `PUT /api/v1/auth/password` - 修改密码

### 照片接口
- `POST /api/v1/photos` - 上传照片
- `GET /api/v1/photos` - 获取照片列表
- `GET /api/v1/photos/{id}` - 获取照片详情
- `PUT /api/v1/photos/{id}` - 更新照片信息
- `DELETE /api/v1/photos/{id}` - 删除照片
- `POST /api/v1/photos/{id}/favorite` - 收藏照片

### 相册接口
- `POST /api/v1/albums` - 创建相册
- `GET /api/v1/albums` - 获取相册列表
- `GET /api/v1/albums/{id}` - 获取相册详情
- `PUT /api/v1/albums/{id}` - 更新相册
- `DELETE /api/v1/albums/{id}` - 删除相册

### 标签接口
- `POST /api/v1/tags` - 创建标签
- `GET /api/v1/tags` - 获取标签列表
- `PUT /api/v1/tags/{id}` - 更新标签
- `DELETE /api/v1/tags/{id}` - 删除标签

## 部署说明

### 后端部署
1. 打包项目
```bash
mvn clean package -Dmaven.test.skip=true
```

2. 运行JAR包
```bash
java -jar target/photo-backend-1.0.0.jar --spring.profiles.active=prod
```

### 前端部署
1. 构建项目
```bash
npm run build
```

2. 将 `dist` 目录部署到Nginx

### Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    client_max_body_size 50M;
}
```

## 开发指南

### 后端开发规范
- 遵循阿里巴巴Java开发规范
- 使用Lombok简化代码
- 统一异常处理
- 统一响应格式

### 前端开发规范
- 遵循Vue 3官方风格指南
- 使用TypeScript类型定义
- 组件命名使用PascalCase
- 文件命名使用kebab-case

## 许可证

MIT License
