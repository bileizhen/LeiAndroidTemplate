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

The API-33+ floating bar and its `liquid` / `miuix` helpers are now included under `ui/component`, adapted from the local 123PanX / LeiFetch code. Package names were rewritten, the existing theme and DataStore options are connected, and all upstream headers are preserved. `HighApiFloatingNavigation` keeps shader creation behind API and hardware-support checks. API 26–32, software rendering and disabled blur use the existing plain bar. `ui/util/TiltLightDirection` stabilizes sensor-driven highlights. The full source/license chain is recorded in `THIRD_PARTY_NOTICES.md` and bundled in the APK.

The shared update dialog, offline legal document viewer and diagnostics file exporter are implemented in this template. They contain no product-specific downloader, installer, account or Xposed modules.

## Reusable application services

- Update-checking design is generalized from XBlocker `data/AppUpdates.kt` and its release/version parser. The template removes XBlocker-specific repository URLs and installation behavior and routes repository metadata through `core/config/AppMetadata.kt`.
- About-page structure is generalized from the shared settings/about patterns in the source apps. Product-specific copy, privacy text and upstream notices are intentionally not copied.
- Logging is implemented as a small template-owned file logger with crash capture and diagnostic sharing so new apps do not inherit Xposed- or product-specific diagnostics.
