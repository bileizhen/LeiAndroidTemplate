# Source map

This template is distilled from the user's Android projects rather than cloning one product wholesale.

- **123PanX**: `core / data / feature / ui` layering, DataStore appearance settings, theme controller, navigation shell conventions.
- **LeiFetch**: reusable UI component naming and structure (`FloatingBottomBar`, settings controls, blur/liquid effect groups).
- **XBlocker**: appearance options and compact utility-app shell patterns.
- **LeiChat**: not included in this extraction because the connected GitHub account could not resolve the repository at build time.

## Kept in the base template

- application/container bootstrap
- DataStore appearance repository
- MIUIX theme + Monet + UI scale
- floating navigation fallback
- Home / Settings / About example features
- mainland-China Maven mirror fallback
- CI skeleton

## Optional product modules

Room/KSP, networking, WorkManager, Xposed/libxposed, Media3, Coil, QR, download engines and product-specific repositories should be added only to apps that require them.

## Advanced shared UI

The source applications contain a richer API-33+ floating bar implementation with blur/liquid-glass shader helpers. Keep those files under `ui/component` when publishing the GitHub template and preserve their upstream notices in `THIRD_PARTY_NOTICES.md`.

## Reusable application services

- Update-checking design is generalized from XBlocker `data/AppUpdates.kt` and its release/version parser. The template removes XBlocker-specific repository URLs and installation behavior and routes repository metadata through `core/config/AppMetadata.kt`.
- About-page structure is generalized from the shared settings/about patterns in the source apps. Product-specific copy, privacy text and upstream notices are intentionally not copied.
- Logging is implemented as a small template-owned file logger with crash capture and diagnostic sharing so new apps do not inherit Xposed- or product-specific diagnostics.
