/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.app.Application
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.testing.ViewModelScenario
import androidx.lifecycle.viewmodel.testing.viewModelScenario
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouterParams
import androidx.mediarouter.testing.MediaRouterTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_CONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_CONNECTING
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_DISCONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_INVALID
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.findRouteById
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MediaRouteButtonViewModelTest {
    private lateinit var context: Application
    private lateinit var router: MediaRouter
    private lateinit var viewModelScenario: ViewModelScenario<MediaRouteButtonViewModel>

    private val viewModel: MediaRouteButtonViewModel
        get() = viewModelScenario.viewModel

    @BeforeTest
    fun before() {
        val routeSelector = MediaRouteSelector.Builder()
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .build()

        context = ApplicationProvider.getApplicationContext()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        router = MediaRouter.getInstance(context)
        router.addProvider(TestMediaRouteProvider(context))

        viewModelScenario = viewModelScenario {
            MediaRouteButtonViewModel(context, SavedStateHandle(), routeSelector)
        }
    }

    @AfterTest
    fun after() {
        viewModelScenario.close()

        MediaRouterTestHelper.resetMediaRouter()
    }

    @Test
    fun `check the default values`() = runTest {
        viewModel.castConnectionState.test {
            assertEquals(CastConnectionState.Disconnected, awaitItem())
        }

        viewModel.dialogType.test {
            assertEquals(DialogType.None, awaitItem())
        }

        viewModel.fixedIcon.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `check the cast connection state with a connected route`() = runTest {
        router.findRouteById(ROUTE_ID_CONNECTED).select()

        viewModel.castConnectionState.test {
            assertEquals(CastConnectionState.Connected, awaitItem())
        }
    }

    @Test
    fun `check the cast connection state with a connecting route`() = runTest {
        router.findRouteById(ROUTE_ID_CONNECTING).select()

        viewModel.castConnectionState.test {
            assertEquals(CastConnectionState.Connecting, awaitItem())
        }
    }

    @Test
    fun `check the cast connection state with a disconnected route`() = runTest {
        router.findRouteById(ROUTE_ID_DISCONNECTED).select()

        viewModel.castConnectionState.test {
            assertEquals(CastConnectionState.Disconnected, awaitItem())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `check the cast connection state with an invalid state route`() = runTest {
        router.findRouteById(ROUTE_ID_INVALID).select()

        viewModel.castConnectionState.test {
            assertEquals(CastConnectionState.Disconnected, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is hidden with the default route`() = runTest {
        viewModel.dialogType.test {
            viewModel.hideDialog()

            assertEquals(DialogType.None, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with the default route`() = runTest {
        viewModel.dialogType.test {
            viewModel.showDialog()

            assertEquals(DialogType.None, awaitItem())
            assertEquals(DialogType.Chooser, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with the default route and router params`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder().build()

            viewModel.dialogType.test {
                viewModel.showDialog()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.Chooser, awaitItem())
            }
        }

    @Test
    fun `check the dialog type when the dialog is shown with the default route and dynamic group`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder()
                .setDialogType(MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP)
                .build()

            viewModel.dialogType.test {
                viewModel.showDialog()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.DynamicChooser, awaitItem())
            }
        }

    @Test
    fun `check the dialog type when the dialog is hidden with a non-default route`() = runTest {
        viewModel.dialogType.test {
            router.findRouteById(ROUTE_ID_CONNECTED).select()
            viewModel.hideDialog()

            assertEquals(DialogType.None, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with a non-default route`() = runTest {
        viewModel.dialogType.test {
            router.findRouteById(ROUTE_ID_CONNECTED).select()
            viewModel.showDialog()

            assertEquals(DialogType.None, awaitItem())
            assertEquals(DialogType.Controller, awaitItem())
        }
    }

    @Test
    fun `check the dialog type when the dialog is shown with a non-default route and router params`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder().build()

            viewModel.dialogType.test {
                router.findRouteById(ROUTE_ID_CONNECTED).select()
                viewModel.showDialog()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.Controller, awaitItem())
            }
        }

    @Test
    fun `check the dialog type when the dialog is shown with a non-default route and dynamic group`() =
        runTest {
            router.routerParams = MediaRouterParams.Builder()
                .setDialogType(MediaRouterParams.DIALOG_TYPE_DYNAMIC_GROUP)
                .build()

            viewModel.dialogType.test {
                router.findRouteById(ROUTE_ID_CONNECTED).select()
                viewModel.showDialog()

                assertEquals(DialogType.None, awaitItem())
                assertEquals(DialogType.DynamicController, awaitItem())
            }
        }

    @Test
    fun `check that the fixed icon value is read from the router parameters`() = runTest {
        viewModel.fixedIcon.test {
            val routerParamsExtras = Bundle()
            routerParamsExtras.putBoolean(MediaRouterParams.EXTRAS_KEY_FIXED_CAST_ICON, true)

            router.routerParams = MediaRouterParams.Builder()
                .setExtras(routerParamsExtras)
                .build()

            shadowOf(Looper.getMainLooper()).idle()

            router.routerParams = null

            shadowOf(Looper.getMainLooper()).idle()

            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `ViewModel factory fails to create a ViewModel without a Context`() {
        MediaRouteButtonViewModel.Factory(MediaRouteSelector.EMPTY)
            .create(ViewModel::class.java, CreationExtras.Empty)
    }

    @Test
    fun `ViewModel factory creates an instance of MediaRouteButtonViewModel`() {
        Robolectric.buildActivity(ComponentActivity::class.java)
            .use { activityController ->
                val viewModel = ViewModelProvider(
                    owner = activityController.setup().get(),
                    factory = MediaRouteButtonViewModel.Factory(MediaRouteSelector.EMPTY),
                ).get<ViewModel>()

                assertIs<MediaRouteButtonViewModel>(viewModel)
            }
    }
}
