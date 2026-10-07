/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.content.Context
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.media.MediaRouteSelector

/**
 * The media route button allows the user to select routes and to control the currently selected
 * route.
 *
 * The application must specify the kinds of routes that the user should be allowed to select by
 * specifying a [selector][MediaRouteSelector].
 *
 * When the default route is selected, the button will appear in an inactive state indicating that
 * the application is not connected to a route. Clicking on the button opens a
 * [MediaRouteChooserDialog] to allow the user to select a route. If no non-default routes
 * match the selector, and it is not possible for an active scan to discover any matching routes,
 * then the button is disabled.
 *
 * When a non-default route is selected, the button will appear in an active state indicating that
 * the application is connected to a route of the kind that it wants to use. The button may also
 * appear in an intermediary connecting state if the route is in the process of connecting to the
 * destination but has not yet completed doing so. In either case, clicking on the button opens a
 * [MediaRouteControllerDialog] to allow the user to control or disconnect from the current route.
 *
 * Starting with Android 17, discovering Cast devices requires the `ACCESS_LOCAL_NETWORK` runtime
 * permission. If your application targets Android 17 or later and declares this permission in its
 * manifest, clicking on the button requests it, if needed. If the permission is denied, a
 * [PermissionDeniedDialog] is shown instead of the other dialogs.
 *
 * This dialog can be customized with the `permissionDeniedDialog` parameter, for example to change
 * its texts:
 *
 * ```kotlin
 * MediaRouteButton(
 *     routeSelector = routeSelector,
 *     permissionDeniedDialog = { onDismissRequest ->
 *         PermissionDeniedDialog(
 *             message = stringResource(R.string.local_network_permission_denied),
 *             buttonText = stringResource(R.string.grant_permission),
 *             onDismissRequest = onDismissRequest,
 *         )
 *     },
 * )
 * ```
 *
 * You can also provide your own composable. In that case, make sure to call `onDismissRequest` when
 * the dialog is dismissed.
 *
 * @param modifier The [Modifier] to be applied to this button.
 * @param routeSelector The media route selector for filtering the routes that the user can select
 * using the media route chooser dialog.
 * @param colors [IconButtonColors] that will be used to resolve the colors used for this icon
 * button in different states. See [IconButtonDefaults.iconButtonColors].
 * @param mediaRouteChooserDialog The media route chooser dialog. The provided callback should be
 * called when the dialog has to be dismissed.
 * @param mediaRouteDynamicChooserDialog The media route chooser dialog for dynamic group.
 * @param mediaRouteControllerDialog The media route controller dialog. The provided callback should
 * be called when the dialog has to be dismissed.
 * @param mediaRouteDynamicControllerDialog The media route controller dialog for dynamic group.
 * @param onDialogTypeChange The callback used to notify when the dialog type has changed.
 * @param permissionDeniedDialog The dialog shown when the local network permission was denied. The
 * provided callback should be called when the dialog has to be dismissed.
 */
@Composable
public fun MediaRouteButton(
    modifier: Modifier = Modifier,
    routeSelector: MediaRouteSelector = MediaRouteSelector.EMPTY,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    mediaRouteChooserDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = { onDismissRequest ->
        MediaRouteChooserDialog(
            routeSelector = routeSelector,
            onDismissRequest = onDismissRequest,
        )
    },
    // TODO Implement the correct dialog (see https://github.com/SRGSSR/MediaMaestro/issues/18)
    mediaRouteDynamicChooserDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = mediaRouteChooserDialog,
    mediaRouteControllerDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = { onDismissRequest ->
        MediaRouteControllerDialog(
            routeSelector = routeSelector,
            onDismissRequest = onDismissRequest,
        )
    },
    // TODO Implement the correct dialog (see https://github.com/SRGSSR/MediaMaestro/issues/19)
    mediaRouteDynamicControllerDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = mediaRouteControllerDialog,
    onDialogTypeChange: (dialogType: DialogType) -> Unit = {},
    permissionDeniedDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = { onDismissRequest ->
        PermissionDeniedDialog(onDismissRequest = onDismissRequest)
    },
) {
    val context = LocalContext.current.applicationContext
    val viewModel = viewModel<MediaRouteButtonViewModel>(
        key = routeSelector.toString(),
        factory = MediaRouteButtonViewModel.Factory(context, routeSelector),
    )
    val castConnectionState by viewModel.castConnectionState.collectAsState()
    val dialogType by viewModel.dialogType.collectAsState(DialogType.None)
    val fixedIcon by viewModel.fixedIcon.collectAsState()

    LaunchedEffect(dialogType) {
        onDialogTypeChange(dialogType)
    }

    MediaRouteButton(
        state = castConnectionState,
        fixedIcon = fixedIcon,
        colors = colors,
        modifier = modifier,
        onClick = rememberShowDialogAction(onPermissionResult = viewModel::onLocalNetworkPermissionResult),
    )

    when (dialogType) {
        DialogType.Chooser -> mediaRouteChooserDialog(viewModel::hideDialog)
        DialogType.DynamicChooser -> mediaRouteDynamicChooserDialog(viewModel::hideDialog)
        DialogType.Controller -> mediaRouteControllerDialog(viewModel::hideDialog)
        DialogType.DynamicController -> mediaRouteDynamicControllerDialog(viewModel::hideDialog)
        DialogType.PermissionDenied -> permissionDeniedDialog(viewModel::hideDialog)
        DialogType.None -> Unit
    }
}

/**
 * Kept for binary compatibility with code compiled before the `permissionDeniedDialog` parameter was added.
 */
@Composable
@Deprecated("Kept for binary compatibility", level = DeprecationLevel.HIDDEN)
public fun MediaRouteButton(
    modifier: Modifier = Modifier,
    routeSelector: MediaRouteSelector = MediaRouteSelector.EMPTY,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    mediaRouteChooserDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = { onDismissRequest ->
        MediaRouteChooserDialog(
            routeSelector = routeSelector,
            onDismissRequest = onDismissRequest,
        )
    },
    mediaRouteDynamicChooserDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = mediaRouteChooserDialog,
    mediaRouteControllerDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = { onDismissRequest ->
        MediaRouteControllerDialog(
            routeSelector = routeSelector,
            onDismissRequest = onDismissRequest,
        )
    },
    mediaRouteDynamicControllerDialog: @Composable (onDismissRequest: () -> Unit) -> Unit = mediaRouteControllerDialog,
    onDialogTypeChange: (dialogType: DialogType) -> Unit = {},
) {
    MediaRouteButton(
        modifier = modifier,
        routeSelector = routeSelector,
        colors = colors,
        mediaRouteChooserDialog = mediaRouteChooserDialog,
        mediaRouteDynamicChooserDialog = mediaRouteDynamicChooserDialog,
        mediaRouteControllerDialog = mediaRouteControllerDialog,
        mediaRouteDynamicControllerDialog = mediaRouteDynamicControllerDialog,
        onDialogTypeChange = onDialogTypeChange,
    )
}

/**
 * Dialog informing the user that the local network permission, required to discover devices on
 * Android 17+, was denied. It offers to open the application settings, where the permission can be
 * granted.
 *
 * @param modifier The [Modifier] to be applied to this dialog.
 * @param message The message of the dialog.
 * @param buttonText The text of the confirm button.
 * @param onClickRequest The action to perform when the confirm button is clicked. The dialog is
 * dismissed afterward. By default, the application settings are opened.
 * @param onDismissRequest The action to perform when this dialog is dismissed.
 *
 * @see MediaRouteButton
 */
@Composable
public fun PermissionDeniedDialog(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.media_maestro_local_network_permission_denied),
    buttonText: String = stringResource(R.string.media_maestro_open_settings),
    onClickRequest: (context: Context) -> Unit = { context ->
        context.startActivity(LocalNetworkPermission.createSettingsIntent(context))
    },
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onClickRequest(context)
                    onDismissRequest()
                },
            ) {
                Text(text = buttonText)
            }
        },
        modifier = modifier,
        text = {
            Text(text = message)
        },
    )
}

/**
 * Remember the action to perform when the button is clicked. If the local network permission is
 * missing, it is requested first. [onPermissionResult] is then called with `true` if the
 * permission is granted or not required, `false` otherwise.
 *
 * @see LocalNetworkPermission
 */
@Composable
private fun rememberShowDialogAction(onPermissionResult: (granted: Boolean) -> Unit): () -> Unit {
    // The permission can't be requested without an ActivityResultRegistryOwner (in previews, for example)
    if (LocalActivityResultRegistryOwner.current == null) {
        return { onPermissionResult(true) }
    }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult,
    )

    return {
        val permission = LocalNetworkPermission.permission
        if (permission != null && LocalNetworkPermission.isMissing(context)) {
            permissionLauncher.launch(permission)
        } else {
            onPermissionResult(true)
        }
    }
}

@Composable
@VisibleForTesting
internal fun MediaRouteButton(
    state: CastConnectionState,
    fixedIcon: Boolean,
    colors: IconButtonColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        colors = colors,
    ) {
        CastIcon(
            state = if (fixedIcon) CastConnectionState.Disconnected else state,
            contentDescription = stringResource(state.contentDescriptionRes),
        )
    }
}
