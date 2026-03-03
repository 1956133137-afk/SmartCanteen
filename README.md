# SmartCanteen 慧餐通智慧管理系统 (Android 终端)

## 📝 项目简介
本项目是基于 Android 系统开发的智能餐饮终端应用程序，主要用于对接工行（ICBC）开放平台接口。项目目前已实现了标准化的 **MVVM 架构**，集成了高效的 **Jetpack Compose** UI 框架，并攻克了工行网关复杂的 **RSA2 签名** 与 **AES 报文加密** 核心技术逻辑。

## 🛠 技术栈
- **UI 框架**: Jetpack Compose (声明式 UI)
- **架构模式**: MVVM (Model-View-ViewModel) + Clean Architecture
- **依赖注入**: Hilt (Dagger Hilt)
- **网络请求**: Retrofit 2 + OkHttp 4
- **异步处理**: Kotlin Coroutines + Flow
- **本地安全**: BouncyCastle (支持 PKCS#1/PKCS#8 密钥解析)
- **序列化**: Gson

## 📱 硬件与环境适配
- **最低支持**: Android 5.1 (API 22)
- **屏幕适配**: 针对 **800px * 1232px (160 DPI)** 的工业级终端进行像素级优化。
  - 采用 **State Hoisting (状态提升)** 模式，完美支持 Android Studio 预览。
  - 采用 **Weight (权重) 布局**，确保功能模块在不同修长比例的屏幕上自动填充，无底部留白。
  - 针对低 DPI 大屏优化了字号 (30sp+) 与点击触控区域 (88dp 图标底座)。

## 🔒 核心逻辑：工行网关对接 (ICBC Gateway)
项目在 `core/network/IcbcGatewayInterceptor.kt` 中实现了全自动化的工行报文处理流程：

1.  **JSON 极致压缩**: 自动去除业务报文中的空格与换行，确保加签原串与服务端绝对一致。
2.  **AES 报文加密**: 支持 `AES/CBC/PKCS5Padding` 算法，IV 固定为 16 位零。
3.  **动态签名 (RSA2)**: 
    - 自动提取公共参数 (`app_id`, `msg_id`, `timestamp` 等)。
    - 对参数进行 **ASCII 升序排列**。
    - 使用私钥生成 `SHA256WithRSA` 签名。
4.  **私钥自适应解析**: `SmCryptoUtils` 能够自动识别 **PKCS#1** 与 **PKCS#8** 格式的私钥，解决了 Android 原生库解析工行私钥时常见的 `TOO_LONG` 错误。

## 📂 目录结构
```text
com.example.smartcanteen
├── core
│   ├── network          # IcbcGatewayInterceptor (核心拦截器)
│   └── utils            # SmCryptoUtils (RSA/AES 加解密工具)
├── data
│   ├── remote           # Retrofit 接口定义
│   │   └── model        # 工行标准响应与业务 Request/Response
│   └── repository       # 数据仓库层
├── di                   # Hilt 依赖注入模块 (NetworkModule)
├── presentation         # UI 表现层
│   └── main             # 首页功能模块 (ViewModel + Compose UI)
└── SmartCanteenApp.kt   # Hilt Application 入口
```

## 🚀 快速联调
1.  **配置密钥**: 在 `di/NetworkModule.kt` 中填入工行分配的 `APP_ID`、`PRIVATE_KEY` 和 `AES_KEY`。
2.  **修改参数**: 在 `MainViewModel.kt` 中修改 `deviceNo` 等测试参数（建议联调阶段使用纯英文数字）。
3.  **运行同步**: 启动应用，点击“白名单同步”卡片，通过 **Logcat** (搜索 `ICBC_DEBUG`) 查看加签原串与返回结果。

## ⚠️ 注意事项
- **私钥格式**: 请确保私钥是完整的 Base64 字符串。
- **权限**: 项目已申请 `INTERNET` 权限，运行前请确保设备联网。
- **SDK 方案**: 项目预留了 `DefaultIcbcClient` 的调用接口，如需切换官方 SDK，请将 JAR 包放入 `app/libs`。
