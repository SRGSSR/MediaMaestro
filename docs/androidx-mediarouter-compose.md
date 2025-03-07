## AndroidX MediaRouter Compose

Simplify media routing in your [Compose][compose] app with this native Material
3 [MediaRouter][androidx-mediarouter] library. Enjoy easy integration, a pure Compose-friendly
approach, and no need for `AppCompatActivity` or `Theme.AppCompat.*`. Focus on what matters:
creating seamless media experiences for your users.

### Add the dependency

This library is available as a [GitHub Package][github-package]. Follow
GitHub's [documentation][using-github-package] to add the repository to your project.

#### Set up the repository

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/SRGSSR/androidx-mediarouter-compose")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
        content {
            includeGroup("ch.srgssr.androidx.mediarouter")
        }
    }
}
```

#### Declaring the dependency

In your module `build.gradle(.kts)` file, add the following dependency:

```kotlin
implementation("ch.srgssr.androidx.mediarouter:mediarouter-compose:<version>")
```

[androidx-mediarouter]: https://developer.android.com/media/routing/mediarouter
[compose]: https://developer.android.com/compose
[github-package]: https://github.com/orgs/SRGSSR/packages?repo_name=androidx-mediarouter-compose
[using-github-package]: https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package
