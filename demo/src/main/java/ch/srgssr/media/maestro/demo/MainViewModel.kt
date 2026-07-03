/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro.demo

import android.app.Application
import android.media.MediaMetadata.METADATA_KEY_ART_URI
import android.media.MediaMetadata.METADATA_KEY_TITLE
import android.media.session.MediaSession
import android.media.session.PlaybackState
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.cast.CastPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.framework.CastContext
import android.media.MediaMetadata as PlatformMediaMetadata

@OptIn(UnstableApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val playerListener = PlayerListener()
    private val castContext = CastContext.getSharedInstance(application)
    private val mediaSession = MediaSession(application, "media-maestro-demo")

    val player: CastPlayer = CastPlayer.Builder(application).build()

    val routeSelector = castContext.mergedSelector ?: MediaRouteSelector.Builder()
        .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
        .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
        .build()

    init {
        MediaRouter.getInstance(application).setMediaSession(mediaSession)

        player.addListener(playerListener)
        player.setMediaItems(listOf(mediaItemAudio, mediaItemVideo))
        player.prepare()
        player.play()
    }

    override fun onCleared() {
        mediaSession.release()
        player.removeListener(playerListener)
        player.release()
    }

    private inner class PlayerListener : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            mediaSession.setMetadata(
                PlatformMediaMetadata.Builder()
                    .putText(METADATA_KEY_TITLE, mediaMetadata.title)
                    .putText(METADATA_KEY_ART_URI, mediaMetadata.artworkUri.toString())
                    .build()
            )
        }

        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_IDLE -> PlaybackState.STATE_CONNECTING
                Player.STATE_BUFFERING -> PlaybackState.STATE_BUFFERING
                Player.STATE_READY -> PlaybackState.STATE_PLAYING
                Player.STATE_ENDED -> PlaybackState.STATE_STOPPED
                else -> PlaybackState.STATE_NONE
            }
            val position = player.currentPosition
            val playbackSpeed = player.playbackParameters.speed
            val actions = listOfNotNull(
                PlaybackState.ACTION_PAUSE.takeIf { player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) },
                PlaybackState.ACTION_PLAY.takeIf { player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) },
                PlaybackState.ACTION_PLAY_PAUSE.takeIf { player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) },
                PlaybackState.ACTION_STOP.takeIf { player.isCommandAvailable(Player.COMMAND_STOP) },
            ).fold(0L) { actions, action ->
                actions or action
            }

            mediaSession.setPlaybackState(
                PlaybackState.Builder()
                    .setState(state, position, playbackSpeed)
                    .setActions(actions)
                    .build()
            )
        }
    }

    private companion object {
        @Suppress("MaxLineLength")
        private val mediaItemAudio = MediaItem.Builder()
            .setUri("https://rts-aod-dd.akamaized.net/ww/13306839/63cc2653-8305-3894-a448-108810b553ef.mp3")
            .setMimeType(MimeTypes.AUDIO_MP4)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("On en parle")
                    .setArtworkUri("https://www.rts.ch/2023/09/28/17/49/11872957.image?w=624&h=351".toUri())
                    .build()
            )
            .build()

        @Suppress("MaxLineLength")
        private val mediaItemVideo = MediaItem.Builder()
            .setUri("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd")
            .setMimeType(MimeTypes.APPLICATION_MPD)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("Tears of Steel (DASH, adaptive, HD, MP4, H264/aac)")
                    .setArtworkUri("https://mango.blender.org/wp-content/gallery/4k-renders/01_thom_celia_bridge.jpg".toUri())
                    .build()
            )
            .build()
    }
}
