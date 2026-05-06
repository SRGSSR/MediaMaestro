/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.core.content.getSystemService
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.testing.MediaRouterTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.ROUTE_ID_CONNECTED
import ch.srgssr.media.maestro.TestMediaRouteProvider.Companion.findRouteById
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class MediaRouteButtonTest {
    private lateinit var context: Context
    private lateinit var dialogTypes: MutableList<DialogType>
    private lateinit var router: MediaRouter

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()
        dialogTypes = mutableListOf()

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

    private fun assertMediaRouteButtonState(
        expectedDialogTypes: List<DialogType>,
        action: (ComposeUiTest.() -> Unit)? = null,
    ) = runComposeUiTest {
        setContent {
            MediaRouteButton(
                modifier = Modifier.testTag(TEST_TAG),
                mediaRouteChooserDialog = {},
                mediaRouteDynamicChooserDialog = {},
                mediaRouteControllerDialog = {},
                mediaRouteDynamicControllerDialog = {},
                onDialogTypeChange = dialogTypes::add,
            )
        }

        action?.let {
            it()
            waitForIdle()
        }

        assertEquals(expectedDialogTypes, dialogTypes)
    }

    private companion object {
        private const val TEST_TAG = "media_route_button"
    }
}
