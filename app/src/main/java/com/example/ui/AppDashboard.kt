package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import coil.size.Precision
import com.example.ui.theme.*
import com.example.util.ApkManager
import kotlinx.coroutines.launch

/** Pinch-to-zoom / pan screenshot. Scales within the full preview viewport (not a shrink-wrapped image frame). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZoomableScreenshotImage(
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
private fun SwipeableZoomableScreenshotPreview(
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
                .border(2.dp, NeoBorder)
                .background(NeoSurface)
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
private fun AdaptiveScreenshotThumbnail(
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
            .border(1.5.dp, NeoBorder)
            .background(NeoSurface)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDashboard(
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var selectedAppForDetail by remember { mutableStateOf<AppUiItem?>(null) }
    var selectedScreenshotIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showPlatformDropdown by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }
    var selectedPlatform by remember { mutableStateOf("Android") }

    val snackbarHostState = remember { SnackbarHostState() }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshLocalInstallations()
                viewModel.checkPendingInstall()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // After launching the system installer, background RykerSoft so Play Protect's
    // "Send app for a security check?" sheet stays visible long enough to tap.
    LaunchedEffect(uiState.backgroundForInstall) {
        if (uiState.backgroundForInstall) {
            kotlinx.coroutines.delay(250)
            (context as? android.app.Activity)?.moveTaskToBack(true)
            viewModel.clearBackgroundForInstall()
        }
    }

    // Side-effects for error or info popups
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let { info ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = info,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearInfo()
            }
        }
    }

    // Filter and sort apps based on search query, selected filter type, and sort option
    val filteredApps = remember(uiState.apps, searchQuery, uiState.filterType, uiState.sortOption, selectedPlatform) {
        if (selectedPlatform == "Desktop") {
            emptyList()
        } else {
            val list = uiState.apps.filter { app ->
                val matchesSearch = app.name.contains(searchQuery, ignoreCase = true) || 
                        app.packageName.contains(searchQuery, ignoreCase = true)
                
                val matchesFilter = when (uiState.filterType) {
                    FilterType.ALL -> true
                    FilterType.GAMES -> app.isGame
                    FilterType.APPS -> !app.isGame
                    FilterType.UPDATES_AVAILABLE -> app.isOutdated
                    FilterType.INSTALLED -> app.isInstalled
                    FilterType.NOT_INSTALLED -> !app.isInstalled
                }
                matchesSearch && matchesFilter
            }
            when (uiState.sortOption) {
                SortOption.RECENTLY_UPDATED -> list.sortedWith(compareByDescending<AppUiItem> { it.latestVersionCode }.thenBy { it.name })
                SortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                SortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                SortOption.VERSION_CODE_DESC -> list.sortedByDescending { it.latestVersionCode }
                SortOption.STATUS -> list.sortedWith(compareBy<AppUiItem> { 
                    when {
                        it.isOutdated -> 0
                        it.isInstalled -> 1
                        else -> 2
                    }
                }.thenByDescending { it.latestVersionCode })
            }
        }
    }

    val (totalCount, gamesCount, appsCount, updatesCount, installedCount) = remember(uiState.apps) {
        val total = uiState.apps.size
        var games = 0
        var apps = 0
        var updates = 0
        var installed = 0
        for (app in uiState.apps) {
            if (app.isGame) games++ else apps++
            if (app.isOutdated) updates++
            if (app.isInstalled) installed++
        }
        Quintuple(total, games, apps, updates, installed)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NeoBg)
    ) {
        // Background Graph Paper Grid
        MemphisBackgroundGrid()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_dashboard_scaffold"),
            containerColor = Color.Transparent,
            topBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeoBg)
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title Header with Store Settings Cog Button (moved to top header position)
                        RykerSoftTitleHeader(
                            onOpenSettings = { showSettingsDialog = true }
                        )

                        // Action Controls Bar (Platform Dropdown, App Count, Sync)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box {
                                    TagChip(
                                        text = "${selectedPlatform.uppercase()} ▾",
                                        bgColor = NeoMutedBg,
                                        textColor = NeoText,
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedPlatform == "Desktop") Icons.Default.Computer else Icons.Default.Android,
                                                contentDescription = selectedPlatform,
                                                tint = NeoText,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        },
                                        modifier = Modifier
                                            .testTag("platform_dropdown_chip")
                                            .clickable { showPlatformDropdown = true }
                                    )

                                    DropdownMenu(
                                        expanded = showPlatformDropdown,
                                        onDismissRequest = { showPlatformDropdown = false },
                                        modifier = Modifier
                                            .background(NeoSurface)
                                            .border(2.dp, NeoBorder)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Android,
                                                        contentDescription = "Android",
                                                        tint = if (selectedPlatform == "Android") NeoCyan else NeoText,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Android",
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedPlatform == "Android") NeoCyan else NeoText
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedPlatform = "Android"
                                                showPlatformDropdown = false
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Computer,
                                                        contentDescription = "Desktop",
                                                        tint = if (selectedPlatform == "Desktop") NeoCyan else NeoText,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Desktop",
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selectedPlatform == "Desktop") NeoCyan else NeoText
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedPlatform = "Desktop"
                                                showPlatformDropdown = false
                                            }
                                        )
                                    }
                                }

                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Search Toggle Button (Magnifying Glass)
                                NeoButton(
                                    onClick = { showSearchBar = !showSearchBar },
                                    style = if (showSearchBar || searchQuery.isNotEmpty()) NeoButtonStyle.ACCENT_CYAN else NeoButtonStyle.NEUTRAL_WHITE,
                                    contentPadding = PaddingValues(6.dp),
                                    modifier = Modifier.testTag("search_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Toggle Search",
                                        tint = if (showSearchBar || searchQuery.isNotEmpty()) Color.Black else NeoText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                // Sort Button (Icon Only)
                                Box {
                                    NeoButton(
                                        onClick = { showSortDropdown = true },
                                        style = if (uiState.sortOption != SortOption.RECENTLY_UPDATED) NeoButtonStyle.ACCENT_CYAN else NeoButtonStyle.NEUTRAL_WHITE,
                                        contentPadding = PaddingValues(6.dp),
                                        modifier = Modifier.testTag("sort_action_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Sort,
                                            contentDescription = "Sort Apps",
                                            tint = if (uiState.sortOption != SortOption.RECENTLY_UPDATED) Color.Black else NeoText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showSortDropdown,
                                        onDismissRequest = { showSortDropdown = false },
                                        modifier = Modifier
                                            .background(NeoSurface)
                                            .border(2.dp, NeoBorder)
                                    ) {
                                        SortOption.entries.forEach { option ->
                                            val isSelected = uiState.sortOption == option
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = option.label,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                            color = if (isSelected) NeoMagenta else NeoText,
                                                            fontSize = 12.sp
                                                        )
                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = NeoMagenta,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.setSortOption(option)
                                                    showSortDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Sync Button (Icon Only)
                                NeoButton(
                                    onClick = { viewModel.syncWithRegistry() },
                                    style = NeoButtonStyle.SECONDARY_YELLOW,
                                    contentPadding = PaddingValues(6.dp),
                                    modifier = Modifier.testTag("sync_action_button")
                                ) {
                                    if (uiState.isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.Black
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Sync Registry",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Expandable Search Bar Box
                        AnimatedVisibility(visible = showSearchBar) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                // Shadow
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .offset(x = 3.dp, y = 3.dp)
                                        .background(NeoBlack)
                                )

                                // Surface Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, NeoBorder)
                                        .background(NeoSurface)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = {
                                            Text(
                                                text = "SEARCH APPS OR GAMES...",
                                                fontSize = 11.sp,
                                                color = NeoSubtext,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Search,
                                                contentDescription = "Search Icon",
                                                tint = NeoText
                                            )
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(
                                                        Icons.Default.Clear,
                                                        contentDescription = "Clear search",
                                                        tint = NeoText
                                                    )
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        shape = RectangleShape,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("search_text_field"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = NeoText,
                                            unfocusedTextColor = NeoText,
                                            cursorColor = NeoMagenta
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Decorative Memphis Zig-Zag Banner Strip
                    MemphisZigZagBanner(height = 6.dp)
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    ) {
                        // Shadow Layer
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(NeoBlack)
                        )

                        // Main Surface Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, NeoMagenta)
                                .background(NeoSurface)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeoGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = data.visuals.message,
                                    color = NeoText,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val actionLabel = data.visuals.actionLabel
                            if (actionLabel != null) {
                                TextButton(
                                    onClick = { data.performAction() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = actionLabel.uppercase(),
                                        color = NeoYellow,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { data.dismiss() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Text(
                                        text = "✕",
                                        color = NeoSubtext,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Filter Chips Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterTagButton(
                                label = "ALL",
                                count = totalCount,
                                isSelected = uiState.filterType == FilterType.ALL,
                                selectedStyle = NeoButtonStyle.SECONDARY_YELLOW,
                                onClick = { viewModel.setFilter(FilterType.ALL) }
                            )
                        }
                        item {
                            FilterTagButton(
                                label = "GAMES",
                                count = gamesCount,
                                isSelected = uiState.filterType == FilterType.GAMES,
                                selectedStyle = NeoButtonStyle.PRIMARY_MAGENTA,
                                onClick = { viewModel.setFilter(FilterType.GAMES) }
                            )
                        }
                        item {
                            FilterTagButton(
                                label = "APPS",
                                count = appsCount,
                                isSelected = uiState.filterType == FilterType.APPS,
                                selectedStyle = NeoButtonStyle.ACCENT_CYAN,
                                onClick = { viewModel.setFilter(FilterType.APPS) }
                            )
                        }
                        item {
                            FilterTagButton(
                                label = "UPDATES",
                                count = updatesCount,
                                isSelected = uiState.filterType == FilterType.UPDATES_AVAILABLE,
                                selectedStyle = NeoButtonStyle.PRIMARY_MAGENTA,
                                onClick = { viewModel.setFilter(FilterType.UPDATES_AVAILABLE) }
                            )
                        }
                        item {
                            FilterTagButton(
                                label = "INSTALLED",
                                count = installedCount,
                                isSelected = uiState.filterType == FilterType.INSTALLED,
                                selectedStyle = NeoButtonStyle.ACTION_GREEN,
                                onClick = { viewModel.setFilter(FilterType.INSTALLED) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Main Content List
                if (uiState.apps.isEmpty()) {
                    EmptyStateView(
                        onLoadSamples = { viewModel.seedSampleApps() }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("apps_lazy_column"),
                        contentPadding = PaddingValues(start = 14.dp, top = 6.dp, end = 14.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        uiState.appManagerUpdateAvailable?.let { updateApp ->
                            item(key = "app_manager_update_banner") {
                                AppManagerUpdateBanner(
                                    app = updateApp,
                                    onUpdateClick = { viewModel.downloadAndInstall(updateApp) },
                                    onOpenDetail = {
                                        selectedAppForDetail = updateApp
                                        selectedScreenshotIndex = 0
                                    },
                                    downloadingPackage = uiState.downloadingPackage,
                                    downloadProgress = uiState.downloadProgress
                                )
                            }
                        }

                        items(filteredApps, key = { it.packageName }) { app ->
                            AppItemCard(
                                app = app,
                                onOpenDetail = { index ->
                                    selectedAppForDetail = app
                                    selectedScreenshotIndex = index
                                },
                                onLongClick = {
                                    clipboardManager.setText(AnnotatedString(app.packageName))
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Copied package ID to clipboard")
                                    }
                                },
                                onActionClick = { viewModel.downloadAndInstall(app) },
                                onLaunchClick = { ApkManager.launchApp(context, app.packageName) },
                                downloadingPackage = uiState.downloadingPackage,
                                downloadProgress = uiState.downloadProgress
                            )
                        }
                    }
                }
            }
        }

        // App Detail Dialog Modal
        selectedAppForDetail?.let { detailApp ->
            val currentApp = uiState.apps.find { it.packageName == detailApp.packageName } ?: detailApp
            AppDetailDialog(
                app = currentApp,
                initialScreenshotIndex = selectedScreenshotIndex,
                onDismiss = { selectedAppForDetail = null },
                onActionClick = { viewModel.downloadAndInstall(currentApp) },
                onLaunchClick = { ApkManager.launchApp(context, currentApp.packageName) },
                onDeleteClick = {
                    viewModel.deleteApp(currentApp.packageName)
                    selectedAppForDetail = null
                },
                downloadingPackage = uiState.downloadingPackage,
                downloadProgress = uiState.downloadProgress
            )
        }

        // Add Custom App Dialog
        if (showAddDialog) {
            AddAppDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, pkg, ver, code, url, icon, notes, isGame ->
                    viewModel.addManualApp(name, pkg, ver, code, url, icon, notes, isGame)
                    showAddDialog = false
                }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                currentUrl = uiState.registryUrl,
                currentGithubToken = uiState.githubToken,
                notificationsEnabled = uiState.notificationsEnabled,
                onDismiss = { showSettingsDialog = false },
                onSave = { url, notify, token ->
                    viewModel.updateSettings(url, notify, token)
                    showSettingsDialog = false
                },
                onLoadSamples = {
                    viewModel.seedSampleApps()
                    showSettingsDialog = false
                },
                onAddAppClick = {
                    showSettingsDialog = false
                    showAddDialog = true
                }
            )
        }
    }
}

@Composable
fun FilterTagButton(
    label: String,
    count: Int,
    isSelected: Boolean,
    selectedStyle: NeoButtonStyle,
    onClick: () -> Unit
) {
    NeoButton(
        onClick = onClick,
        style = if (isSelected) selectedStyle else NeoButtonStyle.NEUTRAL_WHITE,
        shadowOffset = if (isSelected) 2.5.dp else 1.5.dp,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label ($count)",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItemCard(
    app: AppUiItem,
    onOpenDetail: (Int) -> Unit,
    onLongClick: () -> Unit,
    onActionClick: () -> Unit,
    onLaunchClick: () -> Unit,
    downloadingPackage: String?,
    downloadProgress: Int
) {
    val isCurrentDownloading = downloadingPackage == app.packageName

    NeoCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onOpenDetail(0) },
                onLongClick = onLongClick
            )
            .testTag("app_item_${app.packageName}"),
        shadowOffset = 5.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Title & Package, Badges, Action Button
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {

                // App Title, Author/Package, Platform & Status Badges
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            color = NeoText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Tilted Status Sticker Tag
                        val (statusText, statusBg, statusTextClr, rotation) = when {
                            !app.isInstalled -> Quadruple("NEW RELEASE", NeoMagenta, Color.White, -2f)
                            app.isOutdated -> Quadruple("UPDATE READY", NeoYellow, Color.Black, 3f)
                            else -> Quadruple("INSTALLED", NeoGreen, Color.White, 0f)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        StickerBadge(
                            text = statusText,
                            bgColor = statusBg,
                            textColor = statusTextClr,
                            rotation = rotation,
                            shadowOffset = 1.5.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (app.isGame) "C++ / Engine" else "Kotlin / App",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoSubtext,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Category Tags Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TagChip(
                            text = if (app.isGame) "🎮 GAME" else "⚡ APP",
                            bgColor = if (app.isGame) Color(0xFF3B0764) else Color(0xFF0C4A6E),
                            textColor = if (app.isGame) Color(0xFFE9D5FF) else Color(0xFFBAE6FD)
                        )

                        TagChip(
                            text = "ANDROID",
                            bgColor = NeoMutedBg,
                            textColor = NeoText,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = null,
                                    tint = NeoText,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // App Description Excerpt (summary paragraph only)
            MarkdownSummaryText(
                markdown = app.summaryDescription,
                maxLines = 2,
                color = NeoText,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Screenshots Gallery (Horizontal Carousel)
            if (app.screenshots.isNotEmpty()) {
                val fallbackAspectRatio = if (app.isGame) 16f / 9f else 9f / 16f
                val cardHeight = 180.dp

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(app.screenshots.size) { index ->
                        val screenshotUrl = app.screenshots[index]
                        AdaptiveScreenshotThumbnail(
                            url = screenshotUrl,
                            contentDescription = "${app.name} Screenshot ${index + 1}",
                            fixedHeight = cardHeight,
                            fallbackAspectRatio = fallbackAspectRatio,
                            onClick = { onOpenDetail(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Stats Line + Main Action Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NeoBorder)
                    .background(NeoMutedBg)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stats line (Version tag)
                val hasDifferentVersion = app.isInstalled && !app.installedVersionName.isNullOrEmpty() && app.installedVersionName != app.latestVersionName
                val versionDisplay = if (hasDifferentVersion) {
                    "v${app.installedVersionName} → v${app.latestVersionName}"
                } else {
                    "v${app.latestVersionName}"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🕒", fontSize = 10.sp)
                    Text(
                        text = versionDisplay,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (hasDifferentVersion) NeoMagenta else NeoText
                    )
                }

                // Action Button (INSTALL / UPDATE / PLAY)
                Box {
                    if (isCurrentDownloading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.5.dp,
                                color = NeoMagenta
                            )
                            Text(
                                text = "$downloadProgress%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = NeoText
                            )
                        }
                    } else {
                        when {
                            !app.isInstalled -> {
                                NeoButton(
                                    onClick = onActionClick,
                                    style = NeoButtonStyle.PRIMARY_MAGENTA,
                                    shadowOffset = 2.dp,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("INSTALL >", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                            app.isOutdated -> {
                                NeoButton(
                                    onClick = onActionClick,
                                    style = NeoButtonStyle.SECONDARY_YELLOW,
                                    shadowOffset = 2.dp,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("UPDATE >", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                            else -> {
                                NeoButton(
                                    onClick = onLaunchClick,
                                    style = NeoButtonStyle.ACTION_GREEN,
                                    shadowOffset = 2.dp,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (app.isGame) "PLAY >" else "OPEN >", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    onLoadSamples: () -> Unit
) {
    NeoCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowOffset = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MemphisStarburst(
                modifier = Modifier.size(70.dp),
                color = NeoYellow
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StickerBadge(
                text = "NO APPS FOUND!",
                bgColor = NeoMagenta,
                textColor = Color.White,
                rotation = -2f
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your store catalog is currently empty. Load sandbox sample apps or sync with a remote JSON registry URL.",
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace,
                color = NeoSubtext,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            NeoButton(
                onClick = onLoadSamples,
                style = NeoButtonStyle.SECONDARY_YELLOW
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("LOAD SAMPLE GAMES & APPS", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.Black)
            }
        }
    }
}

enum class AppDetailTab(val label: String, val icon: ImageVector) {
    UPDATES("UPDATES", Icons.Default.SystemUpdate),
    DESCRIPTION("DESCRIPTION", Icons.Default.Description),
    SPECS("SPECS", Icons.Default.ListAlt),
    USER_GUIDE("USER GUIDE", Icons.Default.MenuBook)
}

@Composable
fun AppDetailDialog(
    app: AppUiItem,
    initialScreenshotIndex: Int = 0,
    onDismiss: () -> Unit,
    onActionClick: () -> Unit,
    onLaunchClick: () -> Unit,
    onDeleteClick: () -> Unit,
    downloadingPackage: String?,
    downloadProgress: Int
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isCurrentDownloading = downloadingPackage == app.packageName

    val safeScreenshots = remember(app.screenshots) {
        if (app.screenshots.isNotEmpty()) app.screenshots else listOf(app.icon)
    }

    var currentScreenshotIndex by remember(initialScreenshotIndex) {
        mutableIntStateOf(initialScreenshotIndex.coerceIn(0, (safeScreenshots.size - 1).coerceAtLeast(0)))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
        ) {
            // Drop Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(NeoBlack)
            )

            // Dialog Content Window
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.5.dp, NeoBorder)
                    .background(NeoBg)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.5.dp, color = NeoBorder)
                        .background(NeoMutedBg)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = app.name.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            color = NeoText
                        )
                        TagChip(text = if (app.isGame) "GAME" else "APP", bgColor = NeoMagenta, textColor = Color.White)
                    }

                    NeoButton(
                        onClick = onDismiss,
                        style = NeoButtonStyle.PRIMARY_MAGENTA,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("✕", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }

                // Tab Selection State
                var selectedTab by remember { mutableStateOf(AppDetailTab.UPDATES) }
                val tocEntries = remember(app.userGuide) { extractTocEntries(app.userGuide) }
                val headerPositions = remember { mutableStateMapOf<String, Float>() }
                val bodyScrollState = rememberScrollState()
                val coroutineScope = rememberCoroutineScope()

                // Tab Selector Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeoMutedBg)
                        .border(width = 1.dp, color = NeoBorder),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AppDetailTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        val bgColor = if (isSelected) NeoMagenta else Color.Transparent
                        val textColor = if (isSelected) Color.White else NeoSubtext

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(bgColor)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = textColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = tab.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = textColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Scrollable Body Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(bodyScrollState)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Main Screenshot Preview — swipe pages, pinch-zoom / pan active image
                    SwipeableZoomableScreenshotPreview(
                        screenshots = safeScreenshots,
                        currentIndex = currentScreenshotIndex,
                        onIndexChange = { currentScreenshotIndex = it },
                        maxHeight = 360.dp,
                        fallbackAspectRatio = if (app.isGame) 16f / 9f else 9f / 16f
                    )

                    // Thumbnail Selector Strip
                    if (safeScreenshots.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(safeScreenshots.size) { idx ->
                                val isSelected = idx == currentScreenshotIndex
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .border(if (isSelected) 3.dp else 1.5.dp, if (isSelected) NeoMagenta else NeoBorder)
                                        .background(NeoSurface)
                                        .clickable { currentScreenshotIndex = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = safeScreenshots[idx],
                                        contentDescription = "Thumb $idx",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }

                    // Tab Body Contents
                    when (selectedTab) {
                        AppDetailTab.UPDATES -> {
                            NeoCard(shadowOffset = 3.dp) {
                                Text(
                                    text = "RELEASE UPDATES & HISTORY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeoCyan
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val updatesContent = app.updatesHistory.ifBlank { app.changelog }
                                MarkdownBody(
                                    markdown = updatesContent,
                                    bodySize = 11.sp,
                                    lineHeight = 15.sp,
                                    headingColor = NeoCyan,
                                    accentColor = NeoYellow
                                )
                            }
                        }

                        AppDetailTab.DESCRIPTION -> {
                            NeoCard(shadowOffset = 3.dp) {
                                Text(
                                    text = "APPLICATION OVERVIEW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeoMagenta
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                MarkdownBody(
                                    markdown = app.description,
                                    bodySize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    headingColor = NeoMagenta,
                                    accentColor = NeoCyan
                                )
                            }
                        }

                        AppDetailTab.SPECS -> {
                            if (app.specs.isNotBlank()) {
                                NeoCard(shadowOffset = 3.dp) {
                                    Text(
                                        text = "DETAILED SPECIFICATIONS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeoYellow
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    MarkdownBody(
                                        markdown = app.specs,
                                        bodySize = 11.sp,
                                        lineHeight = 15.sp,
                                        headingColor = NeoYellow,
                                        accentColor = NeoCyan
                                    )
                                }
                            }

                            NeoCard(shadowOffset = 3.dp) {
                                Text(
                                    text = "SYSTEM & PACKAGE PROPERTIES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeoText
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                DetailRow("APPLICATION NAME", app.name)
                                DetailRow("PACKAGE ID", app.packageName)
                                DetailRow("CATEGORY", if (app.isGame) "Game" else "Application")
                                DetailRow("LATEST VERSION", "${app.latestVersionName} (code ${app.latestVersionCode})")
                                DetailRow("INSTALLED VERSION", app.installedVersionName?.let { "$it (code ${app.installedVersionCode})" } ?: "Not Installed")
                                DetailRow("UPDATE STATUS", app.statusText)
                                DetailRow("APK DOWNLOAD URL", app.apkUrl)
                            }
                        }

                        AppDetailTab.USER_GUIDE -> {
                            val userGuideText = app.userGuide.ifBlank {
                                "# User Guide for ${app.name}\n\nComprehensive user documentation is available for ${app.name}.\n\n## Overview\nRefer to the description tab for key application capabilities.\n\n## Quick Start\nInstall or update the application using the button below."
                            }

                            // Interactive Table of Contents Header Chips
                            if (tocEntries.isNotEmpty()) {
                                NeoCard(shadowOffset = 3.dp) {
                                    Text(
                                        text = "TABLE OF CONTENTS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = NeoYellow
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(tocEntries.size) { idx ->
                                            val entry = tocEntries[idx]
                                            Box(
                                                modifier = Modifier
                                                    .border(1.5.dp, NeoCyan)
                                                    .background(NeoSurface)
                                                    .clickable {
                                                        val targetY = headerPositions[entry.title]
                                                            ?: headerPositions[entry.targetAnchor]
                                                            ?: 0f
                                                        coroutineScope.launch {
                                                            bodyScrollState.animateScrollTo(targetY.toInt().coerceAtLeast(0))
                                                        }
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = entry.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = NeoCyan
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            NeoCard(shadowOffset = 3.dp) {
                                Text(
                                    text = "${app.name.uppercase()} USER GUIDE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = NeoYellow
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                MarkdownBody(
                                    markdown = userGuideText,
                                    bodySize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    headingColor = NeoYellow,
                                    accentColor = NeoCyan,
                                    onHeaderPositioned = { title, anchor, yPx ->
                                        headerPositions[title] = yPx
                                        headerPositions[anchor] = yPx
                                    }
                                )
                            }
                        }
                    }
                }

                // Action Footer Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.5.dp, color = NeoBorder)
                        .background(NeoMutedBg)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoButton(
                        onClick = onDeleteClick,
                        style = NeoButtonStyle.NEUTRAL_WHITE,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeoRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REMOVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeoRed, fontFamily = FontFamily.Monospace)
                    }

                    if (isCurrentDownloading) {
                        Text(
                            text = "DOWNLOADING... $downloadProgress%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = NeoMagenta
                        )
                    } else {
                        when {
                            !app.isInstalled -> {
                                NeoButton(
                                    onClick = onActionClick,
                                    style = NeoButtonStyle.PRIMARY_MAGENTA
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("INSTALL NOW", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                            app.isOutdated -> {
                                NeoButton(
                                    onClick = onActionClick,
                                    style = NeoButtonStyle.SECONDARY_YELLOW
                                ) {
                                    Icon(Icons.Default.Update, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("UPDATE APP", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.Black)
                                }
                            }
                            else -> {
                                NeoButton(
                                    onClick = onLaunchClick,
                                    style = NeoButtonStyle.ACTION_GREEN
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LAUNCH NOW", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = NeoSubtext)
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = NeoText, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun AddAppDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        packageName: String,
        versionName: String,
        versionCode: Int,
        apkUrl: String,
        iconKeyword: String,
        changelog: String,
        isGame: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var versionName by remember { mutableStateOf("1.0.0") }
    var versionCodeText by remember { mutableStateOf("1") }
    var apkUrl by remember { mutableStateOf("") }
    var iconKeyword by remember { mutableStateOf("apps") }
    var changelog by remember { mutableStateOf("") }
    var isGame by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ADD CUSTOM APPLICATION",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    color = NeoText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoButton(
                        onClick = { isGame = false },
                        style = if (!isGame) NeoButtonStyle.ACCENT_CYAN else NeoButtonStyle.NEUTRAL_WHITE,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("APPLICATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    NeoButton(
                        onClick = { isGame = true },
                        style = if (isGame) NeoButtonStyle.PRIMARY_MAGENTA else NeoButtonStyle.NEUTRAL_WHITE,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("GAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = NeoText,
                    unfocusedTextColor = NeoText,
                    focusedBorderColor = NeoMagenta,
                    unfocusedBorderColor = NeoBorder,
                    focusedLabelColor = NeoMagenta,
                    unfocusedLabelColor = NeoSubtext
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("App Name", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    shape = RectangleShape,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package ID (e.g. com.example.app)", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    shape = RectangleShape,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = versionName,
                        onValueChange = { versionName = it },
                        label = { Text("Version", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        singleLine = true,
                        shape = RectangleShape,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = versionCodeText,
                        onValueChange = { versionCodeText = it },
                        label = { Text("Code", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        singleLine = true,
                        shape = RectangleShape,
                        colors = textFieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = apkUrl,
                    onValueChange = { apkUrl = it },
                    label = { Text("APK Download URL", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    shape = RectangleShape,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasError) {
                    Text(
                        text = "Please fill in Name, Package ID, and APK URL.",
                        color = NeoRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = NeoSubtext)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    NeoButton(
                        onClick = {
                            if (name.isBlank() || packageName.isBlank() || apkUrl.isBlank()) {
                                hasError = true
                            } else {
                                val code = versionCodeText.toIntOrNull() ?: 1
                                onConfirm(name, packageName, versionName, code, apkUrl, iconKeyword, changelog, isGame)
                            }
                        },
                        style = NeoButtonStyle.SECONDARY_YELLOW
                    ) {
                        Text("ADD ITEM", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    currentUrl: String,
    currentGithubToken: String = "",
    notificationsEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (url: String, notify: Boolean, token: String) -> Unit,
    onLoadSamples: () -> Unit,
    onAddAppClick: () -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    var token by remember { mutableStateOf(currentGithubToken) }
    var notify by remember { mutableStateOf(notificationsEnabled) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = NeoText,
        unfocusedTextColor = NeoText,
        focusedBorderColor = NeoMagenta,
        unfocusedBorderColor = NeoBorder,
        focusedLabelColor = NeoMagenta,
        unfocusedLabelColor = NeoSubtext
    )

    Dialog(onDismissRequest = onDismiss) {
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            shadowOffset = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "STORE SETTINGS",
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    color = NeoText
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Remote Registry JSON URL", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    shape = RectangleShape,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("GitHub Token (PAT for Private Repos)", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    placeholder = { Text("ghp_... or github_pat_...", fontSize = 10.sp, color = NeoSubtext) },
                    singleLine = true,
                    shape = RectangleShape,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Background Updates", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = NeoText)
                        Text("Periodically check for new APK versions", fontSize = 10.sp, color = NeoSubtext)
                    }
                    Switch(
                        checked = notify,
                        onCheckedChange = { notify = it }
                    )
                }

                NeoButton(
                    onClick = onAddAppClick,
                    style = NeoButtonStyle.ACCENT_CYAN,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD CUSTOM APPLICATION", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.Black)
                }

                NeoButton(
                    onClick = onLoadSamples,
                    style = NeoButtonStyle.SECONDARY_YELLOW,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET & SEED SAMPLE APPS/GAMES", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", fontFamily = FontFamily.Monospace, color = NeoSubtext)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    NeoButton(
                        onClick = { onSave(url, notify, token) },
                        style = NeoButtonStyle.PRIMARY_MAGENTA
                    ) {
                        Text("SAVE SETTINGS", fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun getMaterialIcon(keyword: String): ImageVector {
    return when (keyword.lowercase().trim()) {
        "sports_esports", "game", "gamepad", "controller", "videogame_asset" -> Icons.Default.SportsEsports
        "extension", "puzzle" -> Icons.Default.Extension
        "calculate", "calculator", "calc" -> Icons.Default.Calculate
        "playlist_add_check", "todo", "tasks", "list" -> Icons.Default.PlaylistAddCheck
        "play_circle", "video", "media", "player" -> Icons.Default.PlayCircle
        "apps", "widgets", "grid" -> Icons.Default.Apps
        "settings", "gear" -> Icons.Default.Settings
        "cloud", "sync", "download" -> Icons.Default.CloudDownload
        "info", "about" -> Icons.Default.Info
        "person", "avatar" -> Icons.Default.Person
        "star", "rate" -> Icons.Default.Star
        "map", "navigation", "gps" -> Icons.Default.Map
        "home" -> Icons.Default.Home
        "photo", "camera", "image" -> Icons.Default.PhotoCamera
        "chat", "message" -> Icons.Default.Chat
        "music", "audio", "sound" -> Icons.Default.MusicNote
        "book", "read" -> Icons.Default.Book
        else -> Icons.Default.Android
    }
}

@Composable
private fun AppManagerUpdateBanner(
    app: AppUiItem,
    onUpdateClick: () -> Unit,
    onOpenDetail: () -> Unit,
    downloadingPackage: String?,
    downloadProgress: Int,
    modifier: Modifier = Modifier
) {
    val isDownloading = downloadingPackage == app.packageName
    val versionDiff = if (!app.installedVersionName.isNullOrEmpty() && app.installedVersionName != app.latestVersionName) {
        "v${app.installedVersionName} → v${app.latestVersionName}"
    } else {
        "v${app.latestVersionName}"
    }

    NeoCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_manager_update_banner"),
        backgroundColor = Color(0xFF2B1B47),
        borderColor = NeoYellow,
        borderWidth = 2.dp,
        shadowOffset = 4.dp,
        shadowColor = NeoBlack,
        onClick = onOpenDetail
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(NeoYellow)
                        .border(1.5.dp, NeoBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Update Available",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "APP MANAGER UPDATE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = NeoYellow
                        )
                        Box(
                            modifier = Modifier
                                .background(NeoMagenta)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = versionDiff,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = "New version of RykerSoft is available! Click to update now.",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            NeoButton(
                onClick = onUpdateClick,
                style = NeoButtonStyle.SECONDARY_YELLOW,
                enabled = !isDownloading,
                modifier = Modifier.testTag("app_manager_update_now_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isDownloading) "$downloadProgress%" else "UPDATE NOW",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}


data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
