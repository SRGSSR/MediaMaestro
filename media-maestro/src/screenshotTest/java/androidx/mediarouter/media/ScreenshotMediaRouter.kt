/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package androidx.mediarouter.media

import android.content.Context
import java.util.UUID

/**
 * The regular [MediaRouter] can't be used in screenshot tests, because it relies on the
 * [android.media.MediaRouter] system service, which is not available.
 *
 * Instead, we can use this class, which mimic its behavior. This allows us to access a list of
 * [routes][MediaRouter.RouteInfo]. Note that this doesn't support device group at the moment.
 *
 * **Example usages**
 *
 * ```kotlin
 * // Using the default ScreenshotMediaRouteProvider
 * val router = ScreenshotMediaRouter(context)
 * // Or with a custom MediaRouteProvider
 * val router = ScreenshotMediaRouter(MyMediaRouteProvider())
 *
 * val routes = router.routes
 * ```
 */
class ScreenshotMediaRouter(private val provider: MediaRouteProvider) {
    constructor(context: Context) : this(ScreenshotMediaRouteProvider(context))

    val routes = provider.descriptor
        ?.routes.orEmpty()
        .map { routeDescriptor ->
            val provider = MediaRouter.ProviderInfo(provider, false)
            val descriptorId = routeDescriptor.id
            val uniqueId = UUID.randomUUID().toString()
            val isSystemRoute = routeDescriptor.isSystemRoute

            val routeInfo = MediaRouter.RouteInfo(provider, descriptorId, uniqueId, isSystemRoute)
            routeInfo.maybeUpdateDescriptor(routeDescriptor)
            routeInfo
        }
}
