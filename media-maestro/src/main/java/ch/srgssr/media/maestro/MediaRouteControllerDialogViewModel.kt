/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.app.Application
import android.app.PendingIntent
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class MediaRouteControllerDialogViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val volumeControlEnabled: Boolean,
) : AndroidViewModel(application) {
    internal data class RouteDetail(
        val route: RouteInfo,
        val volume: Float,
        val volumeRange: ClosedFloatingPointRange<Float>,
    ) {
        val hasMembers: Boolean
            get() = route.isGroup && route.selectedRoutesInGroup.size > 1
    }

    private var mediaController: MediaControllerCompat? = null

    private val mediaControllerCallback = MediaControllerCallback()
    private val mediaRouterCallback = MediaRouterCallback()
    private val router = MediaRouter.getInstance(application)
    private val isGroupVolumeUxEnabled = MediaRouter.isGroupVolumeUxEnabled()

    private val routerUpdates = MutableStateFlow(0)
    private val volumes = MutableStateFlow(emptyMap<RouteInfo, Float>())

    @VisibleForTesting
    internal val mediaDescription = MutableStateFlow<MediaDescriptionCompat?>(null)

    @VisibleForTesting
    internal val playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    private val _isDeviceGroupExpanded = MutableStateFlow(false)
    private val _selectedRoute = routerUpdates.map { router.selectedRoute }

    val showDialog = savedStateHandle.getStateFlow(KEY_SHOW_DIALOG, true)
    val isDeviceGroupExpanded: StateFlow<Boolean> = _isDeviceGroupExpanded.asStateFlow()

    val showPlaybackControl =
        combine(mediaDescription, playbackState) { mediaDescription, playbackState ->
            mediaDescription != null || playbackState != null
        }.stateIn(viewModelScope, WhileSubscribed(), false)
    val showVolumeControl = _selectedRoute.map { selectedRoute ->
        if (!isGroupVolumeUxEnabled && selectedRoute.isGroup && selectedRoute.selectedRoutesInGroup.size > 1) {
            _isDeviceGroupExpanded.update { true }
            false
        } else {
            selectedRoute.isVolumeControlEnabled
                    && (!_isDeviceGroupExpanded.value || isGroupVolumeUxEnabled)
        }
    }.stateIn(viewModelScope, WhileSubscribed(), false)
    val imageModel = mediaDescription.map { mediaDescription ->
        mediaDescription?.iconBitmap?.takeIf { !it.isRecycled } ?: mediaDescription?.iconUri
    }.stateIn(viewModelScope, WhileSubscribed(), null)

    val title = combine(
        mediaDescription,
        playbackState,
        _selectedRoute,
    ) { mediaDescription, playbackState, selectedRoute ->
        if (selectedRoute.presentationDisplayId != RouteInfo.PRESENTATION_DISPLAY_ID_NONE) {
            application.getString(R.string.mr_controller_casting_screen)
        } else if (playbackState == null || playbackState.state == STATE_NONE) {
            application.getString(R.string.mr_controller_no_media_selected)
        } else if (mediaDescription?.title.isNullOrEmpty() && mediaDescription?.subtitle.isNullOrEmpty()) {
            application.getString(R.string.mr_controller_no_info_available)
        } else {
            mediaDescription.title?.toString()
        }
    }.stateIn(viewModelScope, WhileSubscribed(), null)
    val subtitle = mediaDescription.map { it?.subtitle?.toString() }
        .stateIn(viewModelScope, WhileSubscribed(), null)
    val iconInfo = playbackState.map { playbackState ->
        if (playbackState == null) {
            return@map null
        }

        val isPlaying = playbackState.state == STATE_BUFFERING
                || playbackState.state == STATE_PLAYING

        val icon: ImageVector
        val contentDescription: String
        if (isPlaying && playbackState.isPauseActionSupported) {
            icon = Icons.Pause
            contentDescription = application.getString(R.string.mr_controller_pause)
        } else if (isPlaying && playbackState.isStopActionSupported) {
            icon = Icons.Stop
            contentDescription = application.getString(R.string.mr_controller_stop)
        } else if (!isPlaying && playbackState.isPlayActionSupported) {
            icon = Icons.PlayArrow
            contentDescription = application.getString(R.string.mr_controller_play)
        } else {
            return@map null
        }

        icon to contentDescription
    }.stateIn(viewModelScope, WhileSubscribed(), null)

    /**
     * Contains the list of routes to manage in the controller. The first item of the list is always
     * the selected route, while any following routes are member routes of the selected one.
     * This list is never empty.
     */
    val routes = combine(_selectedRoute, volumes) { selectedRoute, volumes ->
        createRouteDetails(selectedRoute)
    }.stateIn(viewModelScope, WhileSubscribed(), createRouteDetails(router.selectedRoute))

    init {
        router.addCallback(
            MediaRouteSelector.EMPTY,
            mediaRouterCallback,
            MediaRouter.CALLBACK_FLAG_UNFILTERED_EVENTS,
        )

        router.mediaSessionToken?.let { mediaSessionToken ->
            val mediaController = MediaControllerCompat(application, mediaSessionToken)
            mediaController.registerCallback(mediaControllerCallback)

            this.mediaController = mediaController
            mediaDescription.update { mediaController.metadata?.description }
            playbackState.update { mediaController.playbackState }
        }

        viewModelScope.launch {
            _selectedRoute.collect {
                if (!it.isSelected || it.isDefaultOrBluetooth) {
                    hideDialog()
                }
            }
        }

        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            volumes.debounce(VOLUME_UPDATE_DELAY)
                .collect {
                    it.forEach { (route, volume) ->
                        if (route.isVolumeControlEnabled && route.volume != volume.toInt()) {
                            route.requestSetVolume(volume.toInt())
                        }
                    }
                }
        }
    }

    fun hideDialog() {
        savedStateHandle[KEY_SHOW_DIALOG] = false
    }

    fun toggleDeviceGroup() {
        _isDeviceGroupExpanded.update { !it }
    }

    fun stopCasting() {
        if (router.selectedRoute.isSelected) {
            router.unselect(MediaRouter.UNSELECT_REASON_STOPPED)
        }

        hideDialog()
    }

    fun disconnect() {
        if (router.selectedRoute.isSelected) {
            router.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED)
        }

        hideDialog()
    }

    fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        return if (keyEvent.key == Key.VolumeDown || keyEvent.key == Key.VolumeUp) {
            val isDeviceGroupExpanded = _isDeviceGroupExpanded.value
            if (keyEvent.type == KeyEventType.KeyDown && (isGroupVolumeUxEnabled || !isDeviceGroupExpanded)) {
                val delta = if (keyEvent.key == Key.VolumeDown) -1 else 1

                router.selectedRoute.requestUpdateVolume(delta)
            }

            true
        } else {
            false
        }
    }

    fun startSessionActivity() {
        mediaController?.sessionActivity?.let { pendingIntent ->
            try {
                pendingIntent.send()
                hideDialog()
            } catch (exception: PendingIntent.CanceledException) {
                Log.d(
                    "MediaRouteController",
                    "$pendingIntent was not sent, it has been canceled.",
                    exception,
                )
            }
        }
    }

    fun onPlaybackIconClick() {
        val mediaController = mediaController ?: return
        val playbackState = playbackState.value ?: return
        val isPlaying = playbackState.state == STATE_PLAYING

        if (isPlaying && playbackState.isPauseActionSupported) {
            mediaController.transportControls.pause()
        } else if (isPlaying && playbackState.isStopActionSupported) {
            mediaController.transportControls.stop()
        } else if (!isPlaying && playbackState.isPlayActionSupported) {
            mediaController.transportControls.play()
        }
    }

    fun setRouteVolume(route: RouteInfo, volume: Float) {
        if (route.isVolumeControlEnabled) {
            volumes.update {
                it.toMutableMap()
                    .apply { set(route, volume) }
            }
        }
    }

    override fun onCleared() {
        router.removeCallback(mediaRouterCallback)
        mediaControllerCallback.onSessionDestroyed()
    }

    private val PlaybackStateCompat.isPlayActionSupported: Boolean
        get() = actions and (ACTION_PLAY or ACTION_PLAY_PAUSE) != 0L

    private val PlaybackStateCompat.isPauseActionSupported: Boolean
        get() = actions and (ACTION_PAUSE or ACTION_PLAY_PAUSE) != 0L

    private val PlaybackStateCompat.isStopActionSupported: Boolean
        get() = actions and ACTION_STOP != 0L

    private val RouteInfo.isVolumeControlEnabled: Boolean
        get() = volumeControlEnabled && volumeHandling == RouteInfo.PLAYBACK_VOLUME_VARIABLE

    private fun createRouteDetails(selectedRoute: RouteInfo): List<RouteDetail> {
        val volumes = volumes.value

        return (listOf(selectedRoute) + selectedRoute.selectedRoutesInGroup)
            .map { route ->
                val isVolumeControlEnabled = route.isVolumeControlEnabled
                val volumeMax = if (isVolumeControlEnabled) {
                    route.volumeMax.toFloat()
                } else {
                    DEFAULT_MAX_VOLUME
                }
                val volume = if (isVolumeControlEnabled) {
                    volumes[route] ?: route.volume.toFloat()
                } else {
                    DEFAULT_MAX_VOLUME
                }

                RouteDetail(
                    route = route,
                    volume = volume,
                    volumeRange = 0f..volumeMax,
                )
            }
    }

    private companion object {
        private const val DEFAULT_MAX_VOLUME = 100f
        private const val KEY_SHOW_DIALOG = "showDialog"
        private val VOLUME_UPDATE_DELAY = 500.milliseconds
    }

    class Factory(private val volumeControlEnabled: Boolean) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[APPLICATION_KEY])
            val savedStateHandle = extras.createSavedStateHandle()

            @Suppress("UNCHECKED_CAST")
            return MediaRouteControllerDialogViewModel(
                application = application,
                savedStateHandle = savedStateHandle,
                volumeControlEnabled = volumeControlEnabled,
            ) as T
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onSessionDestroyed() {
            mediaController?.unregisterCallback(this)
            mediaController = null
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.update { state }
            routerUpdates.update { it + 1 }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaDescription.update { metadata?.description }
            routerUpdates.update { it + 1 }
        }
    }

    private inner class MediaRouterCallback : MediaRouter.Callback() {
        override fun onRouteUnselected(router: MediaRouter, route: RouteInfo, reason: Int) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteChanged(router: MediaRouter, route: RouteInfo) {
            routerUpdates.update { it + 1 }
        }

        override fun onRouteVolumeChanged(router: MediaRouter, route: RouteInfo) {
            volumes.update {
                it.toMutableMap()
                    .apply { remove(route) }
            }
        }
    }
}
