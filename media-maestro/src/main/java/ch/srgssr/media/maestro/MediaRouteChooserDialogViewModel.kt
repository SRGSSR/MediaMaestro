/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.application
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

/**
 * [ViewModel] exposing useful information for building [MediaRouteChooserDialog].
 *
 * @param application The [Application] instance.
 * @param savedStateHandle The [SavedStateHandle] instance.
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 *
 * @see MediaRouteChooserDialogViewModel.Factory
 */
internal class MediaRouteChooserDialogViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val routeSelector: MediaRouteSelector,
) : AndroidViewModel(application) {
    /**
     * The state of the [MediaRouteChooserDialog].
     */
    internal enum class ChooserState {
        /**
         * The dialog is actively searching for devices.
         */
        FindingDevices,

        /**
         * No devices were found after an initial search, a hint might be displayed to turn on Wi-Fi.
         */
        NoDevicesNoWifiHint,

        /**
         * No routes matching the selector have been found.
         */
        NoRoutes,

        /**
         * Available routes are currently being shown.
         */
        ShowingRoutes;

        internal fun title(context: Context): String {
            val titleRes = when (this) {
                FindingDevices,
                NoDevicesNoWifiHint,
                ShowingRoutes -> R.string.mr_chooser_title

                NoRoutes -> R.string.mr_chooser_zero_routes_found_title
            }

            return context.getString(titleRes)
        }

        internal fun confirmLabel(context: Context): String? {
            return when (this) {
                FindingDevices,
                NoDevicesNoWifiHint,
                ShowingRoutes -> null

                NoRoutes -> context.getString(android.R.string.ok)
            }
        }
    }

    private val mediaRouterCallback = MediaRouterCallback()
    private val router = MediaRouter.getInstance(application)
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                hideDialog()
            }
        }
    }

    private val routerUpdates = MutableStateFlow(0)
    private val _routes = routerUpdates.map {
        router.routes
            .filter { route ->
                !route.isDefaultOrBluetooth &&
                        route.isEnabled &&
                        route.matchesSelector(routeSelector)
            }
            .sortedBy(RouteInfo::getName)
    }

    /**
     * Indicate if the dialog should be displayed or not.
     */
    val showDialog = savedStateHandle.getStateFlow(KEY_SHOW_DIALOG, true)

    /**
     * Filtered and sorted routes available for display.
     *
     * @see routeSelector
     */
    val routes = _routes.stateIn(viewModelScope, WhileSubscribed(), emptyList())

    /**
     * The state of the [MediaRouteChooserDialog].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val chooserState = _routes.transformLatest { routes ->
        if (routes.isEmpty()) {
            emit(ChooserState.FindingDevices)

            delay(5.seconds)

            emit(ChooserState.NoDevicesNoWifiHint)

            delay(15.seconds)

            emit(ChooserState.NoRoutes)

            router.removeCallback(mediaRouterCallback)
        } else {
            emit(ChooserState.ShowingRoutes)
        }
    }.stateIn(viewModelScope, WhileSubscribed(), ChooserState.FindingDevices)

    init {
        router.addCallback(
            routeSelector,
            mediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN,
        )

        application.registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
    }

    /**
     * Hide the dialog.
     */
    fun hideDialog() {
        savedStateHandle[KEY_SHOW_DIALOG] = false
    }

    override fun onCleared() {
        router.removeCallback(mediaRouterCallback)
        application.unregisterReceiver(screenOffReceiver)
    }

    private companion object {
        private const val KEY_SHOW_DIALOG = "showDialog"
    }

    /**
     * Factory for [MediaRouteChooserDialogViewModel].
     *
     * @param routeSelector  The media route selector for filtering the routes that the user can
     * select using the media route chooser dialog.
     */
    class Factory(private val routeSelector: MediaRouteSelector) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[APPLICATION_KEY])
            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return MediaRouteChooserDialogViewModel(
                application = application,
                savedStateHandle = savedStateHandle,
                routeSelector = routeSelector,
            ) as T
        }
    }

    private inner class MediaRouterCallback : MediaRouter.Callback() {
        override fun onRouteAdded(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteRemoved(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteSelected(
            router: MediaRouter,
            selectedRoute: RouteInfo,
            reason: Int,
            requestedRoute: RouteInfo,
        ) {
            hideDialog()
        }
    }
}
