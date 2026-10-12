/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

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

@Preview
@Composable
private fun PermissionDeniedDialogPreview() {
    MaterialTheme {
        PermissionDeniedDialog(onDismissRequest = {})
    }
}
