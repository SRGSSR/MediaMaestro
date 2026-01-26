/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan

/**
 * Display a Cast icon, based on the current connection state to Cast. If [state] is:
 *
 * - [CastConnectionState.Disconnected], an icon is displayed, showing just a border, with three arcs
 * in the bottom left corner.
 * - [CastConnectionState.Connected], the same icon is displayed, with the addition of a filled
 * inner rectangle.
 * - [CastConnectionState.Connecting], the same icon as for [CastConnectionState.Disconnected] is
 * displayed, but the arcs are moving outwards.
 */
@Composable
@Suppress("LongMethod")
internal fun CastIcon(
    state: CastConnectionState,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    strokeWidth: Dp = size / 12,
) {
    // Three arcs are displayed in the bottom left corner of the icon, in all states:
    // - 'start' is the innermost one.
    // - 'end' is the outermost one.
    // - 'middle' is the one in between.
    //
    // If the state is 'Connecting', two additional arcs are defined to support the animation. By
    // default, they are displayed on top of the 'start' arc.
    // When the animation is running, the following happens:
    // - 'middle' moves to 'end'.
    // - one of the additional 'start' moves to 'end'.
    // - the other additional 'start' moves to 'middle', after a small delay.

    val infiniteTransition = rememberInfiniteTransition()
    val radiusDeltaMiddleToEnd by infiniteTransition.animateFloat(
        state = state,
        from = 0f,
        to = 4f,
        durationMillis = ConnectingAnimationDuration / 2,
    )
    val radiusDeltaStartToEnd by infiniteTransition.animateFloat(
        state = state,
        from = 0f,
        to = 8f,
        durationMillis = ConnectingAnimationDuration,
    )
    val radiusDeltaStartToMiddle by infiniteTransition.animateFloat(
        state = state,
        from = 0f,
        to = 4f,
        durationMillis = ConnectingAnimationDuration / 2,
        delayMillis = ConnectingAnimationDuration / 2,
    )
    val connectedRectColor by animateColorAsState(
        targetValue = if (state == CastConnectionState.Connected) color else Color.Transparent,
    )
    val strokeWidthPx = with(LocalDensity.current) {
        strokeWidth.toPx()
    }

    Canvas(
        modifier = modifier
            .size(size)
            .aspectRatio(9f / 7.5f)
            .padding(1.dp)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics {
                        this.contentDescription = contentDescription
                        this.role = Role.Image
                    }
                } else {
                    Modifier
                }
            ),
    ) {
        val arcUnitWidth = this.size.height * ArcHeightRatio / ArcUnitCount
        val arcCenter = Offset(0f, this.size.height)

        drawBorder(color, strokeWidthPx)

        // 'start' arc
        drawArc(
            color = color,
            center = arcCenter,
            radius1 = 0f,
            radius2 = 3f * arcUnitWidth,
        )

        // 'middle' arc
        drawArc(
            color = color,
            center = arcCenter,
            radius1 = (7f + radiusDeltaMiddleToEnd) * arcUnitWidth,
            radius2 = (5f + radiusDeltaMiddleToEnd) * arcUnitWidth,
        )

        // 'end' arc
        drawArc(
            color = color,
            center = arcCenter,
            radius1 = 11f * arcUnitWidth,
            radius2 = 9f * arcUnitWidth,
        )

        if (state == CastConnectionState.Connected) {
            drawConnectedRect(
                color = connectedRectColor,
                center = arcCenter,
                strokeWidth = strokeWidthPx,
            )
        } else if (state == CastConnectionState.Connecting) {
            // 'start' arc that moves to 'end'
            drawArc(
                color = color,
                center = arcCenter,
                radius1 = (3f + radiusDeltaStartToEnd) * arcUnitWidth,
                radius2 = (1f + radiusDeltaStartToEnd) * arcUnitWidth,
            )

            // 'start' arc that moves to 'middle'
            drawArc(
                color = color,
                center = arcCenter,
                radius1 = (3f + radiusDeltaStartToMiddle) * arcUnitWidth,
                radius2 = (1f + radiusDeltaStartToMiddle) * arcUnitWidth,
            )
        }
    }
}

@Composable
private fun InfiniteTransition.animateFloat(
    state: CastConnectionState,
    from: Float,
    to: Float,
    durationMillis: Int,
    delayMillis: Int = 0,
): State<Float> {
    return if (state == CastConnectionState.Connecting) {
        animateFloat(
            initialValue = from,
            targetValue = to,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    delayMillis = delayMillis,
                    easing = LinearEasing,
                )
            ),
        )
    } else {
        remember(from) {
            mutableFloatStateOf(from)
        }
    }
}

private fun DrawScope.drawBorder(
    color: Color,
    strokeWidth: Float,
) {
    val width = size.width
    val height = size.height
    val arcsSize = height * ArcHeightRatio
    val halfStrokeWidth = strokeWidth / 2f
    val cornerTopLeft = Offset(halfStrokeWidth, halfStrokeWidth)
    val cornerTopRight = Offset(width - halfStrokeWidth, halfStrokeWidth)
    val cornerBottomRight = Offset(width - halfStrokeWidth, height - halfStrokeWidth)
    val borderPoints = listOf(
        Offset(halfStrokeWidth, height - arcsSize), cornerTopLeft,
        cornerTopLeft, cornerTopRight,
        cornerTopRight, cornerBottomRight,
        cornerBottomRight, Offset(arcsSize, height - halfStrokeWidth),
    )

    drawPoints(
        points = borderPoints,
        pointMode = PointMode.Lines,
        color = color,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawArc(
    color: Color,
    center: Offset,
    radius1: Float,
    radius2: Float,
) {
    val path = Path()
    path.arcTo(
        rect = Rect(center, radius1),
        startAngleDegrees = 0f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false,
    )
    path.arcTo(
        rect = Rect(center, radius2),
        startAngleDegrees = -90f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false,
    )
    path.close()

    drawPath(path, color)
}

private fun DrawScope.drawConnectedRect(
    color: Color,
    center: Offset,
    strokeWidth: Float,
) {
    val width = size.width
    val height = size.height
    val arcsSize = (ArcUnitCount - 1f) * height * ArcHeightRatio / ArcUnitCount
    val doubleStrokeWidth = 2f * strokeWidth
    val startAngle = -atan(doubleStrokeWidth / arcsSize)
    val sweepAngle = -(PI.toFloat() / 2f + 2f * startAngle)
    val path = Path()
    path.moveTo(doubleStrokeWidth, height - arcsSize)
    path.lineTo(doubleStrokeWidth, doubleStrokeWidth)
    path.lineTo(width - doubleStrokeWidth, doubleStrokeWidth)
    path.lineTo(width - doubleStrokeWidth, height - doubleStrokeWidth)
    path.lineTo(arcsSize, height - doubleStrokeWidth)
    path.arcToRad(
        rect = Rect(center, arcsSize),
        startAngleRadians = startAngle,
        sweepAngleRadians = sweepAngle,
        forceMoveTo = false,
    )
    path.close()

    drawPath(path, color)
}

@Preview
@Composable
private fun CastIconConnectedPreview() {
    MaterialTheme {
        CastIcon(
            state = CastConnectionState.Connected,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun CastIconConnectingPreview() {
    MaterialTheme {
        CastIcon(
            state = CastConnectionState.Connecting,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun CastIconDisconnectedPreview() {
    MaterialTheme {
        CastIcon(
            state = CastConnectionState.Disconnected,
            contentDescription = null,
        )
    }
}

private const val ArcHeightRatio = 0.75f
private const val ArcUnitCount = 14
private const val ConnectingAnimationDuration = 1200
