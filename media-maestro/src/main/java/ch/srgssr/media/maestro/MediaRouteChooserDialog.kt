/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.graphics.drawable.Drawable
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.R
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.RouteInfo
import ch.srgssr.media.maestro.MediaRouteChooserDialogViewModel.ChooserState

/**
 * This class implements the route chooser dialog for [MediaRouter].
 *
 * This dialog allows the user to choose a route that matches a given selector.
 *
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 * @param modifier The [Modifier] to be applied to this dialog.
 * @param title The title of the dialog. If `null`, it will be defined based on the chooser state.
 * @param shape The shape of the dialog.
 * @param containerColor The color used for the background of the dialog. Use [Color.Transparent] to
 * have no color.
 * @param buttonColors The colors used for buttons.
 * @param iconContentColor The content color used for the icon.
 * @param titleContentColor The content color used for the title.
 * @param textContentColor The content color used for the text.
 * @param listColors The colors used for the list of routes.
 * @param tonalElevation When [containerColor] is [ColorScheme.surface], a translucent primary color
 * overlay is applied on top of the container. A higher tonal elevation value will result in a
 * darker color in light theme and lighter color in dark theme. See also: [Surface].
 * @param properties Typically platform specific properties to further configure the dialog.
 * @param onDismissRequest The action to perform when this dialog is dismissed.
 *
 * @see MediaRouteButton
 */
@Composable
public fun MediaRouteChooserDialog(
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
    title: String? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    buttonColors: ButtonColors = ButtonDefaults.textButtonColors(),
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    listColors: ListItemColors = ListItemDefaults.colors(
        containerColor = containerColor,
        headlineColor = textContentColor,
        leadingIconColor = iconContentColor,
        supportingColor = textContentColor,
    ),
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current.applicationContext
    val viewModel = viewModel<MediaRouteChooserDialogViewModel>(
        key = routeSelector.toString(),
        factory = MediaRouteChooserDialogViewModel.Factory(routeSelector,context),
    )
    val showDialog by viewModel.showDialog.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val chooserState by viewModel.chooserState.collectAsState()

    LaunchedEffect(showDialog) {
        if (!showDialog) {
            onDismissRequest()
        }
    }

    ChooserDialog(
        routes = routes,
        state = chooserState,
        title = title,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        listColors = listColors,
        buttonColors = buttonColors,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties,
        onDismissRequest = viewModel::hideDialog,
    )
}

@Composable
@VisibleForTesting
internal fun ChooserDialog(
    routes: List<RouteInfo>,
    state: ChooserState,
    title: String?,
    modifier: Modifier = Modifier,
    shape: Shape,
    containerColor: Color,
    listColors: ListItemColors,
    buttonColors: ButtonColors,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
    tonalElevation: Dp,
    properties: DialogProperties,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            val confirmButtonLabel = state.confirmLabel(context)
            if (confirmButtonLabel != null) {
                TextButton(
                    onClick = onDismissRequest,
                    colors = buttonColors,
                ) {
                    Text(text = confirmButtonLabel)
                }
            }
        },
        modifier = modifier,
        title = {
            Text(
                text = title ?: state.title(context),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        text = {
            when (state) {
                ChooserState.FindingDevices -> FindingDevices()
                ChooserState.NoDevicesNoWifiHint -> NoDevicesNoWifiHint(iconContentColor)
                ChooserState.NoRoutes -> NoRoutes(
                    actionContentColor = buttonColors.contentColor,
                    iconContentColor = iconContentColor,
                )

                ChooserState.ShowingRoutes -> ShowingRoutes(
                    routes = routes,
                    colors = listColors,
                    onRouteClick = RouteInfo::select,
                )
            }
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
private fun FindingDevices(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.mr_chooser_looking_for_devices))

        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NoDevicesNoWifiHint(
    iconContentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Wifi,
                contentDescription = stringResource(R.string.ic_media_route_learn_more_accessibility),
                tint = iconContentColor,
            )

            Text(text = DeviceUtils.getDialogChooserWifiWarningDescription(LocalContext.current))
        }

        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun NoRoutes(
    actionContentColor: Color,
    iconContentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Wifi,
            contentDescription = stringResource(R.string.ic_media_route_learn_more_accessibility),
            tint = iconContentColor,
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val uriHandler = LocalUriHandler.current
            val url = "https://support.google.com/chromecast/?p=trouble-finding-devices"
            val link = LinkAnnotation.Url(
                url = url,
                styles = TextLinkStyles(style = SpanStyle(color = actionContentColor)),
                linkInteractionListener = { uriHandler.openUri(url) },
            )

            Text(text = DeviceUtils.getDialogChooserWifiWarningDescription(LocalContext.current))

            Text(
                text = buildAnnotatedString {
                    withLink(link) {
                        append(stringResource(R.string.mr_chooser_wifi_learn_more))
                    }
                },
            )
        }
    }
}

@Composable
private fun ShowingRoutes(
    routes: List<RouteInfo>,
    colors: ListItemColors,
    modifier: Modifier = Modifier,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(
            items = routes,
            key = { it.id },
        ) { route ->
            RouteItem(
                route = route,
                colors = colors,
                modifier = Modifier.animateItem(),
                onRouteClick = onRouteClick,
            )
        }
    }
}

@Composable
private fun RouteItem(
    route: RouteInfo,
    colors: ListItemColors,
    modifier: Modifier = Modifier,
    onRouteClick: (route: RouteInfo) -> Unit,
) {
    val isConnectedOrConnecting =
        route.connectionState == RouteInfo.CONNECTION_STATE_CONNECTED
                || route.connectionState == RouteInfo.CONNECTION_STATE_CONNECTING

    ListItem(
        headlineContent = {
            Text(
                text = route.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        modifier = modifier.clickable(route.isEnabled) {
            onRouteClick(route)
        },
        supportingContent = if (isConnectedOrConnecting && !route.description.isNullOrBlank()) {
            {
                Text(
                    text = route.description.orEmpty(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        } else {
            null
        },
        leadingContent = {
            val context = LocalContext.current
            val imageBitmap = remember(route.iconUri) {
                route.iconUri
                    ?.let { context.contentResolver.openInputStream(it) }
                    ?.use { Drawable.createFromStream(it, null) }
                    ?.toBitmap()
                    ?.asImageBitmap()
            }

            if (imageBitmap != null) {
                Icon(
                    bitmap = imageBitmap,
                    contentDescription = null,
                )
            } else {
                val defaultIcon = when {
                    route.deviceType == RouteInfo.DEVICE_TYPE_TV -> Icons.Tv
                    route.deviceType == RouteInfo.DEVICE_TYPE_REMOTE_SPEAKER -> Icons.Speaker
                    route.isGroup -> Icons.SpeakerGroup
                    else -> Icons.Cast
                }

                Icon(
                    imageVector = defaultIcon,
                    contentDescription = null,
                )
            }
        },
        colors = colors,
    )
}

@Preview
@Composable
private fun ChooserDialogFindingDevicesPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = ChooserState.FindingDevices,
            title = null,
            shape = AlertDialogDefaults.shape,
            containerColor = AlertDialogDefaults.containerColor,
            listColors = ListItemDefaults.colors(),
            buttonColors = ButtonDefaults.textButtonColors(),
            iconContentColor = AlertDialogDefaults.iconContentColor,
            titleContentColor = AlertDialogDefaults.titleContentColor,
            textContentColor = AlertDialogDefaults.textContentColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            properties = DialogProperties(),
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogNoDevicesNoWifiHintPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = ChooserState.NoDevicesNoWifiHint,
            title = null,
            shape = AlertDialogDefaults.shape,
            containerColor = AlertDialogDefaults.containerColor,
            listColors = ListItemDefaults.colors(),
            buttonColors = ButtonDefaults.textButtonColors(),
            iconContentColor = AlertDialogDefaults.iconContentColor,
            titleContentColor = AlertDialogDefaults.titleContentColor,
            textContentColor = AlertDialogDefaults.textContentColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            properties = DialogProperties(),
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogNoRoutesPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = ChooserState.NoRoutes,
            title = null,
            shape = AlertDialogDefaults.shape,
            containerColor = AlertDialogDefaults.containerColor,
            listColors = ListItemDefaults.colors(),
            buttonColors = ButtonDefaults.textButtonColors(),
            iconContentColor = AlertDialogDefaults.iconContentColor,
            titleContentColor = AlertDialogDefaults.titleContentColor,
            textContentColor = AlertDialogDefaults.textContentColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            properties = DialogProperties(),
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun ChooserDialogShowingRoutesPreview() {
    MaterialTheme {
        ChooserDialog(
            routes = emptyList(),
            state = ChooserState.ShowingRoutes,
            title = null,
            shape = AlertDialogDefaults.shape,
            containerColor = AlertDialogDefaults.containerColor,
            listColors = ListItemDefaults.colors(),
            buttonColors = ButtonDefaults.textButtonColors(),
            iconContentColor = AlertDialogDefaults.iconContentColor,
            titleContentColor = AlertDialogDefaults.titleContentColor,
            textContentColor = AlertDialogDefaults.textContentColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            properties = DialogProperties(),
            onDismissRequest = {},
        )
    }
}
