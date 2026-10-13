/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest

class PermissionDeniedDialogScreenshot {
    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun DefaultPreview() {
        ScreenshotTheme {
            PermissionDeniedDialog(onDismissRequest = {})
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun CustomTextsPreview() {
        ScreenshotTheme {
            PermissionDeniedDialog(
                message = "MediaMaestro needs access to nearby devices to find Cast devices on your network.",
                buttonText = "Grant permission",
                onDismissRequest = {},
            )
        }
    }
}
