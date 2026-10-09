package com.maxrave.simpmusic.ui.component

import com.maxrave.simpmusic.ui.theme.currentTheme
import com.maxrave.simpmusic.ui.theme.luxeBackground
import com.maxrave.simpmusic.ui.theme.graphiteBackground
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.theme.LocalIsDarkTheme
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.currentTheme
import com.maxrave.simpmusic.ui.theme.luxeBackground
import com.maxrave.simpmusic.ui.theme.graphiteBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

private val CapsuleShape = RoundedCornerShape(percent = 50)
private val TabWidth = 96.dp
internal val BarHeight = 64.dp
private val BlobHeight = 56.dp
private val BarInset = 6.dp

@Composable
fun LiquidGlassTabBar(
    tabs: List<BottomNavScreen>,
    selectedTab: Int,
    backdrop: PlatformBackdrop,
    layer: GraphicsLayer,
    luminance: State<Float>,
    modifier: Modifier = Modifier,
    availableWidth: Dp = Dp.Unspecified,
    onTabSelected: (Int) -> Unit,
    collapsedContent: (@Composable () -> Unit)? = null,
) {
    val density = LocalDensity.current
    val tabsCount = tabs.size
    val tabWidth = tabWidthFor(tabsCount, availableWidth)
    val fullWidth = tabWidth * tabsCount + BarInset * 2
    val tabWidthPx = with(density) { tabWidth.toPx() }
    val currentTabWidthPx by rememberUpdatedState(tabWidthPx)
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val animationScope = rememberCoroutineScope()
    val barInteraction = rememberGlassInteraction()
    val isDark = LocalIsDarkTheme.current

    var currentIndex by remember { mutableIntStateOf(selectedTab.coerceAtLeast(0)) }
    val draggedFlag = remember { booleanArrayOf(false) }

    val dampedDrag =
        remember(animationScope, tabsCount) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedTab.coerceAtLeast(0).toFloat(),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 76f / 56f,
                onDragStarted = { draggedFlag[0] = false },
                onDragStopped = {
                    if (draggedFlag[0]) {
                        val target = targetValue.roundToInt().coerceIn(0, tabsCount - 1)
                        currentIndex = target
                        animateToValue(target.toFloat())
                    }
                },
                onDrag = { _, dragAmount ->
                    if (dragAmount.x != 0f) draggedFlag[0] = true
                    updateValue(
                        (targetValue + dragAmount.x / currentTabWidthPx * if (isLtr) 1f else -1f)
                            .coerceIn(0f, (tabsCount - 1).toFloat()),
                    )
                },
            )
        }

    LaunchedEffect(selectedTab) {
        if (selectedTab >= 0 && currentIndex != selectedTab) currentIndex = selectedTab
    }
    LaunchedEffect(dampedDrag) {
        snapshotFlow { currentIndex }
            .drop(1)
            .collectLatest { index ->
                dampedDrag.animateToValue(index.toFloat())
                if (draggedFlag[0]) onTabSelected(index)
            }
    }

    Box(
        modifier =
            modifier
                .height(BarHeight)
                .width(fullWidth)
                .pointerInput(barInteraction) { barInteraction.detectPress(this) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.matchParentSize().drawInteractiveGlass(isDark, backdrop, layer, { luminance.value }, CapsuleShape, barInteraction))

        Box(Modifier.matchParentSize().unfoldingTabs(fullWidth, CapsuleShape)) {
            Box(
                Modifier
                    .wrapContentSize(Alignment.CenterStart, unbounded = true)
                    .size(fullWidth, BarHeight),
                contentAlignment = Alignment.CenterStart,
            ) {
                Box(
                    Modifier
                        .graphicsLayer {
                            translationX =
                                (if (isLtr) dampedDrag.value else (tabsCount - 1) - dampedDrag.value) * tabWidthPx +
                                BarInset.toPx()
                        }.drawBackdrop(
                            backdrop = backdrop,
                            shape = { CapsuleShape },
                            effects = {
                                val l = (luminance.value * 2f - 1f).let { sign(it) * it * it }
                                val progress = dampedDrag.pressProgress
                                vibrancy()
                                colorControls(
                                    brightness = 0.05f,
                                    contrast = 1f,
                                    saturation = 1.5f,
                                )
                                blur(
                                    (if (l > 0f) lerp(8f.dp.toPx(), 16f.dp.toPx(), l) else lerp(8f.dp.toPx(), 2f.dp.toPx(), -l)) +
                                        20f.dp.toPx(),
                                )
                                lens(10f.dp.toPx() * progress, 14f.dp.toPx() * progress, chromaticAberration = true)
                            },
                            highlight = { Highlight.Default.copy(alpha = 0.6f) },
                            shadow = { Shadow(radius = 4f.dp, alpha = 0.4f) },
                            innerShadow = {
                                val progress = dampedDrag.pressProgress
                                InnerShadow(radius = 8f.dp * progress, alpha = progress)
                            },
                            layerBlock = {
                                scaleX = dampedDrag.scaleX
                                scaleY = dampedDrag.scaleY
                                val velocity = dampedDrag.velocity / 10f
                                scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
    val lumNorm = ((luminance.value - 0.3f) / 0.5f).coerceIn(0f, 1f)
    
    // Theme ke hisaab se glass ka rang set kar rahe hain
    val veilColor = when (currentTheme) {
        "LUXE_GOLDEN" -> luxeBackground
        "CHRONO_GRAPHITE" -> graphiteBackground
        else -> Color.Black // Default
    }
    
    // Dark theme mein thoda transparent taaki background dikhe
    val darken =
        if (isDark) lerp(0.30f, 0.65f, lumNorm) else lerp(0.06f, 0.14f, lumNorm)
    
    drawRect(veilColor.copy(alpha = darken))
},
                        ).width(tabWidth)
                        .height(BlobHeight),
                )

                Row(
                    Modifier
                        .matchParentSize()
                        .padding(horizontal = BarInset)
                        .then(dampedDrag.modifier),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { position, screen ->
                        LiquidGlassTab(
                            screen = screen,
                            selected = currentIndex == position,
                            width = tabWidth,
                        ) {
                            if (position == currentIndex) {
                                onTabSelected(position)
                            } else {
                                currentIndex = position
                                onTabSelected(position)
                            }
                        }
                    }
                }
            }
        }

        if (collapsedContent != null) {
            Box(Modifier.matchParentSize().foldedCircle(fullWidth), contentAlignment = Alignment.Center) {
                collapsedContent()
            }
        }
    }
}

private fun tabWidthFor(
    tabsCount: Int,
    availableWidth: Dp,
): Dp =
    if (availableWidth.isSpecified && availableWidth > 0.dp) {
        ((availableWidth - BarInset * 2) / tabsCount).coerceAtMost(TabWidth)
    } else {
        TabWidth
    }

internal fun liquidGlassTabBarWidth(
    tabsCount: Int,
    availableWidth: Dp,
): Dp = tabWidthFor(tabsCount, availableWidth) * tabsCount + BarInset * 2

@Composable
private fun LiquidGlassTab(
    screen: BottomNavScreen,
    selected: Boolean,
    width: Dp,
    onClick: () -> Unit,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Column(
        Modifier
            .width(width)
            .fillMaxHeight()
            .clip(CapsuleShape)
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            screen.icon()
            Text(
                text = stringResource(screen.title),
                style = typo().bodySmall,
                color = color,
                maxLines = 1,
            )
        }
    }
}

class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
) {
    private val valueAnimationSpec = spring(1f, 1000f, visibilityThreshold)
    private val velocityAnimationSpec = spring(0.5f, 300f, visibilityThreshold * 10f)
    private val pressProgressAnimationSpec = spring(1f, 1000f, 0.001f)
    private val scaleXAnimationSpec = spring(0.6f, 250f, 0.001f)
    private val scaleYAnimationSpec = spring(0.7f, 250f, 0.001f)

    private val valueAnimation = Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation = Animatable(0f, 5f)
    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val scaleXAnimation = Animatable(initialScale, 0.001f)
    private val scaleYAnimation = Animatable(initialScale, 0.001f)

    private val mutatorMutex = MutatorMutex()
    private val velocityTracker = VelocityTracker()

    val value: Float get() = valueAnimation.value
    val targetValue: Float get() = valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    // ... (Agar iske baad file me aur code hai, toh usko aap delete mat karein, bas is code ke aage paste kar dein) ...
}