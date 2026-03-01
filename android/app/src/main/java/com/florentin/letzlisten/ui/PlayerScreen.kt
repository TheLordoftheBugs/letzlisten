package com.florentin.letzlisten.ui

import androidx.compose.foundation.background
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
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 80.dp, bottom = 120.dp)
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

        // Bottom control bar: Share | Play/Pause | Favorite
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.25f))
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            // Share
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = onShare, enabled = !currentTrack.isUnknown) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        tint = if (!currentTrack.isUnknown) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Play/Pause
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(if (isPlaying) AccentRed else AccentBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    IconButton(onClick = onTogglePlayback) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Lecture",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Favorite track
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                IconButton(onClick = onToggleFavorite, enabled = !currentTrack.isUnknown) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorited) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = when {
                            isFavorited -> AccentRed
                            !currentTrack.isUnknown -> TextPrimary
                            else -> TextSecondary.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(26.dp)
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
