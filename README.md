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
- 文件日志、崩溃日志与诊断导出
- GitHub Releases 检查更新（正式版 / 预发布版）
- 可复用关于页（品牌、版本、项目链接、许可与隐私）
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

请安装 JDK 17 或更新版本，以及 Android SDK Platform 37.0（SDK Manager 包名 `platforms;android-37.0`）。工程显式设置 `compileSdkMinor = 0`，与官方平台目录一致。使用 Android Studio 配置 SDK，或在本机的 `local.properties` 中填写 `sdk.dir`；本机配置不提交。Windows 项目路径包含中文时，可从指向工程的 ASCII 目录联接或 `subst` 盘符构建。

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

调试版的「检查更新」默认显示测试预览，方便查看更新说明、下载源、安装包大小及下载进度；预览不请求网络或安装应用，忽略操作不写入真实设置。正式版检查 GitHub Releases，启动时自动检查仍使用真实服务。需要在调试版手动检查真实更新时，将 `app/build.gradle.kts` 中 debug 的 `UPDATE_DIALOG_PREVIEW` 改为 `false`。

### 日志与诊断

`core/logging` 保存运行及崩溃日志，按字节限制容量并轮转。「导出日志」打开 MIUIX 底部弹窗，提供「保存日志」和「分享日志」。ZIP 内含 `diagnostics.txt`（版本、包名、Android 版本、设备型号与日志）及 `logs.txt`（完整运行日志和异常堆栈）。保存使用系统文件选择器，无需存储权限；分享使用 FileProvider，仅临时授予附件读取权限。

日志写入和导出统一脱敏常见凭据。日志和诊断缓存排除在系统备份之外；诊断缓存最多保留 5 份，新建时清理超过一天的附件。外部保存或发送的副本由用户管理。

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
- 全局通用更新弹窗：检查中、已是最新、错误重试、新版信息与更新说明；
- 自动检查仅在发现新版本时提醒，支持稍后和按通道忽略版本；手动检查仍可查看被忽略版本；
- 并发检查去重、生命周期取消，以及 Activity 重建时不重复自动检查。

仓库与产品信息统一修改：

```text
core/config/AppMetadata.kt
```

初始化脚本会自动修改 App 名称与默认 GitHub 仓库名。

### 关于页

关于页沿用 LeiFetch 的独立品牌区、分阶段滚动淡出和模糊链接卡片，展示应用名称与版本；点击版本复制版本号、versionCode 和包名。关于页提供 GitHub、LeiFetch、许可、声明、隐私及开发组列表，成员卡片含圆形头像、角色、滚动入场动效和详情弹窗；更新与日志入口位于设置页。GPL-3.0、Apache-2.0、第三方声明及隐私说明均可离线阅读、选择复制，并支持系统返回。

`core/config/AboutCredits.kt` 集中配置分组与成员。默认只保留模板作者；新产品可替换作者资料或添加自己的实际成员；头像通过腾讯 CDN 加载并内存缓存，离线使用占位图。

`AppMetadata.WEBSITE_URL` 和 `PRIVACY_URL` 是可选链接，留空时不显示在线入口。创建产品时请同步维护 `assets/legal/PRIVACY.md`、仓库及 APK 内的第三方声明；内置隐私说明描述当前模板的实际网络、日志、备份、导出和更新安装行为。初始化脚本会同步迁移仪器测试包名。

## 已保留的统一外观能力

设置模型统一提供：

- 主题模式：System / Light / Dark
- Monet 动态配色
- UI Scale：80%–120%
- Blur 开关
- Floating Bar 开关
- Liquid Glass 开关
- Predictive Back 开关

基础包提供 Android 26+ 可用的 `PlainFloatingBar`；关闭悬浮底栏后切换到 MIUIX 标准导航栏，底栏仅保留首页和设置；关于页从设置进入。详情页支持系统返回，Android 34+ 可按设置启用预测性返回动画，页面状态在 Activity 重建后恢复。

Android 33+ 且硬件加速可用时，悬浮底栏提供真实 backdrop blur、玻璃折射、色散、高光和拖动反馈；Blur / Liquid Glass 开关即时生效。关闭玻璃只保留模糊，关闭模糊或设备不支持 Shader 时使用纯色底栏。API 26–32 不进入 Shader 组件。设备倾斜高光经过量化，减少无意义重绘。来源与版权链见 `docs/SOURCE_MAP.md` 和 `THIRD_PARTY_NOTICES.md`。

Debug 构建使用 `.debug` applicationId 后缀，可与产品 Release 包并存。连接测试设备后，执行 `./gradlew connectedDebugAndroidTest` 验证底栏、文档、更新弹窗和诊断交互；这些仪器测试不访问真实更新网络。

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

### 参考母版布局

外观页使用小标题栏、返回箭头、响应式手机预览、主题选项和图标分组开关；设置页使用 MIUIX 带说明和勾选的更新渠道菜单。关于页保留模板品牌，沿用 LeiFetch 全屏动态背景、分阶段淡出与模糊链接卡片（低版本使用普通背景），展示图标、名称、版本及透明链接卡；点击版本可复制信息，系统返回回到设置。

更新弹窗使用 MIUIX 底部布局，Markdown 说明可滚动；下载源可通过 `AppMetadata.UPDATE_MIRROR_PREFIX` 添加可选 HTTPS 镜像，默认仅 GitHub 原站。真实 APK 下载要求官方 Releases 提供有效的 `size` 和 `sha256:` digest，完成后校验大小和 SHA-256；缺少校验信息时提供发布页入口。安装前检查包名、版本和签名，正式应用还检查签名与当前版本一致且 versionCode 递增；安装需用户点击，并交给系统确认，未知来源权限返回后继续请求。
