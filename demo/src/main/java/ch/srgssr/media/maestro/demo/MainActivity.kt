/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.media.MediaRouteSelector
import ch.srgssr.media.maestro.MediaRouteButton

class MainActivity : FragmentActivity() {
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            var useMaestro by remember { mutableStateOf(true) }

            DemoTheme {
                MainView(
                    player = mainViewModel.player,
                    useMaestro = useMaestro,
                    routeSelector = mainViewModel.routeSelector,
                    modifier = Modifier.fillMaxSize(),
                    onFabClick = { useMaestro = !useMaestro },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MainView(
    player: Player,
    useMaestro: Boolean,
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
    onFabClick: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    CastIcon(
                        useMaestro = useMaestro,
                        routeSelector = routeSelector,
                    )
                },
            )
        },
        floatingActionButton = {
            SwitchImplementationButton(
                useMaestro = useMaestro,
                onClick = onFabClick,
            )
        },
    ) { innerPadding ->
        Player(
            player = player,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.5f),
        )
    }
}

@Composable
private fun SwitchImplementationButton(
    useMaestro: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ExtendedFloatingActionButton(
        text = {
            val textResId = if (useMaestro) R.string.use_androidx else R.string.use_maestro

            Text(text = stringResource(textResId))
        },
        icon = {
            if (useMaestro) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_media_maestro),
                    contentDescription = null,
                )
            }
        },
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun CastIcon(
    useMaestro: Boolean,
    routeSelector: MediaRouteSelector,
    modifier: Modifier = Modifier,
) {
    if (useMaestro) {
        MediaRouteButton(
            modifier = modifier,
            routeSelector = routeSelector,
        )
    } else {
        AndroidView(
            factory = { context ->
                MediaRouteButton(context).apply {
                    this.routeSelector = routeSelector
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun Player(
    player: Player,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        modifier = modifier,
    )
}

@Composable
@PreviewLightDark
private fun MainViewAndroidXPreview() {
    DemoTheme {
        Surface {
            val context = LocalContext.current

            MainView(
                player = ExoPlayer.Builder(context).build(),
                useMaestro = false,
                routeSelector = MediaRouteSelector.EMPTY,
                onFabClick = {},
            )
        }
    }
}

@Composable
@PreviewLightDark
private fun MainViewComposePreview() {
    DemoTheme {
        Surface {
            val context = LocalContext.current

            MainView(
                player = ExoPlayer.Builder(context).build(),
                useMaestro = true,
                routeSelector = MediaRouteSelector.EMPTY,
                onFabClick = {},
            )
        }
    }
}
