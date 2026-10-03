// Initial member data from the user-provided LeiFetch reference.
// Replace this list for a new product; these roles describe LeiFetch contributions.
package io.github.bileizhen.leitemplate.core.config

// 开发组与贡献者（内测）名单：内容硬编码，头像按 QQ 号从腾讯头像 CDN 加载。
// detail 只写给需要补充贡献说明的成员，详情弹窗里有则多渲染一段。
data class AboutMember(
    val qq: String,
    val name: String,
    val role: String,
    val detail: String? = null,
)

/** 名单分组；分组标题随成员一起记下，详情弹窗里显示所属分组。 */
data class AboutSection(val title: String, val members: List<AboutMember>)

object AboutCredits {
    private val teamMembers = listOf(
        AboutMember(
            qq = "3140014249",
            name = "bileizhen",
            role = "开发 · 设计 · 维护",
            detail = listOf(
                "LeiFetch 的作者与技术设计者：从 NSFX 下载内核的移植到整套界面都由他独立完成，" +
                        "并负责长期的设计、维护与发布。",
                "",
                "· 内核：NSFX 内核 Kotlin 移植，16 线程、动态尾段拆分、断点跨会话续传、指数退避",
                "· 接管：LSPosed 通用捕获（DownloadManager / OkHttp / WebView）、Firefox 适配、系统下载器插件",
                "· 加速：GitHub 镜像前置转发，用下载地址并行测速，校验 206 与文件总长后择优",
                "· 界面：Miuix + Compose 全套 UI 与动效、分段点阵与速度曲线、实时卡片",
                "· 双入口：legacy Xposed 93 与 libxposed API 101，作用域自动申请；数据只存本机，无遥测",
            ).joinToString("\n"),
        ),
        AboutMember(
            qq = "2468872022",
            name = "LinYe_2804",
            role = "开发 · 图标绘制",
            detail = listOf(
                "对 LeiFetch 现用的 NSFX 下载内核做了大量优化与修改：把 NeoNSF 的功能迁移进来，为内核新增了这些特性。",
                "",
                "· 断点续传：合法的 Last-Modified 也参与安全分段与续传，仍优先强 ETag，兼容旧版仅存 ETag 的断点日志",
                "· 大小捕获：捕获 Firefox、WebView、OkHttp、HttpURLConnection 提供的文件大小，重复捕获任务时用后续结果补全已有任务",
                "· 小文件直连：已知小于 8 MiB 的文件直接单连接下载，省去一次 Range: bytes=0-0 探测；大小提示失效时自动清理临时数据并回退完整探测流程",
                "· 连接复用：完整读取响应后保留 HTTP 连接复用，异常、中断或未读完时仍主动断开，大幅提升下载连接稳定性",
                "",
                "全部改动只作功能性拓展与性能、体验优化，原有的动态尾部分片、全局连接预算、限速、重定向安全与原子检查点机制均保留。",
            ).joinToString("\n"),
        ),
        AboutMember(
            qq = "2536843865",
            name = "Hutao_felicity",
            role = "镜像站",
            detail = "LeiFetch 内置的 GitHub 下载加速镜像站 ghfile.geekertao.top 与 gh.dpik.top，均由他搭建。",
        ),
    )

    private val betaTesters = listOf(
        AboutMember("617498164", "ShiraM1zu", "内测用户"),
        AboutMember("2183396164", "加藤糊", "内测用户"),
        AboutMember("442259851", "KelierAndes", "内测用户"),
        AboutMember("3022513812", "DiceSKY", "内测用户"),
        AboutMember("165658800", "Matsuri", "内测用户"),
        AboutMember("1945826346", "Rcst20", "内测用户"),
        AboutMember("2070526365", "zyemmmm", "内测用户"),
    )

    val sections = listOf(
        AboutSection("开发组", teamMembers),
        AboutSection("贡献者 · 内测", betaTesters),
    )

}
