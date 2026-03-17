package com.example.coroutineflowpost.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coroutineflowpost.domain.model.Post
import com.example.coroutineflowpost.presentation.viewmodel.PostUiState
import com.example.coroutineflowpost.presentation.viewmodel.PostViewModel
import com.example.coroutineflowpost.presentation.viewmodel.UiEvent
import com.example.coroutineflowpost.ui.theme.BorderSubtle
import com.example.coroutineflowpost.ui.theme.CardBg
import com.example.coroutineflowpost.ui.theme.CoralRed
import com.example.coroutineflowpost.ui.theme.CoralRedDim
import com.example.coroutineflowpost.ui.theme.DeepBg
import com.example.coroutineflowpost.ui.theme.HeroBgEnd
import com.example.coroutineflowpost.ui.theme.HeroBgStart
import com.example.coroutineflowpost.ui.theme.Lavender
import com.example.coroutineflowpost.ui.theme.LavenderDim
import com.example.coroutineflowpost.ui.theme.Mint
import com.example.coroutineflowpost.ui.theme.MintDim
import com.example.coroutineflowpost.ui.theme.SkyBlue
import com.example.coroutineflowpost.ui.theme.SkyBlueDim
import com.example.coroutineflowpost.ui.theme.Tangerine
import com.example.coroutineflowpost.ui.theme.TangerineDim
import com.example.coroutineflowpost.ui.theme.TextDim
import com.example.coroutineflowpost.ui.theme.TextMuted
import com.example.coroutineflowpost.ui.theme.TextPrimary
import com.example.coroutineflowpost.ui.theme.TextSecondary

// Four colour slots cycling by post.id
private data class AccentSlot(
    val solid: Color,
    val dim:   Color,
    val tag:   String
)

private val accents = listOf(
    AccentSlot(CoralRed,  CoralRedDim,  "Featured"),
    AccentSlot(SkyBlue,   SkyBlueDim,   "Trending"),
    AccentSlot(Mint,      MintDim,      "New"),
    AccentSlot(Tangerine, TangerineDim, "Popular")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(viewModel: PostViewModel) {
    val uiState          by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing     by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedChip      by viewModel.selectedChipIndex.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = DeepBg,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = viewModel::refresh,
                containerColor = Lavender,
                contentColor   = DeepBg,
                shape          = RoundedCornerShape(16.dp),
                modifier       = Modifier.size(52.dp),
                elevation      = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is PostUiState.Loading -> LoadingContent()
            is PostUiState.Empty   -> EmptyContent(onRetry = viewModel::refresh)
            is PostUiState.Error   -> ErrorContent(
                message  = state.message,
                isOnline = state.isOnline,
                onRetry  = viewModel::refresh
            )
            is PostUiState.Success -> PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh    = viewModel::refresh,
                modifier     = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                PostFeed(
                    posts          = state.posts,
                    isOnline       = state.isOnline,
                    selectedChip   = selectedChip,
                    onChipSelected = viewModel::onChipSelected
                )
            }
        }
    }
}

// ── Feed ──────────────────────────────────────────────────────────────────────

@Composable
private fun PostFeed(
    posts:            List<Post>,
    isOnline:         Boolean,
    selectedChip:     Int,
    onChipSelected:   (Int) -> Unit
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { FeedTopBar() }

        item {
            HeroBanner(
                postCount = posts.size,
                modifier  = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Offline banner
        if (!isOnline) {
            item {
                OfflineBanner()
            }
        }

        item { FilterChipsRow(
            selectedChip   = selectedChip,      // passed down
            onChipSelected = onChipSelected,    // passed down
            modifier = Modifier.padding(bottom = 8.dp
            ))
        }


        item {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "LATEST POSTS",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color         = TextDim
                )
                Text("${posts.size} results", fontSize = 11.sp, color = TextDim)
            }
        }

        items(posts) { post ->
            val slot = accents[(post.id - 1) % accents.size]
            PostCard(
                post        = post,
                accentColor = slot.solid,
                accentBg    = slot.dim,
                tag         = slot.tag,
                modifier    = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun FeedTopBar() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = "FlowPost",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight    = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                brush         = Brush.linearGradient(
                    colors = listOf(CoralRed, Tangerine, Mint, SkyBlue, Lavender),
                    start  = Offset(0f, 0f),
                    end    = Offset(400f, 0f)
                )
            )
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MintDim
        ) {
            Row(
                modifier              = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Mint)
                )
                Text(
                    "LIVE",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Mint,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ── Hero banner ───────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(postCount: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(HeroBgStart, Color(0xFF0D1A4A), HeroBgEnd),
                    start  = Offset(0f, 0f),
                    end    = Offset(600f, 400f)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Text(
                "✦  DAILY DIGEST",
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                color         = Lavender,
                modifier      = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text  = "Your personalised\nfeed is ready",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    lineHeight    = 28.sp
                ),
                color    = TextPrimary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                "Fetched from network · cached locally",
                fontSize   = 12.sp,
                color      = TextDim,
                fontWeight = FontWeight.Normal
            )
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatItem(value = postCount.toString(), label = "POSTS",  valueColor = CoralRed)
                StatItem(value = "10",                 label = "USERS",  valueColor = Tangerine)
                StatItem(value = "Now",                label = "SYNCED", valueColor = Mint)
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, valueColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            value,
            fontSize      = 20.sp,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            color         = valueColor
        )
        Text(
            label,
            fontSize      = 9.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color         = TextDim
        )
    }
}

// ── Offline banner ────────────────────────────────────────────────────────────

@Composable
private fun OfflineBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CoralRedDim)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(CoralRed)
        )
        Text(
            "You are offline · showing cached data",
            fontSize   = 12.sp,
            color      = CoralRed,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Filter chips ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    selectedChip:   Int,                      // hoisted from ViewModel
    onChipSelected: (Int) -> Unit,            // hoisted from ViewModel
    modifier:       Modifier = Modifier
) {
    val chips    = listOf("All", "Tech", "Science", "Design", "Business")

    var selected by remember { mutableIntStateOf(0) }

    LazyRow(
        modifier              = modifier,
        contentPadding        = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(chips) { index, chip ->
            FilterChip(

                selected = index == selectedChip,       //  from ViewModel
                onClick  = { onChipSelected(index) },   //  calls ViewModel
                label    = { Text(chip, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LavenderDim,
                    selectedLabelColor     = Lavender,
                    containerColor         = Color.Transparent,
                    labelColor             = TextMuted
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled             = true,
                    selected            = index == selected,
                    borderColor         = BorderSubtle,
                    selectedBorderColor = Lavender.copy(alpha = 0.4f),
                    borderWidth         = 1.dp,
                    selectedBorderWidth = 1.dp
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

// ── Post card ─────────────────────────────────────────────────────────────────

@Composable
private fun PostCard(
    post:        Post,
    accentColor: Color,
    accentBg:    Color,
    tag:         String,
    modifier:    Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.27f),
                        accentColor.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(CardBg)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Glowing left bar
            Box(
                Modifier
                    .width(3.dp)
                    .defaultMinSize(minHeight = 56.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.25f))
                        )
                    )
            )

            Column(Modifier.weight(1f)) {
                // Meta row
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier              = Modifier.padding(bottom = 9.dp)
                ) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(accentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "U${post.userId}",
                            fontSize   = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = accentColor
                        )
                    }
                    Text(
                        "User ${post.userId}",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextSecondary
                    )
                    Box(
                        Modifier
                            .size(2.dp)
                            .clip(CircleShape)
                            .background(TextDim)
                    )
                    Text(
                        "#${post.id.toString().padStart(3, '0')}",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDim,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.End
                    )
                }

                // Title
                Text(
                    text          = post.title.replaceFirstChar { it.uppercase() },
                    fontSize      = 14.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color         = TextPrimary,
                    maxLines      = 2,
                    overflow      = TextOverflow.Ellipsis,
                    lineHeight    = 19.sp,
                    modifier      = Modifier.padding(bottom = 5.dp)
                )

                // Body
                Text(
                    text       = post.body,
                    fontSize   = 12.sp,
                    color      = TextMuted,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Normal
                )

                // Footer
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentBg
                    ) {
                        Text(
                            tag.uppercase(),
                            color         = accentColor,
                            fontSize      = 9.sp,
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier      = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text("2 min · →", fontSize = 11.sp, color = TextDim)
                }
            }
        }
    }
}

// ── State screens ─────────────────────────────────────────────────────────────

@Composable
fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                color       = Lavender,
                strokeWidth = 2.dp,
                modifier    = Modifier.size(36.dp)
            )
            Text("Loading posts…", color = TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
fun EmptyContent(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Text(
                "Nothing here yet",
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tap below to pull fresh data",
                color     = TextMuted,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = Lavender),
                shape   = RoundedCornerShape(12.dp)
            ) {
                Text("Load Posts", fontWeight = FontWeight.Bold, color = DeepBg)
            }
        }
    }
}

@Composable
fun ErrorContent(message: String, isOnline: Boolean = true, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Text(
                if (!isOnline) "You are offline" else "Something went wrong",
                color      = TextPrimary,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                if (!isOnline) "Check your connection and try again" else message,
                color     = TextMuted,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (!isOnline) CoralRed else Lavender
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Retry", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}