/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest

class IconsScreenshot {
    @Composable
    @PreviewTest
    @PreviewLightDark
    private fun IconPreview(@PreviewParameter(IconProvider::class) imageVector: ImageVector) {
        ScreenshotTheme {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
        }
    }
}

class IconProvider : PreviewParameterProvider<ImageVector> {
    override val values = sequenceOf(
        Icons.Audiotrack,
        Icons.Cast,
        Icons.Close,
        Icons.ExpandMore,
        Icons.Pause,
        Icons.PlayArrow,
        Icons.Speaker,
        Icons.SpeakerGroup,
        Icons.Stop,
        Icons.Tv,
        Icons.Wifi,
    )
}
