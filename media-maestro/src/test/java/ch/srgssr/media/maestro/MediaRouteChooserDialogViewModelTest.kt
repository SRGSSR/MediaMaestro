/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.app.Application
import android.content.Intent
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.testing.ViewModelScenario
import androidx.lifecycle.viewmodel.testing.viewModelScenario
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteProvider
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.testing.MediaRouterTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import ch.srgssr.media.maestro.MediaRouteChooserDialogViewModel.ChooserState
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_CONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_NAME_CONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_NAME_DISCONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_NAME_GROUP
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_NAME_PRESENTATION
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.findRouteById
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
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

@RunWith(AndroidJUnit4::class)
class MediaRouteChooserDialogViewModelTest {
    private lateinit var context: Application
    private lateinit var provider: MediaRouteProvider
    private lateinit var router: MediaRouter
    private lateinit var viewModelScenario: ViewModelScenario<MediaRouteChooserDialogViewModel>

    private val viewModel: MediaRouteChooserDialogViewModel
        get() = viewModelScenario.viewModel

    @BeforeTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun before() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val routeSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        context = ApplicationProvider.getApplicationContext()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        provider = TestMediaRouteProvider(context)

        router = MediaRouter.getInstance(context)

        viewModelScenario = viewModelScenario {
            MediaRouteChooserDialogViewModel(context, SavedStateHandle(), routeSelector)
        }
    }

    @AfterTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun after() {
        viewModelScenario.close()

        MediaRouterTestHelper.resetMediaRouter()

        Dispatchers.resetMain()
    }

    @Test
    fun `check the default values`() = runTest {
        viewModel.showDialog.test {
            assertTrue(awaitItem())
        }

        viewModel.routes.test {
            assertEquals(emptyList(), awaitItem())
        }

        viewModel.chooserState.test {
            assertEquals(ChooserState.FindingDevices, awaitItem())
        }
    }

    @Test
    fun `hide dialog`() = runTest {
        router.addProvider(provider)

        viewModel.showDialog.test {
            assertTrue(awaitItem())

            viewModel.hideDialog()

            assertFalse(awaitItem())
        }
    }

    @Test
    fun `check that the route are filtered and sorted alphabetically`() = runTest {
        router.addProvider(provider)

        viewModel.routes.test {
            assertEquals(
                expected = listOf(
                    ROUTE_NAME_CONNECTED,
                    ROUTE_NAME_DISCONNECTED,
                    ROUTE_NAME_GROUP,
                    ROUTE_NAME_PRESENTATION,
                ),
                actual = awaitItem().map { it.name },
            )
        }
    }

    @Test
    fun `check that the chooser state moves to ShowingRoutes when there are routes available`() {
        runTest {
            viewModel.chooserState.test {
                assertEquals(ChooserState.FindingDevices, awaitItem())

                router.addProvider(provider)

                shadowOf(Looper.getMainLooper()).idle()

                assertEquals(ChooserState.ShowingRoutes, awaitItem())
            }
        }
    }

    @Test
    fun `check that the chooser state is updated periodically when no routes are available`() {
        runTest {
            viewModel.chooserState.test {
                assertEquals(ChooserState.FindingDevices, awaitItem())
                assertEquals(ChooserState.NoDevicesNoWifiHint, awaitItem())
                assertEquals(ChooserState.NoRoutes, awaitItem())
            }
        }
    }

    @Test
    fun `check the title based on the chooser state`() {
        assertEquals(
            expected = context.getString(R.string.mr_chooser_title),
            actual = ChooserState.FindingDevices.title(context),
        )
        assertEquals(
            expected = context.getString(R.string.mr_chooser_title),
            actual = ChooserState.NoDevicesNoWifiHint.title(context),
        )
        assertEquals(
            expected = context.getString(R.string.mr_chooser_zero_routes_found_title),
            actual = ChooserState.NoRoutes.title(context),
        )
        assertEquals(
            expected = context.getString(R.string.mr_chooser_title),
            actual = ChooserState.ShowingRoutes.title(context),
        )
    }

    @Test
    fun `check the confirm label based on the chooser state`() {
        assertNull(ChooserState.FindingDevices.confirmLabel(context))
        assertNull(ChooserState.NoDevicesNoWifiHint.confirmLabel(context))
        assertEquals(
            expected = context.getString(android.R.string.ok),
            actual = ChooserState.NoRoutes.confirmLabel(context),
        )
        assertNull(ChooserState.ShowingRoutes.confirmLabel(context))
    }

    @Test
    fun `check that selecting a route hides the dialog`() {
        router.addProvider(provider)

        runTest {
            viewModel.showDialog.test {
                assertTrue(awaitItem())

                router.findRouteById(ROUTE_ID_CONNECTED).select()

                shadowOf(Looper.getMainLooper()).idle()

                assertFalse(awaitItem())
            }
        }
    }

    @Test
    fun `check that turning the screen off hides the dialog`() {
        runTest {
            viewModel.showDialog.test {
                assertTrue(awaitItem())

                context.sendBroadcast(Intent(Intent.ACTION_SCREEN_OFF))

                shadowOf(Looper.getMainLooper()).idle()

                assertFalse(awaitItem())
            }
        }
    }

    @Test
    fun `ViewModel factory creates an instance of MediaRouteChooserDialogViewModel`() {
        Robolectric.buildActivity(ComponentActivity::class.java)
            .use { activityController ->
                val viewModel = ViewModelProvider(
                    owner = activityController.setup().get(),
                    factory = MediaRouteChooserDialogViewModel.Factory(context, MediaRouteSelector.EMPTY),
                ).get<ViewModel>()

                assertIs<MediaRouteChooserDialogViewModel>(viewModel)
            }
    }
}
