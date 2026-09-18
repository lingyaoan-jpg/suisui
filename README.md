# 碎碎念（Suisui）

一个分享日常碎碎念的社交 Android 应用：在自己的「小世界」里发图文碎碎念，在「发现」页逛逛别人的世界，可以点赞、评论、订阅和拉黑。配套一个 Spring Boot 服务端，含 JWT 认证与图片上传。

## 功能

- **小世界**：发布图文碎碎念、删除、点赞，图片支持全屏预览
- **发现**：浏览公开用户，进入他人世界查看其碎碎念
- **互动**：点赞、评论、订阅、黑名单
- **通知**：点赞 / 评论 / 订阅通知列表
- **统计**：碎碎念数、获赞数、评论数等个人数据
- **账号**：注册、登录、改密、资料编辑（含头像与背景图上传）、公开开关

## 技术栈

**Android 端**（`app/`）

- Java + ViewBinding
- Retrofit 2 + OkHttp + Gson
- Glide 图片加载
- RecyclerView / ViewPager2 / Navigation

**服务端**（`suisui-server/`）

- Spring Boot 3.2.6 + Spring Security
- Spring Data JPA + MySQL
- JWT 认证（jjwt 0.12.6）
- 图片上传存储到 `uploads/images/`

## 项目结构

```
suisui/
├── app/                          # Android 端
│   └── src/main/java/com/suisui/app/
│       ├── adapter/              # RecyclerView 适配器
│       ├── api/                  # Retrofit 接口
│       ├── model/                # 数据模型
│       ├── ui/                   # Activity / Fragment
│       └── util/                 # 工具类
├── suisui-server/                # 服务端
│   ├── src/main/java/com/suisui/server/
│   │   ├── config/               # Security、CORS、Web 配置
│   │   ├── controller/           # REST 控制器
│   │   ├── dto/                  # 请求 / 响应 DTO
│   │   ├── entity/               # JPA 实体
│   │   ├── repository/           # JPA Repository
│   │   ├── security/             # JWT 工具
│   │   └── service/              # 业务逻辑
│   └── start.bat                 # 一键启动脚本
└── 项目开发记录.md
```

## 数据库

数据库 `suisui`，9 张表：`users`、`notes`、`note_images`、`note_tags`、`comments`、`likes`、`subscriptions`、`blocklists`、`notifications`。

JPA 配置为 `ddl-auto: update`，首次运行会自动建表。

## 运行

### 服务端

需要本机安装 MySQL 与 JDK 17+。

数据库连接与 JWT 密钥都在 `suisui-server/src/main/resources/application.yml`，均支持用环境变量覆盖（括号内为默认值）：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `DB_HOST` | `localhost` | MySQL 主机 |
| `DB_PORT` | `3306` | MySQL 端口 |
| `DB_NAME` | `suisui` | 数据库名 |
| `DB_USERNAME` | `root` | 数据库账号 |
| `DB_PASSWORD` | `123456` | 数据库密码 |
| `JWT_SECRET` | 开发用占位值 | JWT 签名密钥，**部署到公网前请务必自行设置** |

```bash
# 一键启动（会自动清理 8080 端口占用并检查 MySQL）
suisui-server/start.bat

# 或手动
java -jar suisui-server/target/suisui-server-1.0.0.jar
```

### Android 端

需要 Android SDK（在 `local.properties` 中配置 `sdk.dir`）。

服务端地址同样配在 `local.properties`（该文件不入库）：

```properties
suisui.serverUrl=http://10.0.2.2:8080/
```

- 用**模拟器**：保持默认 `http://10.0.2.2:8080/` 即可（`10.0.2.2` 是 Android 模拟器访问宿主机的固定地址）
- 用**真机**：改成电脑的局域网 IP，例如 `http://192.168.1.100:8080/`（在 cmd 里输入 `ipconfig` 查看），并确保手机与电脑在同一 WiFi 下

该地址会在构建时注入 `BuildConfig.BASE_URL`，接口请求与图片加载共用同一处配置。

```bash
./gradlew.bat assembleDebug
```

调试 APK 输出：`app/build/outputs/apk/debug/app-debug.apk`

## API 一览

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | `/api/auth/register` `/api/auth/login` `/api/auth/password` | 注册 / 登录 / 改密 |
| 用户 | `/api/user/profile` `/api/world/{id}` `/api/world/public` | 资料 / 世界页 / 公开开关 |
| 碎碎念 | `/api/notes` `/api/notes/{userId}` `/api/notes/detail/{id}` | 增删改查 |
| 点赞 | `/api/likes` | 点赞 / 取消 |
| 评论 | `/api/comments` `/api/comments/{noteId}` | 增删改查 |
| 订阅 | `/api/subscriptions` `/api/subscriptions/{id}` | 列表 / 切换 |
| 黑名单 | `/api/blocklist` `/api/blocklist/{id}` | 列表 / 切换 |
| 通知 | `/api/notifications` | 列表 / 清空 |
| 统计 | `/api/stats` | 数据统计 |
| 图片 | `/api/images/upload` | 上传 |
| 发现 | `/api/world/discover` | 发现公开用户 |

## 说明

- 本项目是个人练手项目，Android 端与服务端都还在完善中，管理后台尚未开发。
- 开发过程与踩坑记录见 `项目开发记录.md`。