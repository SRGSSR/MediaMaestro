/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import ch.srgssr.media.maestro.MediaRouteControllerDialogViewModel.RouteDetail
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter

/**
 * This class implements the route controller dialog for [MediaRouter].
 *
 * This dialog allows the user to control or disconnect from the currently selected route.
 *
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 * @param modifier The [Modifier] to be applied to this dialog.
 * @param volumeControlEnabled Whether to enable the volume slider and volume control using the
 * volume keys when the route supports it.
 * @param shape The shape of the dialog.
 * @param containerColor The color used for the background of the dialog. Use [Color.Transparent] to
 * have no color.
 * @param buttonColors The colors used for buttons.
 * @param iconContentColor The content color used for the icon.
 * @param titleContentColor The content color used for the title.
 * @param textContentColor The content color used for the text.
 * @param tonalElevation When [containerColor] is [ColorScheme.surface], a translucent primary color
 * overlay is applied on top of the container. A higher tonal elevation value will result in a
 * darker color in light theme and lighter color in dark theme. See also: [Surface].
 * @param properties Typically platform specific properties to further configure the dialog.
 * @param onDismissRequest The action to perform when this dialog is dismissed.
 * @param customControlView The view that will replace the default media controls for the currently
 * playing content.
 *
 * @see MediaRouteButton
 */
@Composable
public fun MediaRouteControllerDialog(
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
    volumeControlEnabled: Boolean = true,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    buttonColors: ButtonColors = ButtonDefaults.textButtonColors(),
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit,
    customControlView: @Composable (() -> Unit)? = null,
) {
    val context = LocalContext.current.applicationContext
    val viewModel = viewModel<MediaRouteControllerDialogViewModel>(
        key = routeSelector.toString(),
        factory = MediaRouteControllerDialogViewModel.Factory(volumeControlEnabled, context),
    )
    val showDialog by viewModel.showDialog.collectAsState()
    val isDeviceGroupExpanded by viewModel.isDeviceGroupExpanded.collectAsState()
    val showPlaybackControl by viewModel.showPlaybackControl.collectAsState()
    val showVolumeControl by viewModel.showVolumeControl.collectAsState()
    val imageModel by viewModel.imageModel.collectAsState()
    val title by viewModel.title.collectAsState()
    val subtitle by viewModel.subtitle.collectAsState()
    val iconInfo by viewModel.iconInfo.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val selectedRouteDetail = routes[0]
    val groupRouteDetails = routes.drop(1)

    LaunchedEffect(showDialog) {
        if (!showDialog) {
            onDismissRequest()
        }
    }

    ControllerDialog(
        routeDetail = selectedRouteDetail,
        imageModel = imageModel,
        title = title,
        subtitle = subtitle,
        iconInfo = iconInfo,
        isDeviceGroupExpanded = isDeviceGroupExpanded,
        showPlaybackControl = showPlaybackControl,
        showVolumeControl = showVolumeControl,
        groupRouteDetails = groupRouteDetails,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        buttonColors = buttonColors,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
        customControlView = customControlView,
        toggleDeviceGroup = viewModel::toggleDeviceGroup,
        onKeyEvent = viewModel::onKeyEvent,
        onPlaybackTitleClick = viewModel::startSessionActivity,
        onPlaybackIconClick = viewModel::onPlaybackIconClick,
        onStopCasting = viewModel::stopCasting,
        onDisconnect = viewModel::disconnect,
        onDismissRequest = viewModel::hideDialog,
        onVolumeChange = viewModel::setRouteVolume,
    )
}

@Composable
@Suppress("LongMethod")
internal fun ControllerDialog(
    routeDetail: RouteDetail,
    imageModel: Any?,
    title: String?,
    subtitle: String?,
    iconInfo: Pair<ImageVector, String>?,
    isDeviceGroupExpanded: Boolean,
    showPlaybackControl: Boolean,
    showVolumeControl: Boolean,
    groupRouteDetails: List<RouteDetail>,
    modifier: Modifier = Modifier,
    shape: Shape,
    containerColor: Color,
    buttonColors: ButtonColors,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
    tonalElevation: Dp,
    properties: DialogProperties,
    customControlView: @Composable (() -> Unit)?,
    toggleDeviceGroup: () -> Unit,
    onKeyEvent: (keyEvent: KeyEvent) -> Boolean,
    onPlaybackTitleClick: () -> Unit,
    onPlaybackIconClick: () -> Unit,
    onStopCasting: () -> Unit,
    onDisconnect: () -> Unit,
    onDismissRequest: () -> Unit,
    onVolumeChange: (route: RouteInfo, volume: Float) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onStopCasting,
                colors = buttonColors,
            ) {
                Text(text = stringResource(R.string.mr_controller_stop_casting))
            }
        },
        modifier = modifier.onKeyEvent(onKeyEvent),
        dismissButton = if (routeDetail.route.canDisconnect()) {
            {
                TextButton(
                    onClick = onDisconnect,
                    colors = buttonColors,
                ) {
                    Text(text = stringResource(R.string.mr_controller_disconnect))
                }
            }
        } else {
            null
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = routeDetail.route.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Close,
                        contentDescription = stringResource(R.string.mr_controller_close_description),
                    )
                }
            }
        },
        text = {
            ControllerDialogContent(
                routeDetail = routeDetail,
                imageModel = imageModel,
                title = title,
                subtitle = subtitle,
                iconInfo = iconInfo,
                iconContentColor = iconContentColor,
                isDeviceGroupExpanded = isDeviceGroupExpanded,
                showPlaybackControl = showPlaybackControl,
                showVolumeControl = showVolumeControl,
                groupRouteDetails = groupRouteDetails,
                modifier = Modifier.fillMaxWidth(),
                customControlView = customControlView,
                onToggleDeviceGroup = toggleDeviceGroup,
                onPlaybackTitleClick = onPlaybackTitleClick,
                onPlaybackIconClick = onPlaybackIconClick,
                onVolumeChange = onVolumeChange,
            )
        },
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
    )
}

@Composable
private fun ControllerDialogContent(
    routeDetail: RouteDetail,
    imageModel: Any?,
    title: String?,
    subtitle: String?,
    iconInfo: Pair<ImageVector, String>?,
    iconContentColor: Color,
    isDeviceGroupExpanded: Boolean,
    showPlaybackControl: Boolean,
    showVolumeControl: Boolean,
    groupRouteDetails: List<RouteDetail>,
    modifier: Modifier = Modifier,
    customControlView: @Composable (() -> Unit)?,
    onToggleDeviceGroup: () -> Unit,
    onPlaybackTitleClick: () -> Unit,
    onPlaybackIconClick: () -> Unit,
    onVolumeChange: (route: RouteInfo, volume: Float) -> Unit,
) {
    @Suppress("NoNameShadowing")
    val showPlaybackControl = showPlaybackControl && customControlView == null
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        customControlView?.invoke()

        AnimatedVisibility(visible = imageModel != null && !isLandscape) {
            Image(
                imageModel = imageModel,
                modifier = Modifier.fillMaxWidth(),
                onClick = onPlaybackTitleClick,
            )
        }

        if (!showVolumeControl && !showPlaybackControl) {
            return
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            if (showPlaybackControl) {
                PlaybackControlRow(
                    title = title,
                    subtitle = subtitle,
                    icon = iconInfo?.first,
                    iconContentColor = iconContentColor,
                    contentDescription = iconInfo?.second,
                    modifier = Modifier.fillMaxWidth(),
                    onTitleClick = onPlaybackTitleClick,
                    onIconClick = onPlaybackIconClick,
                )
            }

            if (showVolumeControl) {
                VolumeControl(
                    routeDetail = routeDetail,
                    iconContentColor = iconContentColor,
                    modifier = Modifier.fillMaxWidth(),
                    isExpanded = isDeviceGroupExpanded,
                    onExpandCollapseClick = onToggleDeviceGroup,
                    onVolumeChange = onVolumeChange,
                )
            }
        }

        AnimatedVisibility(visible = isDeviceGroupExpanded) {
            DeviceGroup(
                routeDetails = groupRouteDetails,
                iconContentColor = iconContentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                onVolumeChange = onVolumeChange,
            )
        }
    }
}

@Composable
private fun Image(
    imageModel: Any?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var contentScale by remember(imageModel) { mutableStateOf(ContentScale.Fit) }

    AsyncImage(
        model = imageModel,
        contentDescription = stringResource(R.string.mr_controller_album_art),
        modifier = modifier.clickable(onClick = onClick),
        onState = { state ->
            if (state is AsyncImagePainter.State.Success) {
                val width = state.result.image.width
                val height = state.result.image.height

                contentScale = if (width >= height) {
                    ContentScale.FillWidth
                } else {
                    ContentScale.Fit
                }
            }
        },
        contentScale = contentScale,
    )
}

@Composable
private fun PlaybackControlRow(
    title: CharSequence?,
    subtitle: CharSequence?,
    icon: ImageVector?,
    iconContentColor: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onTitleClick: () -> Unit,
    onIconClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
                .clickable(onClick = onTitleClick),
        ) {
            if (title != null) {
                Text(
                    text = title.toString(),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            if (subtitle != null) {
                Text(
                    text = subtitle.toString(),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (icon != null) {
            IconButton(onClick = onIconClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = iconContentColor,
                )
            }
        }
    }
}

@Composable
private fun VolumeControl(
    routeDetail: RouteDetail,
    iconContentColor: Color,
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onExpandCollapseClick: () -> Unit,
    onVolumeChange: (route: RouteInfo, volume: Float) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Audiotrack,
            contentDescription = null,
            tint = iconContentColor,
        )

        Slider(
            value = routeDetail.volume,
            onValueChange = { onVolumeChange(routeDetail.route, it) },
            modifier = Modifier.weight(1f),
            valueRange = routeDetail.volumeRange,
        )

        if (routeDetail.hasMembers) {
            IconButton(onClick = onExpandCollapseClick) {
                val scale by animateFloatAsState(targetValue = if (isExpanded) -1f else 1f)
                val contentDescriptionRes = if (isExpanded) {
                    R.string.mr_controller_collapse_group
                } else {
                    R.string.mr_controller_expand_group
                }

                Icon(
                    imageVector = Icons.ExpandMore,
                    contentDescription = stringResource(contentDescriptionRes),
                    modifier = Modifier.scale(scaleX = 1f, scaleY = scale),
                    tint = iconContentColor,
                )
            }
        }
    }
}

@Composable
private fun DeviceGroup(
    routeDetails: List<RouteDetail>,
    iconContentColor: Color,
    modifier: Modifier = Modifier,
    onVolumeChange: (route: RouteInfo, volume: Float) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        routeDetails.forEach { (route, volume, volumeRange) ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = route.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Audiotrack,
                        contentDescription = null,
                        tint = iconContentColor,
                    )

                    Slider(
                        value = volume,
                        onValueChange = { onVolumeChange(route, it) },
                        modifier = Modifier.weight(1f),
                        enabled = route.isEnabled,
                        valueRange = volumeRange,
                    )
                }
            }
        }
    }
}
