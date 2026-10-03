// Editable About member configuration. Only the template author is included.
package io.github.bileizhen.leitemplate.core.config

data class AboutMember(
    val qq: String,
    val name: String,
    val role: String,
    val detail: String? = null,
)

data class AboutSection(val title: String, val members: List<AboutMember>)

object AboutCredits {
    val sections = listOf(
        AboutSection("开发组", listOf(
            AboutMember(
                qq = "3140014249",
                name = AppMetadata.AUTHOR,
                role = "开发 · 设计 · 维护",
                detail = "应用作者，负责开发、界面设计与长期维护。",
            ),
        )),
    )
}
