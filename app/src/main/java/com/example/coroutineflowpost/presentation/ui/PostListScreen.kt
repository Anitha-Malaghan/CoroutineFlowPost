import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.coroutineflowpost.ui.theme.Amber
import com.example.coroutineflowpost.ui.theme.AmberDim
import com.example.coroutineflowpost.ui.theme.Blue
import com.example.coroutineflowpost.ui.theme.BlueDim
import com.example.coroutineflowpost.ui.theme.BorderSubtle
import com.example.coroutineflowpost.ui.theme.DarkBg
import com.example.coroutineflowpost.ui.theme.DarkCard
import com.example.coroutineflowpost.ui.theme.Emerald
import com.example.coroutineflowpost.ui.theme.EmeraldDim
import com.example.coroutineflowpost.ui.theme.TextDim
import com.example.coroutineflowpost.ui.theme.TextMuted
import com.example.coroutineflowpost.ui.theme.TextPrimary
import com.example.coroutineflowpost.ui.theme.TextSecondary
import com.example.coroutineflowpost.ui.theme.Violet
import com.example.coroutineflowpost.ui.theme.VioletDim


// Cycling accent colors per card index
private val accentColors = listOf(
    Pair(Violet,  VioletDim),
    Pair(Blue,    BlueDim),
    Pair(Emerald, EmeraldDim),
    Pair(Amber,   AmberDim)
)

private val tagLabels = listOf("Featured", "Trending", "New", "Popular")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(viewModel: PostViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = { FlowPostTopBar(onRefresh = viewModel::refresh) },
        floatingActionButton = {
            FloatingActionButton(
                onClick            = viewModel::refresh,
                containerColor     = Violet,
                contentColor       = Color.White,
                shape              = RoundedCornerShape(16.dp),
                modifier           = Modifier.size(52.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is PostUiState.Loading -> LoadingContent()
                is PostUiState.Empty   -> EmptyContent(onRetry = viewModel::refresh)
                is PostUiState.Error   -> ErrorContent(state.message, onRetry = viewModel::refresh)
                is PostUiState.Success -> PostFeed(posts = state.posts)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlowPostTopBar(onRefresh: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(DarkBg)
    ) {
        // Top bar row
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = "FlowPost",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight   = FontWeight.Bold,
                    brush        = Brush.linearGradient(
                        colors = listOf(Violet, Blue, Emerald),
                        start  = Offset(0f, 0f),
                        end    = Offset(300f, 0f)
                    ),
                    letterSpacing = (-0.5).sp
                )
            )
            Surface(
                shape         = RoundedCornerShape(20.dp),
                color         = VioletDim,
                tonalElevation = 0.dp
            ) {
                Text(
                    text     = "LIVE",
                    color    = Violet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    letterSpacing = 1.sp
                )
            }
        }

        // Hero section
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(
                text      = "TODAY'S FEED",
                fontSize  = 11.sp,
                fontWeight = FontWeight.Medium,
                color     = TextDim,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "What's flowing today",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight    = FontWeight.Bold,
                    color         = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                text      = "Synced from network · stored locally",
                fontSize  = 13.sp,
                color     = TextMuted,
                fontWeight = FontWeight.Light,
                modifier  = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
        }

        // Filter chips
        LazyRow(
            contentPadding      = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier            = Modifier.padding(bottom = 12.dp)
        ) {
            val chips = listOf("All", "Tech", "Science", "Design", "Business")
            itemsIndexed(chips) { index, chip ->
                FilterChip(
                    selected = index == 0,
                    onClick  = {},
                    label    = {
                        Text(chip, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor    = VioletDim,
                        selectedLabelColor        = Violet,
                        containerColor            = Color.Transparent,
                        labelColor                = TextMuted
                    ),
                    border   = FilterChipDefaults.filterChipBorder(
                        enabled          = true,
                        selected         = index == 0,
                        borderColor      = BorderSubtle,
                        selectedBorderColor = Violet.copy(alpha = 0.4f),
                        borderWidth      = 1.dp,
                        selectedBorderWidth = 1.dp
                    ),
                    shape    = RoundedCornerShape(20.dp)
                )
            }
        }

        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
    }
}

@Composable
private fun PostFeed(posts: List<Post>) {
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(posts) { post ->
            val colorIndex  = (post.id - 1) % accentColors.size
            val accent      = accentColors[colorIndex]
            val tag         = tagLabels[colorIndex]
            PostCard(post = post, accentColor = accent.first, accentBg = accent.second, tag = tag)
        }
    }
}

@Composable
private fun PostCard(
    post:        Post,
    accentColor: Color,
    accentBg:    Color,
    tag:         String
) {
    Surface(
        shape         = RoundedCornerShape(16.dp),
        color         = DarkCard,
        tonalElevation = 0.dp,
        modifier      = Modifier.fillMaxWidth()
    ) {
        // Subtle border
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    color = BorderSubtle,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(DarkCard, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Colored left accent stripe
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(accentColor, accentColor.copy(alpha = 0.4f))
                            )
                        )
                        .defaultMinSize(minHeight = 60.dp)
                )

                Column(Modifier.weight(1f)) {
                    // Meta row: avatar + user + id
                    Row(
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier             = Modifier.padding(bottom = 8.dp)
                    ) {
                        // Avatar circle
                        Box(
                            Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(accentBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = "U${post.userId}",
                                color      = accentColor,
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text       = "User ${post.userId}",
                            fontSize   = 12.sp,
                            color      = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Box(
                            Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(TextDim)
                        )

                        Text(
                            text     = "#${post.id.toString().padStart(3, '0')}",
                            fontSize = 11.sp,
                            color    = TextDim,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }

                    // Title
                    Text(
                        text       = post.title.replaceFirstChar { it.uppercase() },
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        letterSpacing = (-0.2).sp,
                        modifier   = Modifier.padding(bottom = 6.dp)
                    )

                    // Body
                    Text(
                        text       = post.body,
                        fontSize   = 13.sp,
                        color      = TextMuted,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Light
                    )

                    // Footer row
                    Row(
                        modifier             = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Tag pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentBg
                        ) {
                            Text(
                                text     = tag.uppercase(),
                                color    = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text     = "2 min read →",
                            fontSize = 11.sp,
                            color    = TextDim
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Violet, strokeWidth = 2.dp)
            Text("Loading posts...", color = TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
fun EmptyContent(onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Text("No posts yet", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Pull fresh data from the network", color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            Button(
                onClick      = onRetry,
                colors       = ButtonDefaults.buttonColors(containerColor = Violet),
                shape        = RoundedCornerShape(12.dp),
                modifier     = Modifier.padding(top = 4.dp)
            ) {
                Text("Load Posts", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Text("Something went wrong", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(message, color = TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
            Button(
                onClick  = onRetry,
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Text("Retry", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}