package io.github.bileizhen.leitemplate.core.config

/**
 * Product metadata shared by About, update checking and diagnostics.
 * scripts/init_template.py rewrites APP_NAME and GITHUB_REPO for a new project.
 */
object AppMetadata {
    const val APP_NAME = "Lei Android Template"
    const val APP_DESCRIPTION = "MIUIX + Jetpack Compose reusable Android application shell"
    const val AUTHOR = "bileizhen"
    const val GITHUB_OWNER = "bileizhen"
    const val GITHUB_REPO = "LeiAndroidTemplate"
    const val LICENSE = "GPL-3.0-only"

    const val PROJECT_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO"
    const val RELEASES_URL = "$PROJECT_URL/releases"
    const val ISSUES_URL = "$PROJECT_URL/issues"
    const val AUTHOR_URL = "https://github.com/$GITHUB_OWNER"
    // Optional product-specific URLs. Leave blank to use bundled documents.
    const val WEBSITE_URL = ""
    const val PRIVACY_URL = ""
}
