/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.ScreenshotMediaRouter
import ch.srgssr.media.maestro.MediaRouteControllerDialogViewModel.RouteDetail
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.android.tools.screenshot.PreviewTest

class MediaRouteControllerDialogScreenshot {
    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun NoControlsPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = false,
                showVolumeControl = false,
                groupRouteDetails = emptyList(),
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun PlaybackControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = false,
                groupRouteDetails = emptyList(),
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun VolumeControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = false,
                showVolumeControl = true,
                groupRouteDetails = emptyList(),
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun CustomControlViewPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = false,
                groupRouteDetails = emptyList(),
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                customControlView = {
                    Text(text = "Custom control view")
                },
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun PlaybackControlAndVolumePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = true,
                groupRouteDetails = emptyList(),
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun PlaybackControlAndVolumeCustomStylePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = true,
                groupRouteDetails = emptyList(),
                shape = CutCornerShape(32.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.inversePrimary,
                ),
                iconContentColor = MaterialTheme.colorScheme.inversePrimary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                textContentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 16.dp,
                properties = DialogProperties(),
                customControlView = null,
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    private fun CustomControlViewAndVolumeControlPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            ControllerDialog(
                routeDetail = routeDetail(router.routes[0]),
                imageModel = null,
                title = "Media title",
                subtitle = "Media subtitle",
                iconInfo = Icons.PlayArrow to "Play",
                isDeviceGroupExpanded = false,
                showPlaybackControl = true,
                showVolumeControl = true,
                groupRouteDetails = emptyList(),
                shape = AlertDialogDefaults.shape,
                containerColor = AlertDialogDefaults.containerColor,
                buttonColors = ButtonDefaults.textButtonColors(),
                iconContentColor = AlertDialogDefaults.iconContentColor,
                titleContentColor = AlertDialogDefaults.titleContentColor,
                textContentColor = AlertDialogDefaults.textContentColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                properties = DialogProperties(),
                customControlView = {
                    Text(text = "Custom control view")
                },
                toggleDeviceGroup = {},
                onKeyEvent = { false },
                onPlaybackTitleClick = {},
                onPlaybackIconClick = {},
                onStopCasting = {},
                onDisconnect = {},
                onDismissRequest = {},
                onVolumeChange = { _, _ -> },
            )
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    @OptIn(ExperimentalCoilApi::class)
    private fun ImageUriLandscapePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            val imageColor = MaterialTheme.colorScheme.onSurface
            val imagePreviewHandler = AsyncImagePreviewHandler {
                createImage(imageColor.toArgb(), width = IMAGE_SIZE, height = IMAGE_SIZE * 9 / 16)
            }

            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides imagePreviewHandler) {
                ControllerDialog(
                    routeDetail = routeDetail(router.routes[0]),
                    imageModel = "https://image.url/",
                    title = "Media title",
                    subtitle = "Media subtitle",
                    iconInfo = Icons.PlayArrow to "Play",
                    isDeviceGroupExpanded = false,
                    showPlaybackControl = true,
                    showVolumeControl = true,
                    groupRouteDetails = emptyList(),
                    shape = AlertDialogDefaults.shape,
                    containerColor = AlertDialogDefaults.containerColor,
                    buttonColors = ButtonDefaults.textButtonColors(),
                    iconContentColor = AlertDialogDefaults.iconContentColor,
                    titleContentColor = AlertDialogDefaults.titleContentColor,
                    textContentColor = AlertDialogDefaults.textContentColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                    properties = DialogProperties(),
                    customControlView = null,
                    toggleDeviceGroup = {},
                    onKeyEvent = { false },
                    onPlaybackTitleClick = {},
                    onPlaybackIconClick = {},
                    onStopCasting = {},
                    onDisconnect = {},
                    onDismissRequest = {},
                    onVolumeChange = { _, _ -> },
                )
            }
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    @OptIn(ExperimentalCoilApi::class)
    private fun ImageUriSquarePreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            val imageColor = MaterialTheme.colorScheme.onSurface
            val imagePreviewHandler = AsyncImagePreviewHandler {
                createImage(imageColor.toArgb(), width = IMAGE_SIZE, height = IMAGE_SIZE)
            }

            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides imagePreviewHandler) {
                ControllerDialog(
                    routeDetail = routeDetail(router.routes[0]),
                    imageModel = "https://image.url/",
                    title = "Media title",
                    subtitle = "Media subtitle",
                    iconInfo = Icons.PlayArrow to "Play",
                    isDeviceGroupExpanded = false,
                    showPlaybackControl = true,
                    showVolumeControl = true,
                    groupRouteDetails = emptyList(),
                    shape = AlertDialogDefaults.shape,
                    containerColor = AlertDialogDefaults.containerColor,
                    buttonColors = ButtonDefaults.textButtonColors(),
                    iconContentColor = AlertDialogDefaults.iconContentColor,
                    titleContentColor = AlertDialogDefaults.titleContentColor,
                    textContentColor = AlertDialogDefaults.textContentColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                    properties = DialogProperties(),
                    customControlView = null,
                    toggleDeviceGroup = {},
                    onKeyEvent = { false },
                    onPlaybackTitleClick = {},
                    onPlaybackIconClick = {},
                    onStopCasting = {},
                    onDisconnect = {},
                    onDismissRequest = {},
                    onVolumeChange = { _, _ -> },
                )
            }
        }
    }

    @Composable
    @PreviewTest
    @ScreenshotPreviews
    @OptIn(ExperimentalCoilApi::class)
    private fun ImageUriPortraitPreview() {
        val router = ScreenshotMediaRouter(LocalContext.current)

        ScreenshotTheme {
            val imageColor = MaterialTheme.colorScheme.onSurface
            val imagePreviewHandler = AsyncImagePreviewHandler {
                createImage(
                    imageColor.toArgb(),
                    width = IMAGE_SIZE,
                    height = IMAGE_SIZE * 4 / 3
                )
            }

            CompositionLocalProvider(LocalAsyncImagePreviewHandler provides imagePreviewHandler) {
                ControllerDialog(
                    routeDetail = routeDetail(router.routes[0]),
                    imageModel = "https://image.url/",
                    title = "Media title",
                    subtitle = "Media subtitle",
                    iconInfo = Icons.PlayArrow to "Play",
                    isDeviceGroupExpanded = false,
                    showPlaybackControl = true,
                    showVolumeControl = true,
                    groupRouteDetails = emptyList(),
                    shape = AlertDialogDefaults.shape,
                    containerColor = AlertDialogDefaults.containerColor,
                    buttonColors = ButtonDefaults.textButtonColors(),
                    iconContentColor = AlertDialogDefaults.iconContentColor,
                    titleContentColor = AlertDialogDefaults.titleContentColor,
                    textContentColor = AlertDialogDefaults.textContentColor,
                    tonalElevation = AlertDialogDefaults.TonalElevation,
                    properties = DialogProperties(),
                    customControlView = null,
                    toggleDeviceGroup = {},
                    onKeyEvent = { false },
                    onPlaybackTitleClick = {},
                    onPlaybackIconClick = {},
                    onStopCasting = {},
                    onDisconnect = {},
                    onDismissRequest = {},
                    onVolumeChange = { _, _ -> },
                )
            }
        }
    }

    private fun createImage(@ColorInt color: Int, width: Int, height: Int): Image {
        val paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE

        val widthFloat = width.toFloat()
        val heightFloat = height.toFloat()

        val bitmap = createBitmap(width, height)
        Canvas(bitmap).apply {
            drawColor(color)
            drawRect(1f, 1f, widthFloat - 2f, heightFloat - 2f, paint)
            drawLine(0f, 0f, widthFloat, heightFloat, paint)
            drawLine(widthFloat, 0f, 0f, heightFloat, paint)
        }

        return bitmap.asImage()
    }

    private fun routeDetail(route: MediaRouter.RouteInfo): RouteDetail {
        return RouteDetail(
            route = route,
            volume = route.volume.toFloat(),
            volumeRange = 0f..route.volumeMax.toFloat(),
        )
    }

    private companion object {
        private const val IMAGE_SIZE = 1000
    }
}
