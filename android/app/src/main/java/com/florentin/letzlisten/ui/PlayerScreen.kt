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
    hasStartedPlaying: Boolean,
    isLoading: Boolean,
    isFavorited: Boolean,
    languageFlag: String,
    albumArtUrl: String? = null,
    artworkSize: Int = 220,
    onTogglePlayback: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenStationPicker: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
        )
    ) {
        // Center content — bottom padding accounts for bar (64dp + 16dp×2)
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
            StationArtwork(
                station = currentStation,
                albumArtUrl = albumArtUrl,
                hasStartedPlaying = hasStartedPlaying,
                isTrackKnown = !currentTrack.isUnknown,
                size = artworkSize
            )

            Spacer(Modifier.height(28.dp))

            // Station name
            Text(
                text = currentStation?.name ?: "",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Track info: shown only when real ICY metadata is available.
            // If the station never broadcasts artist/title, nothing is displayed.
            if (hasStartedPlaying && !currentTrack.isUnknown) {
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
                Spacer(Modifier.height(8.dp))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorited) AccentRed else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Top LEFT: Favorites (heart in circle, like iOS heart.circle)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onOpenFavorites)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Mes favoris",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Top CENTER: Language picker (flag in capsule, like iOS)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onOpenLanguagePicker)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = languageFlag, fontSize = 20.sp)
            }
        }

        // Top RIGHT: Station selector (antenna like iOS)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 20.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onOpenStationPicker)
        ) {
            Icon(
                imageVector = Icons.Default.Radio,
                contentDescription = "Changer de station",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Bottom control bar — [Share] [Play] like iOS (no AirPlay on Android)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.12f))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                val canShare = isPlaying && hasStartedPlaying && !currentTrack.isUnknown

                // Share (64×64, rounded rect — like iOS square.and.arrow.up button)
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

                // Play/Stop (64×64 circle, red/blue — like iOS)
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
            }
        }
    }
}

/**
 * Shows album art only when the radio is playing AND ICY metadata is present AND iTunes
 * returned an artwork URL — exactly like iOS.  Otherwise the station logo is displayed,
 * using the same priority as iOS FaviconFetcher:
 *   1. Bundled drawable (logoImageName mapped to a local asset)
 *   2. Facebook  → Graph API profile picture
 *   3. Others    → apple-touch-icon.png → apple-touch-icon-precomposed.png
 *                  → favicon.ico → Google favicon (256 px)
 */
@Composable
private fun StationArtwork(
    station: RadioStation?,
    albumArtUrl: String?,
    hasStartedPlaying: Boolean,
    isTrackKnown: Boolean,
    size: Int
) {
    val bundledRes = remember(station?.logoImageName) { bundledLogoRes(station?.logoImageName) }
    val logoUrls = remember(station?.id) { station?.let { stationLogoUrls(it) } ?: emptyList() }
    // Reset album-art failure each time a new URL arrives
    var albumArtFailed by remember(albumArtUrl) { mutableStateOf(false) }
    // Reset logo index each time the station changes
    var logoIndex by remember(station?.id) { mutableIntStateOf(0) }

    // Album art is only shown while playing with confirmed ICY + iTunes data
    val showAlbumArt = hasStartedPlaying && isTrackKnown && albumArtUrl != null && !albumArtFailed

    val currentLogoUrl = logoUrls.getOrNull(logoIndex)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E1E3F))
    ) {
        when {
            station == null -> CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size((size / 4).dp),
                strokeWidth = 3.dp
            )
            showAlbumArt -> AsyncImage(
                model = albumArtUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = { albumArtFailed = true },
                modifier = Modifier.fillMaxSize()
            )
            bundledRes != null -> AsyncImage(
                model = bundledRes,
                contentDescription = station?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            currentLogoUrl != null -> AsyncImage(
                model = currentLogoUrl,
                contentDescription = station?.name,
                contentScale = ContentScale.Crop,
                // Try the next URL in the cascade on error
                onError = { logoIndex++ },
                modifier = Modifier.fillMaxSize()
            )
            else -> Text(
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
