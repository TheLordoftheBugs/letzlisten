package com.florentin.letzlisten.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florentin.letzlisten.data.RadioStation
import com.florentin.letzlisten.player.LanguageManager
import com.florentin.letzlisten.player.RadioViewModel
import com.florentin.letzlisten.player.TrackInfo
import com.florentin.letzlisten.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RadioViewModel) {
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val hasStartedPlaying by viewModel.hasStartedPlaying.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isFavorited by viewModel.isFavorited.collectAsStateWithLifecycle()
    val albumArtUrl by viewModel.albumArtUrl.collectAsStateWithLifecycle()
    val continuousPlayback by viewModel.continuousPlayback.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val languageManager = remember { LanguageManager(context) }
    val currentLanguage by languageManager.currentLanguage.collectAsStateWithLifecycle()

    val appVersion = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "—" }
        catch (_: Exception) { "—" }
    }

    var showStationPicker by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Un téléphone en paysage a une largeur >= 600dp mais une hauteur ~360dp.
        // On exige une hauteur >= 480dp pour distinguer les vraies tablettes.
        val isTablet = maxWidth >= 600.dp && maxHeight >= 480.dp

        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                StationListPanel(
                    stations = stations,
                    currentStation = currentStation,
                    chooseYourRadioLabel = languageManager.chooseYourRadio,
                    onStationSelected = { viewModel.switchStation(it) },
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                PlayerScreen(
                    currentStation = currentStation,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    hasStartedPlaying = hasStartedPlaying,
                    isLoading = isLoading,
                    isFavorited = isFavorited,
                    albumArtUrl = albumArtUrl,
                    artworkSize = 280,
                    onTogglePlayback = { viewModel.togglePlayback() },
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onOpenStationPicker = {}, // sidebar always visible on tablet
                    onOpenFavorites = { showFavorites = true },
                    onOpenSettings = { showSettings = true },
                    onShare = { if (viewModel.isPlaying.value) shareTrack(context, currentTrack, currentStation, languageManager) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            PlayerScreen(
                currentStation = currentStation,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                hasStartedPlaying = hasStartedPlaying,
                isLoading = isLoading,
                isFavorited = isFavorited,
                albumArtUrl = albumArtUrl,
                onTogglePlayback = { viewModel.togglePlayback() },
                onToggleFavorite = { viewModel.toggleFavorite() },
                onOpenStationPicker = { showStationPicker = true },
                onOpenFavorites = { showFavorites = true },
                onOpenSettings = { showSettings = true },
                onShare = { if (viewModel.isPlaying.value) shareTrack(context, currentTrack, currentStation, languageManager) },
                modifier = Modifier.fillMaxSize()
            )

            if (showStationPicker) {
                ModalBottomSheet(
                    onDismissRequest = { showStationPicker = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = SurfaceDark
                ) {
                    StationListPanel(
                        stations = stations,
                        currentStation = currentStation,
                        chooseYourRadioLabel = languageManager.chooseYourRadio,
                        doneLabel = languageManager.done,
                        onDismiss = { showStationPicker = false },
                        onStationSelected = {
                            viewModel.switchStation(it)
                            showStationPicker = false
                        },
                        modifier = Modifier.fillMaxHeight(0.85f)
                    )
                }
            }
        }

        if (showFavorites) {
            ModalBottomSheet(
                onDismissRequest = { showFavorites = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = SurfaceDark
            ) {
                FavoritesSheet(
                    favorites = favorites,
                    favoritesLabel = languageManager.favorites,
                    noFavoritesLabel = languageManager.noFavoritesYet,
                    noFavoritesHintLabel = languageManager.noFavoritesHint,
                    doneLabel = languageManager.done,
                    onDismiss = { showFavorites = false },
                    onRemove = { viewModel.removeFavorite(it) }
                )
            }
        }

        // Settings sheet — fillMaxHeight pour s'afficher en plein écran comme iOS .large detent
        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = SurfaceDark
            ) {
                SettingsSheet(
                    languageManager = languageManager,
                    currentLanguage = currentLanguage,
                    favoritesCount = favorites.size,
                    continuousPlayback = continuousPlayback,
                    appVersion = appVersion,
                    onSetLanguage = { languageManager.setLanguage(it) },
                    onSetContinuousPlayback = { viewModel.setContinuousPlayback(it) },
                    exportData = { viewModel.exportFavorites() },
                    onImportBytes = { viewModel.importFavorites(it) },
                    onClearAll = { viewModel.clearAllFavorites() },
                    onDismiss = { showSettings = false },
                    onUnlockSecret = { viewModel.unlockSecretStations() }
                )
            }
        }
    }
}

private fun shareTrack(
    context: Context,
    track: TrackInfo,
    station: RadioStation?,
    languageManager: LanguageManager
) {
    if (track.isUnknown) return
    val text = languageManager.shareMessage(
        artist = track.artist,
        title = track.title,
        station = station?.name ?: "LëtzListen",
        url = station?.websiteUrl
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
