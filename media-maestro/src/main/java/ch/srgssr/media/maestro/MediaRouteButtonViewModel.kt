/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.mediarouter.app.SystemOutputSwitcherDialogController
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import androidx.mediarouter.media.MediaRouterParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * The type of dialog to show.
 */
public enum class DialogType {
    /**
     * Show the chooser dialog to select a route to connect to.
     */
    Chooser,

    /**
     * Show the dynamic chooser dialog to select a route to connect to.
     */
    DynamicChooser,

    /**
     * Show the controller dialog for the currently selected route.
     */
    Controller,

    /**
     * Show the dynamic controller dialog for the currently selected route.
     */
    DynamicController,

    /**
     * No dialog should be shown.
     */
    None,
}

/**
 * [ViewModel] exposing useful information for building [MediaRouteButton].
 *
 * @param application The [Application] instance.
 * @param savedStateHandle The [SavedStateHandle] instance.
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 *
 * @see MediaRouteChooserDialogViewModel.Factory
 */
internal class MediaRouteButtonViewModel(
    application: Context,
    private val savedStateHandle: SavedStateHandle,
    private val routeSelector: MediaRouteSelector,
) : ViewModel() {
    private val mediaRouterCallback = MediaRouterCallback()
    private val router = MediaRouter.getInstance(application)

    private val _fixedIcon = MutableStateFlow(false)
    private val routerUpdates = MutableStateFlow(0)
    private val showDialog = savedStateHandle.getStateFlow(KEY_SHOW_DIALOG, false)

    /**
     * The [CastConnectionState] for the currently selected route.
     */
    val castConnectionState = routerUpdates
        .map { router.selectedRoute }
        .map { route ->
            if (route.isDefaultOrBluetooth) {
                CastConnectionState.Disconnected
            } else {
                when (val connectionState = route.connectionState) {
                    RouteInfo.CONNECTION_STATE_CONNECTED -> CastConnectionState.Connected
                    RouteInfo.CONNECTION_STATE_CONNECTING -> CastConnectionState.Connecting
                    RouteInfo.CONNECTION_STATE_DISCONNECTED -> CastConnectionState.Disconnected
                    else -> error("Unknown connection state: $connectionState")
                }
            }
        }
        .stateIn(viewModelScope, WhileSubscribed(), CastConnectionState.Disconnected)

    /**
     * The type of dialog to show.
     */
    val dialogType = combine(showDialog, routerUpdates) { showDialog, _ ->
        if (!showDialog) {
            return@combine DialogType.None
        }

        val routerParams = router.routerParams
        if (routerParams != null) {
            if (routerParams.isOutputSwitcherEnabled && MediaRouter.isMediaTransferEnabled()) {
                if (SystemOutputSwitcherDialogController.showDialog(application)) {
                    return@combine DialogType.None
                }
            }
        }

        val dynamicGroup = routerParams?.dialogType == MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP
        if (router.selectedRoute.isDefaultOrBluetooth) {
            if (dynamicGroup) {
                DialogType.DynamicChooser
            } else {
                DialogType.Chooser
            }
        } else {
            if (dynamicGroup) {
                DialogType.DynamicController
            } else {
                DialogType.Controller
            }
        }
    }.distinctUntilChanged()

    /**
     * `true` to use a static Cast icon, `false` to use a dynamic icon.
     */
    val fixedIcon = _fixedIcon.asStateFlow()

    init {
        if (!routeSelector.isEmpty) {
            router.addCallback(routeSelector, mediaRouterCallback)
        }
    }

    /**
     * Show the dialog.
     */
    fun showDialog() {
        savedStateHandle[KEY_SHOW_DIALOG] = true
    }

    /**
     * Hide the dialog.
     */
    fun hideDialog() {
        savedStateHandle[KEY_SHOW_DIALOG] = false
    }

    override fun onCleared() {
        if (!routeSelector.isEmpty) {
            router.removeCallback(mediaRouterCallback)
        }
    }

    private companion object {
        private const val KEY_SHOW_DIALOG = "showDialog"
    }

    /**
     * Factory for [MediaRouteButtonViewModel].
     *
     * @param routeSelector  The media route selector for filtering the routes that the user can
     * select using the media route chooser dialog.
     */
    class Factory(private val routeSelector: MediaRouteSelector, private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return MediaRouteButtonViewModel(context, savedStateHandle, routeSelector) as T
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

        override fun onRouteSelected(router: MediaRouter, route: RouteInfo, reason: Int) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) {
            routerUpdates.update { it + 1 }
        }

        override fun onProviderAdded(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onProviderRemoved(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onProviderChanged(router: MediaRouter, provider: MediaRouter.ProviderInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouterParamsChanged(router: MediaRouter, params: MediaRouterParams?) {
            _fixedIcon.update {
                params?.extras?.getBoolean(MediaRouterParams.EXTRAS_KEY_FIXED_CAST_ICON) == true
            }
        }
    }
}
