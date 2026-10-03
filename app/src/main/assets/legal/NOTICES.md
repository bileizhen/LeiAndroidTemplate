# Third-party notices

This template uses the following libraries at runtime:

- MIUIX KMP by Yukonga and contributors (`top.yukonga.miuix.kmp`), Apache-2.0.
- AndroidX / Jetpack Compose, under the Apache License 2.0.
- Kotlin and kotlinx.coroutines by JetBrains and contributors, Apache-2.0.
- Material Icons by Google, Apache-2.0.

## Floating Bottom Bar and Liquid Glass

`FloatingBottomBar.kt`, `liquid/{CombinedBackdrop,InnerShadow,Lens,Vibrancy}.kt`, `miuix/animation/{DampedDragAnimation,InteractiveHighlight}.kt` and `miuix/modifier/DragGestureInspector.kt` were ported from the user's local 123PanX / LeiFetch implementation. Package names were adapted for this template; the original attribution headers remain in each file.

The source chain runs through LeiFetch / XBlocker to SukiSU-Ultra v4.1.3 (`0ca744a`), GPL-3.0. The underlying liquid glass examples are derived from compose-miuix-ui/miuix and Kyant0/AndroidLiquidGlass, Apache-2.0. Original copyrights and licenses continue to apply; the combined template remains GPL-3.0-only.

- LeiFetch: https://github.com/bileizhen/LeiFetch
- SukiSU-Ultra: https://github.com/SukiSU-Ultra/SukiSU-Ultra/tree/v4.1.3
- MIUIX examples: https://github.com/compose-miuix-ui/miuix
- Liquid glass helpers: https://github.com/Kyant0/AndroidLiquidGlass

The quantized sensor-light adapter was adapted from 123PanX's `ui/util/TiltLightDirection.kt`, GPL-3.0. It avoids recomposition for insignificant sensor changes.

The Gradle Wrapper is distributed under Apache-2.0. Its generated launch scripts retain their upstream copyright notices.

Full GPL-3.0 and Apache-2.0 texts are packaged in `app/src/main/assets/legal/` and accessible offline from the About page. APK recipients can obtain the corresponding application source and build instructions from the configured GitHub project link.
