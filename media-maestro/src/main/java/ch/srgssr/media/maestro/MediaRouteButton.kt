/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter

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
 * @param permissionDeniedDialog The permission denied dialog, dialog displayed if an Android 17+
 * refuses the ACCESS_LOCAL_NETWORK permission (Nearby devices).
 *
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
    permissionDeniedDialog: @Composable (context: Context, onDismissRequest: () -> Unit) -> Unit =
        { context, onDismissRequest ->
            PermissionDeniedDialog(
                context = context,
                message = "Permission not granted",
                buttonText = "Redirect to settings",
                modifier = modifier,
                onDismissRequest = onDismissRequest
            )
        }
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
        onClick = rememberShowDialogAction(
            onPermissionGranted = {
                viewModel.showDialog()
                viewModel.permissionUpdates.value = PackageManager.PERMISSION_GRANTED
            },
            onPermissionNotGranted = {
                viewModel.showDialog()
                viewModel.permissionUpdates.value = PackageManager.PERMISSION_DENIED
            },
        ),
    )

    when (dialogType) {
        DialogType.Chooser -> mediaRouteChooserDialog(viewModel::hideDialog)
        DialogType.DynamicChooser -> mediaRouteDynamicChooserDialog(viewModel::hideDialog)
        DialogType.Controller -> mediaRouteControllerDialog(viewModel::hideDialog)
        DialogType.DynamicController -> mediaRouteDynamicControllerDialog(viewModel::hideDialog)
        DialogType.PermissionDenied -> permissionDeniedDialog(context, viewModel::hideDialog)
        DialogType.None -> Unit
    }
}


/**
 * Remember the action to perform when the button is clicked. If the local network permission is missing, it is
 * requested first, and [onPermissionGranted] is called once the user answered.
 */
@Composable
private fun rememberShowDialogAction(
    onPermissionGranted: () -> Unit,
    onPermissionNotGranted: () -> Unit,
): () -> Unit {
    // The permission can't be requested without an ActivityResultRegistryOwner (in previews, for example)
    if (LocalActivityResultRegistryOwner.current == null) {
        return onPermissionGranted
    }

    val context = LocalContext.current
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission())
        { granted ->
            if (granted) onPermissionGranted()
            else onPermissionNotGranted()
        }

    val permission = LocalNetworkPermission.permission

    return {
        if (permission == null) onPermissionGranted()
        else when {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_LOCAL_NETWORK
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                onPermissionGranted()
            }

            else -> {
                // You can directly ask for the permission.
                permissionLauncher.launch(permission)
            }
        }
    }
}

/**
 * This class implements the dialog when the permission to ACCESS_LOCAL_NETWORK is denied
 * (Nearby devices).
 *
 * This dialog should show a warning to the user and provide an appropriate action afterward.
 *
 * @param context The [Context] to be applied to this dialog.
 * @param message The message of the dialog.
 * @param buttonText The button text of the dialog (positive button).
 * @param modifier The [Modifier] to be applied to this dialog.
 * @param onDismissRequest The action to perform when this dialog is dismissed.
 * @param onClickRequest The action to perform when the button is clicked.
 *
 */
@Composable
public fun PermissionDeniedDialog(
    context: Context,
    message: String,
    buttonText: String,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    onClickRequest: (context: Context) -> Unit = { context ->
        context.startActivity(
            LocalNetworkPermission.createSettingsIntent(
                context
            )
        )
    }
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onClickRequest.invoke(context)
                },
            ) {
                Text(text = buttonText)
            }
        },
        modifier = modifier,
        text = {
            Text(
                text = message,
            )
        },
    )
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
