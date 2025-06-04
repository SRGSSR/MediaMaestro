/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.mediarouter.media.ScreenshotMediaRouter
import ch.srgssr.media.maestro.MediaRouteChooserDialogViewModel.ChooserState
import com.android.tools.screenshot.PreviewTest

class MediaRouteChooserDialogScreenshot {
    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun FindingDevicesPreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.FindingDevices,
                title = null,
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                listColors = ListItemDefaults.colors(),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun NoDevicesNoWifiHintPreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoDevicesNoWifiHint,
                title = null,
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                listColors = ListItemDefaults.colors(),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun NoRoutesPreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoRoutes,
                title = null,
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                listColors = ListItemDefaults.colors(),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun ShowingRoutesPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ChooserDialog(
                routes = router.routes,
                state = ChooserState.ShowingRoutes,
                title = null,
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                listColors = ListItemDefaults.colors(
                    containerColor = AlertDialogDefaults.containerColor,
                    headlineColor = AlertDialogDefaults.textContentColor,
                    leadingIconColor = AlertDialogDefaults.iconContentColor,
                    supportingColor = AlertDialogDefaults.textContentColor,
                ),
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun FindingDevicesCustomStylePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.FindingDevices,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.inversePrimary,
                ),
                iconContentColor = MaterialTheme.colorScheme.inversePrimary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                textContentColor = MaterialTheme.colorScheme.onPrimary,
                listColors = ListItemDefaults.colors(),
                tonalElevation = 16.dp,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun NoDevicesNoWifiHintCustomStylePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoDevicesNoWifiHint,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.inversePrimary,
                ),
                iconContentColor = MaterialTheme.colorScheme.inversePrimary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                textContentColor = MaterialTheme.colorScheme.onPrimary,
                listColors = ListItemDefaults.colors(),
                tonalElevation = 16.dp,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun NoRoutesCustomStylePreview() {
        ScreenshotTheme {
            ChooserDialog(
                routes = emptyList(),
                state = ChooserState.NoRoutes,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.inversePrimary,
                ),
                iconContentColor = MaterialTheme.colorScheme.inversePrimary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                textContentColor = MaterialTheme.colorScheme.onPrimary,
                listColors = ListItemDefaults.colors(),
                tonalElevation = 16.dp,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun ShowingRoutesCustomStylePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ChooserDialog(
                routes = router.routes,
                state = ChooserState.ShowingRoutes,
                title = "Custom title",
                shape = CutCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.inversePrimary,
                ),
                iconContentColor = MaterialTheme.colorScheme.inversePrimary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                textContentColor = MaterialTheme.colorScheme.onPrimary,
                listColors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    headlineColor = MaterialTheme.colorScheme.onPrimary,
                    leadingIconColor = MaterialTheme.colorScheme.inversePrimary,
                    supportingColor = MaterialTheme.colorScheme.onPrimary,
                ),
                tonalElevation = 16.dp,
                properties = DialogProperties(),
                onDismissRequest = {},
            )
        }
    }
}
