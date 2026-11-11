package com.rykersoft.appmanager.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.rykersoft.appmanager.ui.theme.Hub
import kotlinx.coroutines.launch

// The screenshot viewer: a strip of thumbnails and a pager you can pinch and pan.

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ZoomableScreenshotImage(
    url: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    onZoomedChanged: (Boolean) -> Unit = {},
    onIntrinsicAspect: ((Float) -> Unit)? = null
) {
    var scale by remember(url) { mutableFloatStateOf(1f) }
    var offset by remember(url) { mutableStateOf(Offset.Zero) }
    var containerWidth by remember { mutableFloatStateOf(0f) }
    var containerHeight by remember { mutableFloatStateOf(0f) }
    var imageAspect by remember(url) { mutableFloatStateOf(0f) }
    val context = LocalContext.current

    fun fittedImageSize(cw: Float, ch: Float, aspect: Float): Pair<Float, Float> {
        if (cw <= 0f || ch <= 0f) return 0f to 0f
        if (aspect <= 0f) return cw to ch
        val containerAspect = cw / ch
        return if (aspect > containerAspect) {
            cw to (cw / aspect)
        } else {
            (ch * aspect) to ch
        }
    }

    fun clampOffset(raw: Offset, atScale: Float): Offset {
        if (atScale <= 1.01f || containerWidth <= 0f || containerHeight <= 0f) return Offset.Zero
        val (drawnW, drawnH) = fittedImageSize(containerWidth, containerHeight, imageAspect)
        // Pan only after the scaled image exceeds the viewport (Fit letterboxing).
        val maxX = ((drawnW * atScale - containerWidth) / 2f).coerceAtLeast(0f)
        val maxY = ((drawnH * atScale - containerHeight) / 2f).coerceAtLeast(0f)
        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY)
        )
    }

    LaunchedEffect(isActive) {
        if (!isActive && scale != 1f) {
            scale = 1f
            offset = Offset.Zero
        }
    }

    LaunchedEffect(scale, isActive) {
        if (isActive) onZoomedChanged(scale > 1.01f)
    }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        offset = if (newScale <= 1.01f) {
            Offset.Zero
        } else {
            clampOffset(offset + panChange, newScale)
        }
    }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .onSizeChanged {
                containerWidth = it.width.toFloat()
                containerHeight = it.height.toFloat()
                offset = clampOffset(offset, scale)
            }
            .transformable(
                state = transformableState,
                enabled = isActive,
                canPan = { scale > 1.01f }
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    // Zoom from the center of the viewport so the image can grow into letterbox space.
                    transformOrigin = TransformOrigin.Center
                },
            contentScale = ContentScale.Fit,
            onSuccess = { success ->
                val size = success.painter.intrinsicSize
                if (size.width > 0f && size.height > 0f) {
                    val next = (size.width / size.height).coerceIn(0.4f, 2.5f)
                    imageAspect = next
                    onIntrinsicAspect?.invoke(next)
                }
            }
        )
    }
}

/** Swipeable main screenshot preview with pinch-zoom on the active page. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SwipeableZoomableScreenshotPreview(
    screenshots: List<String>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: Dp = 420.dp,
    fallbackAspectRatio: Float = 9f / 16f
) {
    val pageCount = screenshots.size.coerceAtLeast(1)
    val safeIndex = currentIndex.coerceIn(0, pageCount - 1)
    val pagerState = rememberPagerState(
        initialPage = safeIndex,
        pageCount = { pageCount }
    )
    var isZoomed by remember { mutableStateOf(false) }
    val aspectByUrl = remember { mutableMapOf<String, Float>() }
    var aspectRatio by remember { mutableFloatStateOf(fallbackAspectRatio) }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != currentIndex) {
            onIndexChange(pagerState.currentPage)
        }
        val url = screenshots.getOrNull(pagerState.currentPage)
        aspectRatio = url?.let { aspectByUrl[it] } ?: fallbackAspectRatio
    }

    LaunchedEffect(pagerState.settledPage) {
        isZoomed = false
    }

    LaunchedEffect(safeIndex) {
        if (pagerState.currentPage != safeIndex && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(safeIndex)
        }
    }

    // Full-width viewport: image is Fit/letterboxed at 1x; pinch-zoom grows into unused space.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val ratio = aspectRatio.coerceIn(0.4f, 2.5f)
        val heightForImage = (maxWidth / ratio).coerceIn(180.dp, maxHeight)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightForImage)
                .animateContentSize()
                .border(Hub.D.border, Hub.C.border)
                .background(Hub.C.surface)
                .clip(RectangleShape),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isZoomed && pageCount > 1,
                beyondViewportPageCount = 1
            ) { page ->
                val isActive = page == pagerState.currentPage
                ZoomableScreenshotImage(
                    url = screenshots.getOrNull(page),
                    contentDescription = "Screenshot ${page + 1}",
                    modifier = Modifier.fillMaxSize(),
                    isActive = isActive,
                    onZoomedChanged = { zoomed ->
                        if (isActive) isZoomed = zoomed
                    },
                    onIntrinsicAspect = { next ->
                        val url = screenshots.getOrNull(page)
                        if (url != null) aspectByUrl[url] = next
                        if (isActive && aspectRatio != next) {
                            aspectRatio = next
                        }
                    }
                )
            }
        }
    }
}

/** Carousel thumb sized from image aspect ratio; always shows the full screenshot. */
@Composable
internal fun AdaptiveScreenshotThumbnail(
    url: String,
    contentDescription: String,
    fixedHeight: Dp,
    fallbackAspectRatio: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var aspectRatio by remember(url) { mutableFloatStateOf(fallbackAspectRatio) }
    val width = (fixedHeight * aspectRatio.coerceIn(0.4f, 2.5f)).coerceIn(72.dp, 450.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(fixedHeight)
            .animateContentSize()
            .border(Hub.D.border, Hub.C.border)
            .background(Hub.C.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .size(Size.ORIGINAL)
                .precision(Precision.EXACT)
                .crossfade(false)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            onSuccess = { success ->
                val size = success.painter.intrinsicSize
                if (size.width > 0f && size.height > 0f) {
                    val next = (size.width / size.height).coerceIn(0.4f, 2.5f)
                    if (aspectRatio != next) aspectRatio = next
                }
            }
        )
    }
}

