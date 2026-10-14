/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class PermissionDeniedDialogTest {
    private lateinit var context: Application

    @BeforeTest
    fun before() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `default texts are displayed`() = runComposeUiTest {
        setContent {
            PermissionDeniedDialog(onDismissRequest = {})
        }

        onNodeWithText(context.getString(R.string.media_maestro_local_network_permission_denied)).assertIsDisplayed()
        onNodeWithText(context.getString(R.string.media_maestro_open_settings)).assertIsDisplayed()
    }

    @Test
    fun `custom texts are displayed`() = runComposeUiTest {
        setContent {
            PermissionDeniedDialog(
                message = MESSAGE,
                buttonText = BUTTON_TEXT,
                onDismissRequest = {},
            )
        }

        onNodeWithText(MESSAGE).assertIsDisplayed()
        onNodeWithText(BUTTON_TEXT).assertIsDisplayed()
    }

    @Test
    fun `clicking on the default button opens the settings and dismisses the dialog`() = runComposeUiTest {
        var dismissCount = 0

        setContent {
            PermissionDeniedDialog(onDismissRequest = { dismissCount++ })
        }

        onNodeWithText(context.getString(R.string.media_maestro_open_settings)).performClick()
        waitForIdle()

        val startedActivity = shadowOf(context).nextStartedActivity

        assertNotNull(startedActivity)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, startedActivity.action)
        assertEquals("package:${context.packageName}", startedActivity.dataString)
        assertEquals(1, dismissCount)
    }

    @Test
    fun `clicking on the button calls the custom action and dismisses the dialog`() = runComposeUiTest {
        var clickContext: Context? = null
        var dismissCount = 0

        setContent {
            PermissionDeniedDialog(
                buttonText = BUTTON_TEXT,
                onClickRequest = { clickContext = it },
                onDismissRequest = { dismissCount++ },
            )
        }

        onNodeWithText(BUTTON_TEXT).performClick()
        waitForIdle()

        assertNotNull(clickContext)
        assertNull(shadowOf(context).nextStartedActivity)
        assertEquals(1, dismissCount)
    }

    private companion object {
        private const val BUTTON_TEXT = "Grant permission"
        private const val MESSAGE = "Permission denied"
    }
}
