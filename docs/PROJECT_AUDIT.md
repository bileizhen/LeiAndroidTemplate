# Template contents and build verification

The imported application features and source documentation are retained.

| Required capability | Implementation |
| --- | --- |
| Kotlin, Jetpack Compose, MIUIX | Root/app Gradle files, `ui/theme/Theme.kt` |
| core / data / feature / ui layers | `app/src/main/java/io/github/bileizhen/leitemplate/` |
| DataStore settings | `AppContainer.kt`, `data/settings/`, `data/update/` |
| Logs, crash capture, diagnostics | `core/logging/`, `feature/logs/` |
| GitHub Releases update checking | `core/update/`, `feature/about/` |
| Stable / Prerelease channels | `UpdateChannel`, `UpdateSettingsRepository`, settings page |
| About and appearance pages | `feature/about/`, `feature/settings/` |
| Floating Bottom Bar | `ui/component/MainNavigation.kt`, `ui/LeiTemplateApp.kt` |
| Blur / Liquid Glass configuration | Persisted appearance settings and API-gated switches; advanced shader rendering is an extension point |
| Predictive Back configuration | Appearance setting and API 34+ predictive detail-page return animation |
| Template initialization | `scripts/init_template.py`, regression checks in `scripts/tests/` |
| Standard Gradle Wrapper | Gradle 8.13-generated scripts/JAR and verified distribution checksum |
| GitHub Actions | `.github/workflows/android.yml`: SDK provisioning, initialization checks, debug build, unit tests, APK/reports |

## Repairs

- Resolve the MIUIX Blur manifest minimum SDK conflict while retaining Android 26 support. The current plain navigation never instantiates shader classes.
- Keep main navigation available when the floating option is disabled.
- Handle detail-page back navigation and preserve page state across Activity recreation.
- Restore the standalone embedded wrapper in the optional bootstrap script and verify the distribution checksum.
- Rename both application and test source-set packages; skip generated files and machine-specific SDK settings during initialization.
- Ignore overflowing release version numbers instead of throwing; propagate coroutine cancellation from update checks.
- Exclude module build products from Git and preserve executable/LF wrapper scripts for Linux CI.
- Install the official `platforms;android-37.0` SDK package and set `compileSdkMinor = 0` so a clean runner resolves the same platform without a local directory alias; avoid the retired SDK `tools` package.

## Verification commands

```sh
python3 -m unittest discover -s scripts/tests -v
./gradlew assembleDebug testDebugUnitTest --no-daemon
```

The build targets compile SDK 37 and target SDK 36 with Android Gradle Plugin 8.13.2. This plugin reports a compile-SDK compatibility warning; successful assembly is verified independently of that warning. These checks do not claim device UI or instrumentation coverage.
