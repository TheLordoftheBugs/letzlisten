package com.florentin.letzlisten.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.florentin.letzlisten.data.RadioStation
import com.florentin.letzlisten.player.TrackInfo
import com.florentin.letzlisten.ui.theme.AccentBlue
import com.florentin.letzlisten.ui.theme.AccentRed
import com.florentin.letzlisten.ui.theme.BackgroundBottom
import com.florentin.letzlisten.ui.theme.BackgroundTop
import com.florentin.letzlisten.ui.theme.SurfaceDark
import com.florentin.letzlisten.ui.theme.TextPrimary
import com.florentin.letzlisten.ui.theme.TextSecondary

@Composable
fun PlayerScreen(
    currentStation: RadioStation?,
    currentTrack: TrackInfo,
    isPlaying: Boolean,
    isLoading: Boolean,
    isFavorited: Boolean,
    artworkSize: Int = 220,
    onTogglePlayback: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenStationPicker: () -> Unit,
    onOpenFavorites: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
        )
    ) {
        // Center content — bottom padding accounts for bar (64dp + 16dp×2) + nav bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 96.dp)
        ) {
            StationArtwork(station = currentStation, size = artworkSize)

            Spacer(Modifier.height(28.dp))

            Text(
                text = currentStation?.name ?: "",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!currentTrack.isUnknown) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = currentTrack.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentTrack.artist,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Top overlay: favorites (left) + station picker (right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onOpenFavorites) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Mes favoris",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenStationPicker) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = "Changer de radio",
                    tint = TextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Bottom control bar — matches iOS BottomControlBar exactly
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                val canShare = !currentTrack.isUnknown
                val canFavorite = !currentTrack.isUnknown

                // Share (64×64, rounded rect background like iOS)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = if (canShare) 0.15f else 0.05f))
                        .clickable(enabled = canShare, onClick = onShare)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        tint = Color.White.copy(alpha = if (canShare) 0.9f else 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                // Play/Stop (64×64 circle, red/blue — same size as iOS)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isPlaying) AccentRed else AccentBlue)
                        .then(if (!isLoading) Modifier.clickable(onClick = onTogglePlayback) else Modifier)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Lecture",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Favorite (64×64, rounded rect background — red tint when favorited)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isFavorited -> AccentRed.copy(alpha = 0.25f)
                                canFavorite -> Color.White.copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.05f)
                            }
                        )
                        .clickable(enabled = canFavorite, onClick = onToggleFavorite)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorited) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = when {
                            isFavorited -> AccentRed
                            canFavorite -> Color.White.copy(alpha = 0.9f)
                            else -> Color.White.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StationArtwork(station: RadioStation?, size: Int) {
    var showPlaceholder by remember(station?.id) { mutableStateOf(false) }
    val url = station?.websiteUrl?.let { "$it/favicon.ico" }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E1E3F))
    ) {
        if (url != null && !showPlaceholder) {
            AsyncImage(
                model = url,
                contentDescription = station?.name,
                contentScale = ContentScale.Crop,
                onError = { showPlaceholder = true },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = station?.name
                    ?.split(" ", "-")
                    ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    ?.take(2)
                    ?.joinToString("") ?: "LL",
                fontSize = (size / 4).sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary.copy(alpha = 0.7f)
            )
        }
    }
}
