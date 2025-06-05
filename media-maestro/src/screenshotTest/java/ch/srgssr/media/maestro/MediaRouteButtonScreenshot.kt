/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest

class MediaRouteButtonScreenshot {
    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun DisconnectedPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Disconnected,
                fixedIcon = false,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectingPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connecting,
                fixedIcon = false,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectedPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connected,
                fixedIcon = false,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun DisconnectedFixedIconPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Disconnected,
                fixedIcon = true,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectingFixedIconPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connecting,
                fixedIcon = true,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectedFixedIconPreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connected,
                fixedIcon = true,
                colors = IconButtonDefaults.iconButtonColors(),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun DisconnectedCustomStylePreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Disconnected,
                fixedIcon = false,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectingCustomStylePreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connecting,
                fixedIcon = false,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                onClick = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun ConnectedCustomStylePreview() {
        ScreenshotTheme {
            MediaRouteButton(
                state = CastConnectionState.Connected,
                fixedIcon = false,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                onClick = {},
            )
        }
    }
}
