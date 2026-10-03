# LeiAndroidTemplate

一个从 **123PanX / LeiFetch / XBlocker** 的共同工程习惯中抽出的 Android 模板，用来快速创建同一套架构和界面的新项目。

> LeiChat 在本次整理时无法通过已连接的 GitHub 账户解析，因此当前版本先以另外三个仓库的公共部分为准。

## 默认技术栈

- Kotlin + Jetpack Compose
- MIUIX KMP 0.9.3
- `compileSdk 37` / `targetSdk 36` / Java 17
- 单 Activity + Edge-to-edge
- DataStore 持久化外观与更新设置
- System / Light / Dark、Monet、UI 缩放
- 悬浮底栏，以及 Blur / Liquid Glass / Predictive Back 的统一配置入口
- `core / data / feature / ui` 分层约定
- 阿里云 Maven 镜像优先、官方仓库回退
- 文件日志、崩溃日志与诊断信息分享
- GitHub Releases 检查更新（正式版 / 预发布版）
- 可复用关于页（版本、GitHub、反馈、日志、许可）
- GitHub Actions Debug 构建与单元测试

模板默认只带“应用壳”，不会把 Room、Xposed、Media3、下载引擎等某个产品才需要的依赖塞进所有新项目。

## 创建新项目

GitHub 仓库启用 **Template repository** 后，点击 **Use this template** 创建项目，然后：

```bash
python scripts/init_template.py \
  --name MyApp \
  --package io.github.bileizhen.myapp
```

Windows：

```powershell
py scripts\init_template.py --name 123PanX --package io.github.bileizhen.pan123x
```

初始化脚本会同时修改：

- `rootProject.name`
- App 显示名
- namespace / applicationId
- Kotlin package 声明与源码目录
- Application 类名（`123PanX` 这类数字开头名称也会自动处理）

### Gradle Wrapper

仓库已提交 Gradle 8.13 标准 Wrapper（`gradlew`、`gradlew.bat`、配置与 JAR），直接构建：

```bash
./gradlew assembleDebug testDebugUnitTest
```

Windows：

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

Wrapper 从 Gradle 官方服务下载发行包并验证 SHA-256。若下载的源码归档省略了 JAR，可运行 `python scripts/bootstrap_gradle_wrapper.py` 恢复；该脚本校验发行包后提取内嵌的标准 Wrapper，不依赖系统 Gradle。

请安装 JDK 17 或更新版本，以及 Android SDK Platform 37。使用 Android Studio 配置 SDK，或在本机的 `local.properties` 中填写 `sdk.dir`；本机配置不提交。Windows 项目路径包含中文时，可从指向工程的 ASCII 目录联接或 `subst` 盘符构建。

GitHub Actions 显式安装 SDK，优先使用官方 Maven 仓库，执行初始化脚本回归检查、`assembleDebug` 与单元测试，并上传 Debug APK 和测试报告。

## 架构

```text
app/src/main/java/<package>/
├─ AppContainer.kt
├─ MainActivity.kt
├─ *Application.kt
├─ core/
│  ├─ config/            # AppMetadata：名称、仓库、作者、许可
│  ├─ logging/           # 文件日志、崩溃日志、诊断报告
│  └─ update/            # GitHub Release 检查与版本比较
├─ data/
│  ├─ settings/          # 外观 Repository + DataStore
│  └─ update/            # 自动检查与更新通道设置
├─ feature/
│  ├─ home/
│  ├─ settings/
│  ├─ logs/
│  └─ about/
└─ ui/
   ├─ component/         # 无业务耦合的设计系统组件
   ├─ theme/
   └─ util/
```

依赖方向建议保持为：`feature -> data/core -> platform`。业务 Repository、API、数据库 Entity 不要放进 `ui`。


## 通用应用能力

### 日志与诊断

`core/logging` 默认保存应用运行日志并做简单轮转，同时安装全局未捕获异常记录器。日志页支持：

- 查看和刷新日志；
- 清空本地日志；
- 分享诊断报告；
- 诊断报告自动附带版本、包名、Android 版本、设备型号以及日志。

业务代码可以直接调用：

```kotlin
container.logger.info("Sync", "Sync started")
container.logger.warn("Network", "Request failed", error)
```

### 检查更新

`core/update` 通过 GitHub Releases API 检查版本，默认支持：

- Stable：只接收正式版；
- Prerelease：正式版与 alpha / beta / rc；
- 启动自动检查开关；
- `v1.2.3`、`1.2.3-rc.1` 等版本比较；
- 识别 Release 中的 APK Asset，但模板默认只跳转发布页，不擅自安装 APK。

仓库与产品信息统一修改：

```text
core/config/AppMetadata.kt
```

初始化脚本会自动修改 App 名称与默认 GitHub 仓库名。

### 关于页

关于页默认展示应用名称、简介、版本号与 versionCode，并提供：检查更新、日志与诊断、GitHub、Issues、开发者与开源许可入口。产品如果需要隐私政策、QQ群、官网等链接，只需要继续扩展 `AppMetadata` 和 `AboutScreen`。

## 已保留的统一外观能力

设置模型统一提供：

- 主题模式：System / Light / Dark
- Monet 动态配色
- UI Scale：80%–120%
- Blur 开关
- Floating Bar 开关
- Liquid Glass 开关
- Predictive Back 开关

基础包提供 Android 26+ 可用的 `PlainFloatingBar`；关闭悬浮底栏后切换到 MIUIX 标准导航栏，保持首页、设置和关于页可访问。详情页支持系统返回，Android 34+ 可按设置启用预测性返回动画，页面状态在 Activity 重建后恢复。

Blur / Liquid Glass 的 DataStore 配置和外观入口已保留。当前模板使用纯色底栏，尚未包含高级 Shader 渲染；添加渲染实现时应单独隔离到 API 33+ 组件，并保留低版本纯色回退与第三方 attribution。对应来源说明见 `docs/SOURCE_MAP.md`。

## 按需能力

不同 App 再按实际需求添加：

- Room + KSP
- OkHttp / Kotlin Serialization
- WorkManager
- Xposed / libxposed
- Media3
- Coil
- ZXing / QR
- 下载/上传引擎

这样 XBlocker 不会背上网盘依赖，聊天软件也不会继承下载器或 Xposed 代码。

## 新项目检查清单

1. 运行 `scripts/init_template.py`。
2. 替换 launcher icon。
3. 修改 versionCode / versionName。
4. 只添加产品真正需要的权限。
5. 在 `core / data / feature` 中添加业务模块。
6. 可复用 MIUIX 组件继续放 `ui/component`。
7. 执行 `./gradlew assembleDebug testDebugUnitTest`。

## License

GPL-3.0-only。详见 `LICENSE` 与 `THIRD_PARTY_NOTICES.md`。
