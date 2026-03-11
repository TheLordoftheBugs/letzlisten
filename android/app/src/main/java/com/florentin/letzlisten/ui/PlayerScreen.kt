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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.platform.LocalUriHandler
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
    hasStartedPlaying: Boolean,
    isLoading: Boolean,
    isFavorited: Boolean,
    albumArtUrl: String? = null,
    artworkSize: Int = 220,
    onTogglePlayback: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenStationPicker: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val websiteUrl = currentStation?.websiteUrl

    BoxWithConstraints(
        modifier = modifier.background(
            Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
        )
    ) {
        // Compact = téléphone en mode paysage (hauteur insuffisante pour le layout portrait)
        val isCompact = maxHeight < 500.dp
        val canShare = isPlaying && hasStartedPlaying && !currentTrack.isUnknown
        // Tailles calquées sur iOS : btnSize = cercles haut + côtés bas, playSize = bouton Play
        val btnSize = if (isCompact) 53.dp else 60.dp
        val playSize = if (isCompact) 62.dp else 77.dp
        val iconSize = if (isCompact) 24.dp else 26.dp
        val playIconSize = if (isCompact) 26.dp else 34.dp
        val barVerticalPadding = if (isCompact) 10.dp else 16.dp
        // Hauteur estimée de la barre du bas : bouton + padding × 2 + divider
        val bottomBarClearance = playSize + barVerticalPadding * 2 + 2.dp

        if (isCompact) {
            // Layout paysage : artwork à gauche, infos à droite
            val artworkSizeCompact = (maxHeight.value * 0.42f).toInt().coerceIn(80, 150)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 52.dp)        // dégager les boutons du haut
                    .navigationBarsPadding()
                    .padding(bottom = bottomBarClearance)
                    .padding(horizontal = 24.dp)
            ) {
                StationArtwork(
                    station = currentStation,
                    albumArtUrl = albumArtUrl,
                    hasStartedPlaying = hasStartedPlaying,
                    isTrackKnown = !currentTrack.isUnknown,
                    size = artworkSizeCompact,
                    onClick = websiteUrl?.let { url -> { uriHandler.openUri(url) } }
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStation?.name ?: "",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (websiteUrl != null)
                            Modifier.clickable { uriHandler.openUri(websiteUrl) }
                        else Modifier
                    )
                    if (hasStartedPlaying && !currentTrack.isUnknown) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = currentTrack.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentTrack.artist,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isFavorited) AccentRed else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Layout portrait — bottom padding = hauteur de la barre de contrôle
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
                    size = artworkSize,
                    onClick = websiteUrl?.let { url -> { uriHandler.openUri(url) } }
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
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (websiteUrl != null)
                        Modifier.clickable { uriHandler.openUri(websiteUrl) }
                    else Modifier
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
        }

        // Top LEFT: Favorites (heart in circle, like iOS heart.circle)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp)
                .size(btnSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onOpenFavorites)
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Mes favoris",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(iconSize)
            )
        }

        // Top CENTER: Station selector (antenna, like iOS)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
                .size(btnSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onOpenStationPicker)
        ) {
            Icon(
                imageVector = Icons.Default.Radio,
                contentDescription = "Changer de station",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(iconSize)
            )
        }

        // Top RIGHT: Settings (like iOS gearshape, top right)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 16.dp, end = 20.dp)
                .size(btnSize)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onOpenSettings)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Paramètres",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(iconSize)
            )
        }

        // Bottom control bar — Play centré, Share en bas à droite
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = barVerticalPadding)
        ) {
                // Play/Stop — centré, plus grand que les boutons côtés (comme iOS playSize)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(playSize)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isPlaying) AccentRed else AccentBlue)
                        .then(if (!isLoading) Modifier.clickable(onClick = onTogglePlayback) else Modifier)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(playIconSize),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Lecture",
                            tint = Color.White,
                            modifier = Modifier.size(playIconSize)
                        )
                    }
                }

                // AirPlay slot — spacer symétrique (AirPlay = iOS uniquement)
                Spacer(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(btnSize)
                )

                // Share — coin droit (Circle comme iOS, même taille que boutons du haut)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(btnSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (canShare) 0.15f else 0.05f))
                        .clickable(enabled = canShare, onClick = onShare)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        tint = Color.White.copy(alpha = if (canShare) 0.9f else 0.4f),
                        modifier = Modifier.size(iconSize)
                    )
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
    size: Int,
    onClick: (() -> Unit)? = null
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
