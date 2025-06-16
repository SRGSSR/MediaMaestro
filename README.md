# ![MediaMaestro](.idea/icon.svg) MediaMaestro

[![Last release](https://img.shields.io/github/v/release/SRGSSR/MediaMaestro?label=Release)](https://github.com/SRGSSR/MediaMaestro/releases)
[![Android min SDK](https://img.shields.io/badge/Android-21%2B-34A853)](https://github.com/SRGSSR/MediaMaestro)
[![Build status](https://img.shields.io/github/actions/workflow/status/SRGSSR/MediaMaestro/quality.yml?label=Build)](https://github.com/SRGSSR/MediaMaestro/actions/workflows/quality.yml)
[![License](https://img.shields.io/github/license/SRGSSR/MediaMaestro?label=License)](https://github.com/SRGSSR/MediaMaestro/blob/main/LICENSE)

Simplify media routing in your [Compose][compose] app with this native Material
3 [MediaRouter][androidx-mediarouter] library. Enjoy easy integration, a pure Compose-friendly
approach, and no need for `AppCompatActivity` or `Theme.AppCompat.*`. Focus on what matters:
creating seamless media experiences for your users.

## Getting started

1. Add the GitHub Packages repository:

```kotlin
// settings.gradle.kts
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/SRGSSR/MediaMaestro")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
        content {
            includeGroup("ch.srgssr.media.maestro")
        }
    }
}
```

2. Add the dependency:

```kotlin
// <module>/build.gradle.kts
implementation("ch.srgssr.media.maestro:media-maestro:<version>")
```

3. Add the Media Router button:

```kotlin
// In your @Composable layout
MediaRouteButton()
```

And you're good to go! If you need more customization, you can check
the [documentation][media-maestro-doc].

## Release

New versions are automatically published to GitHub Packages whenever a new tag following the `x.y.z`
pattern is pushed.

## License

This library is provided under the MIT License. Please refer to the [license][license] file for
detailed information.

[androidx-mediarouter]: https://developer.android.com/media/routing/mediarouter
[compose]: https://developer.android.com/compose
[license]: https://github.com/SRGSSR/MediaMaestro/blob/main/LICENSE
[media-maestro-doc]: https://srgssr.github.io/MediaMaestro
[new-issue]: https://github.com/SRGSSR/MediaMaestro/issues/new/choose
