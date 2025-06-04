/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest

class CastIconScreenshot {
    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun DisconnectedPreview() {
        ScreenshotTheme {
            CastIcon(
                state = CastConnectionState.Disconnected,
                contentDescription = null,
                size = 72.dp,
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectingPreview() {
        ScreenshotTheme {
            CastIcon(
                state = CastConnectionState.Connecting,
                contentDescription = null,
                size = 72.dp,
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectedPreview() {
        ScreenshotTheme {
            CastIcon(
                state = CastConnectionState.Connected,
                contentDescription = null,
                size = 72.dp,
            )
        }
    }
}
