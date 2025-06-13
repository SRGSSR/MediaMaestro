/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Looper
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import android.support.v4.media.session.PlaybackStateCompat.STATE_BUFFERING
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_A
import android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import android.view.KeyEvent.KEYCODE_VOLUME_UP
import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.testing.ViewModelScenario
import androidx.lifecycle.viewmodel.testing.viewModelScenario
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.testing.MediaRouterTestHelper
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import ch.srgssr.media.maestro.MediaRouteControllerDialogViewModel.RouteDetail
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.DEFAULT_ROUTE_NAME
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_CONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_GROUP
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_PRESENTATION
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_NAME_CONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.findRouteById
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(ParameterizedRobolectricTestRunner::class)
class MediaRouteControllerDialogViewModelTest(
    private val volumeControlEnabled: Boolean,
) {
    private lateinit var context: Application
    private lateinit var router: MediaRouter
    private lateinit var viewModelScenario: ViewModelScenario<MediaRouteControllerDialogViewModel>

    private val viewModel: MediaRouteControllerDialogViewModel
        get() = viewModelScenario.viewModel

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        router = MediaRouter.getInstance(context)
        router.addProvider(TestMediaRouteProvider(context))
        router.findRouteById(ROUTE_ID_CONNECTED).select()

        viewModelScenario = viewModelScenario {
            MediaRouteControllerDialogViewModel(context, SavedStateHandle(), volumeControlEnabled)
        }
    }

    @AfterTest
    fun after() {
        viewModelScenario.close()

        MediaRouterTestHelper.resetMediaRouter()
    }

    @Test
    fun `check the default values`() = runTest {
        viewModel.showDialog.test {
            assertTrue(awaitItem())
        }

        viewModel.isDeviceGroupExpanded.test {
            assertFalse(awaitItem())
        }

        viewModel.showPlaybackControl.test {
            assertFalse(awaitItem())
        }

        viewModel.showVolumeControl.test {
            assertEquals(volumeControlEnabled, awaitItem())
        }

        viewModel.imageModel.test {
            assertNull(awaitItem())
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_media_selected), awaitItem())
        }

        viewModel.subtitle.test {
            assertNull(awaitItem())
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }

        viewModel.routes.test {
            val route = router.selectedRoute
            val selectedRouteDetail = RouteDetail(
                route = route,
                volume = getVolume(route),
                volumeRange = getVolumeRange(route),
            )

            assertEquals(listOf(selectedRouteDetail), awaitItem())
        }
    }

    @Test
    fun `check show playback control with media description`() = runTest {
        viewModel.mediaDescription.update { MediaDescriptionCompat.Builder().build() }

        viewModel.showPlaybackControl.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `check show playback control with playback state`() = runTest {
        viewModel.playbackState.update { PlaybackStateCompat.Builder().build() }

        viewModel.showPlaybackControl.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `check image model with null icon bitmap`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setIconBitmap(null)
                .build()
        }

        viewModel.imageModel.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check image model with recycled icon bitmap`() = runTest {
        val bitmap = BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)
        bitmap.recycle()

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setIconBitmap(bitmap)
                .build()
        }

        viewModel.imageModel.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check image model with icon bitmap`() = runTest {
        val bitmap = BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setIconBitmap(bitmap)
                .build()
        }

        viewModel.imageModel.test {
            assertEquals(bitmap, awaitItem())
        }
    }

    @Test
    fun `check image model with icon URI`() = runTest {
        val iconUri = "https://example.com/icon.png".toUri()

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setIconUri(iconUri)
                .build()
        }

        viewModel.imageModel.test {
            assertEquals(iconUri, awaitItem())
        }
    }

    @Test
    fun `check image model with icon bitmap and icon URI`() = runTest {
        val bitmap = BitmapFactory.decodeByteArray(byteArrayOf(), 0, 0)
        val iconUri = "https://example.com/icon.png".toUri()

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setIconBitmap(bitmap)
                .setIconUri(iconUri)
                .build()
        }

        viewModel.imageModel.test {
            assertEquals(bitmap, awaitItem())
        }
    }

    @Test
    fun `check title with selected route has a presentation display id`() = runTest {
        router.findRouteById(ROUTE_ID_PRESENTATION).select()

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_casting_screen), awaitItem())
        }
    }

    @Test
    fun `check title with playback state being none`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_NONE, 0L, 0f)
                .build()
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_media_selected), awaitItem())
        }
    }

    @Test
    fun `check title with playback state different than none`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_info_available), awaitItem())
        }
    }

    @Test
    fun `check title with empty media description`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.mediaDescription.update { MediaDescriptionCompat.Builder().build() }

        viewModel.title.test {
            assertEquals(context.getString(R.string.mr_controller_no_info_available), awaitItem())
        }
    }

    @Test
    fun `check title with media description containing title only`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .build()
        }

        val title = "Title"

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setTitle(title)
                .build()
        }

        viewModel.title.test {
            assertEquals(title, awaitItem())
        }
    }

    @Test
    fun `check title with media description containing subtitle only`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .build()
        }

        val subtitle = "Subtitle"

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setSubtitle(subtitle)
                .build()
        }

        viewModel.title.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with empty media description`() = runTest {
        viewModel.mediaDescription.update { MediaDescriptionCompat.Builder().build() }

        viewModel.subtitle.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with null subtitle`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setSubtitle(null)
                .build()
        }

        viewModel.subtitle.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check subtitle with empty subtitle`() = runTest {
        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setSubtitle("")
                .build()
        }

        viewModel.subtitle.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `check subtitle with non-empty subtitle`() = runTest {
        val subtitle = "Subtitle"

        viewModel.mediaDescription.update {
            MediaDescriptionCompat.Builder()
                .setSubtitle(subtitle)
                .build()
        }

        viewModel.subtitle.test {
            assertEquals(subtitle, awaitItem())
        }
    }

    @Test
    fun `check icon info while buffering and no capabilities`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_BUFFERING, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while buffering and pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_BUFFERING, 0L, 0f)
                .setActions(ACTION_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while buffering and play pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_BUFFERING, 0L, 0f)
                .setActions(ACTION_PLAY_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while buffering and stop supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_BUFFERING, 0L, 0f)
                .setActions(ACTION_STOP)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Stop, context.getString(R.string.mr_controller_stop)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while playing and no capabilities`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while playing and pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .setActions(ACTION_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while playing and play pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .setActions(ACTION_PLAY_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Pause, context.getString(R.string.mr_controller_pause)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while playing and stop supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PLAYING, 0L, 0f)
                .setActions(ACTION_STOP)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.Stop, context.getString(R.string.mr_controller_stop)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while paused and no capabilities`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PAUSED, 0L, 0f)
                .build()
        }

        viewModel.iconInfo.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `check icon info while paused and play supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PAUSED, 0L, 0f)
                .setActions(ACTION_PLAY)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.PlayArrow, context.getString(R.string.mr_controller_play)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `check icon info while paused and play pause supported`() = runTest {
        viewModel.playbackState.update {
            PlaybackStateCompat.Builder()
                .setState(STATE_PAUSED, 0L, 0f)
                .setActions(ACTION_PLAY_PAUSE)
                .build()
        }

        viewModel.iconInfo.test {
            assertEquals(
                expected = Pair(Icons.PlayArrow, context.getString(R.string.mr_controller_play)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `hide dialog`() = runTest {
        viewModel.showDialog.test {
            assertTrue(awaitItem())

            viewModel.hideDialog()

            assertFalse(awaitItem())
        }
    }

    @Test
    fun `toggle device group`() = runTest {
        viewModel.isDeviceGroupExpanded.test {
            assertFalse(awaitItem())

            viewModel.toggleDeviceGroup()

            assertTrue(awaitItem())

            viewModel.toggleDeviceGroup()

            assertFalse(awaitItem())
        }
    }

    @Test
    fun `stop casting`() = runTest {
        viewModel.showDialog.test {
            assertEquals(ROUTE_NAME_CONNECTED, router.selectedRoute.name)
            assertTrue(awaitItem())

            viewModel.stopCasting()

            assertEquals(DEFAULT_ROUTE_NAME, router.selectedRoute.name)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun disconnect() = runTest {
        viewModel.showDialog.test {
            assertEquals(ROUTE_NAME_CONNECTED, router.selectedRoute.name)
            assertTrue(awaitItem())

            viewModel.disconnect()

            assertEquals(DEFAULT_ROUTE_NAME, router.selectedRoute.name)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `check that selecting the default route hides the dialog`() = runTest {
        viewModel.showDialog.test {
            assertEquals(ROUTE_NAME_CONNECTED, router.selectedRoute.name)
            assertTrue(awaitItem())

            router.routes[0].select()

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(DEFAULT_ROUTE_NAME, router.selectedRoute.name)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `check that unselecting the a route hides the dialog`() = runTest {
        viewModel.showDialog.test {
            assertEquals(ROUTE_NAME_CONNECTED, router.selectedRoute.name)
            assertTrue(awaitItem())

            router.unselect(MediaRouter.UNSELECT_REASON_DISCONNECTED)

            shadowOf(Looper.getMainLooper()).idle()

            assertEquals(DEFAULT_ROUTE_NAME, router.selectedRoute.name)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `on key event, volume down`() = runTest {
        val keyEvent = KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_VOLUME_DOWN))

        assertTrue(viewModel.onKeyEvent(keyEvent))
    }

    @Test
    fun `on key event, volume up`() {
        val keyEvent = KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_VOLUME_UP))

        assertTrue(viewModel.onKeyEvent(keyEvent))
    }

    @Test
    fun `on key event, non-volume key`() {
        val keyEvent = KeyEvent(NativeKeyEvent(ACTION_DOWN, KEYCODE_A))

        assertFalse(viewModel.onKeyEvent(keyEvent))
    }

    @Test
    fun `selecting a group of devices provides the corresponding volumes`() = runTest {
        router.findRouteById(ROUTE_ID_GROUP).select()

        viewModel.routes.test {
            val routes = router.selectedRoute.selectedRoutesInGroup
            val expected = listOf(getGroupRouteDetail()) + routes.map { route ->
                RouteDetail(
                    route = route,
                    volume = getVolume(route),
                    volumeRange = getVolumeRange(route),
                )
            }

            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `set route volume on a new route of a group`() = runTest {
        router.findRouteById(ROUTE_ID_GROUP).select()

        val routes = router.selectedRoute.selectedRoutesInGroup
        val targetVolume = 10f
        val routeToUpdate = routes[0]
        val expected = listOf(getGroupRouteDetail()) + routes.map { route ->
            RouteDetail(
                route = route,
                volume = if (volumeControlEnabled && route.id == routeToUpdate.id) {
                    targetVolume
                } else {
                    getVolume(route)
                },
                volumeRange = getVolumeRange(route),
            )
        }

        viewModel.setRouteVolume(routeToUpdate, targetVolume)

        viewModel.routes.test {
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `set route volume on an updated route of a group`() = runTest {
        router.findRouteById(ROUTE_ID_GROUP).select()

        val routes = router.selectedRoute.selectedRoutesInGroup
        val targetVolume = 10f
        val routeToUpdate = routes[0]
        val expected = listOf(getGroupRouteDetail()) + routes.map { route ->
            RouteDetail(
                route = route,
                volume = if (volumeControlEnabled && route.id == routeToUpdate.id) {
                    targetVolume
                } else {
                    getVolume(route)
                },
                volumeRange = getVolumeRange(route),
            )
        }

        viewModel.setRouteVolume(routeToUpdate, volume = 5f)
        viewModel.setRouteVolume(routeToUpdate, targetVolume)

        viewModel.routes.test {
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `set route volume on a route outside of a group`() = runTest {
        router.findRouteById(ROUTE_ID_GROUP).select()

        val routes = router.selectedRoute.selectedRoutesInGroup
        val routeToUpdate = router.findRouteById(ROUTE_ID_PRESENTATION)
        val expected = listOf(getGroupRouteDetail()) + routes.map { route ->
            RouteDetail(
                route = route,
                volume = getVolume(route),
                volumeRange = getVolumeRange(route),
            )
        }

        viewModel.setRouteVolume(routeToUpdate, volume = 10f)

        viewModel.routes.test {
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `set route volume on a group`() = runTest {
        val routeToUpdate = router.findRouteById(ROUTE_ID_GROUP)

        routeToUpdate.select()

        val targetVolume = 10f
        val routes = router.selectedRoute.selectedRoutesInGroup
        val groupRouteDetail = if (volumeControlEnabled) {
            getGroupRouteDetail().copy(volume = targetVolume)
        } else {
            getGroupRouteDetail()
        }
        val expected = listOf(groupRouteDetail) + routes.map { route ->
            RouteDetail(
                route = route,
                volume = getVolume(route),
                volumeRange = getVolumeRange(route),
            )
        }

        viewModel.setRouteVolume(routeToUpdate, targetVolume)

        viewModel.routes.test {
            assertEquals(expected, awaitItem())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `ViewModel factory fails to create a ViewModel without a Context`() {
        MediaRouteControllerDialogViewModel.Factory(volumeControlEnabled = true)
            .create(ViewModel::class.java, CreationExtras.Empty)
    }

    @Test
    fun `ViewModel factory creates an instance of MediaRouteControllerDialogViewModel`() {
        Robolectric.buildActivity(ComponentActivity::class.java)
            .use { activityController ->
                val viewModel = ViewModelProvider(
                    owner = activityController.setup().get(),
                    factory = MediaRouteControllerDialogViewModel.Factory(volumeControlEnabled = true),
                ).get<ViewModel>()

                assertIs<MediaRouteControllerDialogViewModel>(viewModel)
            }
    }

    @Test
    fun `RouteDetail with non-group has no member`() {
        val routeDetail = RouteDetail(
            route = router.findRouteById(ROUTE_ID_CONNECTED),
            volume = 0f,
            volumeRange = 0f..100f,
        )

        assertFalse(routeDetail.hasMembers)
    }

    @Test
    fun `RouteDetail with group has member`() {
        val routeDetail = RouteDetail(
            route = router.findRouteById(ROUTE_ID_GROUP),
            volume = 0f,
            volumeRange = 0f..100f,
        )

        assertTrue(routeDetail.hasMembers)
    }

    private fun getGroupRouteDetail(): RouteDetail {
        val route = router.findRouteById(ROUTE_ID_GROUP)

        return RouteDetail(
            route = route,
            volume = getVolume(route),
            volumeRange = getVolumeRange(route),
        )
    }

    private fun getVolume(route: MediaRouter.RouteInfo): Float {
        return if (volumeControlEnabled) route.volume.toFloat() else 100f
    }

    private fun getVolumeRange(route: MediaRouter.RouteInfo): ClosedFloatingPointRange<Float> {
        return if (volumeControlEnabled) 0f..route.volumeMax.toFloat() else 0f..100f
    }

    companion object {
        @JvmStatic
        @Parameters(name = "volumeControlEnabled = {0}")
        fun parameters(): List<Boolean> {
            return listOf(true, false)
        }
    }
}
