/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.getSystemService
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.testing.MediaRouterTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_CONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.findRouteById
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class MediaRouteButtonTest {
    private lateinit var context: Application
    private lateinit var dialogTypes: MutableList<DialogType>
    private lateinit var registry: TestActivityResultRegistry
    private lateinit var router: MediaRouter

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()
        dialogTypes = mutableListOf()
        registry = TestActivityResultRegistry()

        // Trigger static initialization inside MediaRouter
        context.getSystemService<android.media.MediaRouter>()

        router = MediaRouter.getInstance(context)
        router.addProvider(TestMediaRouteProvider(context))
    }

    @AfterTest
    fun after() {
        MediaRouterTestHelper.resetMediaRouter()
    }

    @Test
    fun `default dialog type`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None),
        )
    }

    @Test
    fun `clicking on button should open the chooser dialog`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Chooser),
            action = {
                onNodeWithTag(TEST_TAG).performClick()
            },
        )

        assertNull(registry.launchedInput)
    }

    @Test
    fun `clicking on button should open the controller dialog when a non-default route is selected`() {
        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Controller),
            action = {
                router.findRouteById(ROUTE_ID_CONNECTED).select()

                onNodeWithTag(TEST_TAG).performClick()
            },
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.CINNAMON_BUN])
    fun `clicking on button should open the chooser dialog when the local network permission is not declared`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.CINNAMON_BUN, declarePermission = false)

        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Chooser),
            action = {
                onNodeWithTag(TEST_TAG).performClick()
            },
        )

        assertNull(registry.launchedInput)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.CINNAMON_BUN])
    fun `clicking on button should open the chooser dialog when the local network permission is granted`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.CINNAMON_BUN, declarePermission = true)
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_LOCAL_NETWORK)

        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Chooser),
            action = {
                onNodeWithTag(TEST_TAG).performClick()
            },
        )

        assertNull(registry.launchedInput)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.CINNAMON_BUN])
    fun `clicking on button should request the local network permission and open the chooser when granted`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.CINNAMON_BUN, declarePermission = true)

        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.Chooser),
            action = {
                onNodeWithTag(TEST_TAG).performClick()

                // The dialog is only shown once the user answered the permission request
                assertEquals(listOf(DialogType.None), dialogTypes)
                assertEquals(Manifest.permission.ACCESS_LOCAL_NETWORK, registry.launchedInput)

                runOnUiThread { registry.answer(result = true) }
            },
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.CINNAMON_BUN])
    fun `clicking on button should request the local network permission and open the denied dialog when denied`() {
        context.setupLocalNetworkPermission(targetSdk = Build.VERSION_CODES.CINNAMON_BUN, declarePermission = true)

        assertMediaRouteButtonState(
            expectedDialogTypes = listOf(DialogType.None, DialogType.PermissionDenied),
            action = {
                onNodeWithTag(TEST_TAG).performClick()

                assertEquals(Manifest.permission.ACCESS_LOCAL_NETWORK, registry.launchedInput)

                runOnUiThread { registry.answer(result = false) }
            },
        )
    }

    private fun assertMediaRouteButtonState(
        expectedDialogTypes: List<DialogType>,
        action: (ComposeUiTest.() -> Unit)? = null,
    ) = runComposeUiTest {
        setContent {
            val registryOwner = object : ActivityResultRegistryOwner {
                override val activityResultRegistry: ActivityResultRegistry = registry
            }

            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                MediaRouteButton(
                    modifier = Modifier.testTag(TEST_TAG),
                    mediaRouteChooserDialog = {},
                    mediaRouteDynamicChooserDialog = {},
                    mediaRouteControllerDialog = {},
                    mediaRouteDynamicControllerDialog = {},
                    onDialogTypeChange = dialogTypes::add,
                    permissionDeniedDialog = {},
                )
            }
        }

        action?.let {
            it()
            waitForIdle()
        }

        assertEquals(expectedDialogTypes, dialogTypes)
    }

    /**
     * [ActivityResultRegistry] recording the launched request, and letting the test provide the result.
     */
    private class TestActivityResultRegistry : ActivityResultRegistry() {
        private var requestCode: Int? = null

        var launchedInput: Any? = null
            private set

        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) {
            this.requestCode = requestCode
            this.launchedInput = input
        }

        fun answer(result: Any) {
            dispatchResult(checkNotNull(requestCode), result)
        }
    }

    private companion object {
        private const val TEST_TAG = "media_route_button"
    }
}
