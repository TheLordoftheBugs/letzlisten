package com.florentin.letzlisten.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.florentin.letzlisten.data.RadioStation
import com.florentin.letzlisten.player.AppLanguage
import com.florentin.letzlisten.player.LanguageManager
import com.florentin.letzlisten.player.RadioViewModel
import com.florentin.letzlisten.player.TrackInfo
import com.florentin.letzlisten.ui.theme.AccentBlue
import com.florentin.letzlisten.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RadioViewModel) {
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val hasStartedPlaying by viewModel.hasStartedPlaying.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isStationsLoading by viewModel.isStationsLoading.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val isFavorited by viewModel.isFavorited.collectAsStateWithLifecycle()
    val albumArtUrl by viewModel.albumArtUrl.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val languageManager = remember { LanguageManager(context) }
    val currentLanguage by languageManager.currentLanguage.collectAsStateWithLifecycle()

    var showStationPicker by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Un téléphone en paysage a une largeur >= 600dp mais une hauteur ~360dp.
        // On exige une hauteur >= 480dp pour distinguer les vraies tablettes.
        val isTablet = maxWidth >= 600.dp && maxHeight >= 480.dp

        if (isTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                StationListPanel(
                    stations = stations,
                    currentStation = currentStation,
                    isStationsLoading = isStationsLoading,
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
                    languageFlag = currentLanguage.flag,
                    albumArtUrl = albumArtUrl,
                    artworkSize = 280,
                    onTogglePlayback = { viewModel.togglePlayback() },
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onOpenStationPicker = {}, // sidebar always visible on tablet
                    onOpenFavorites = { showFavorites = true },
                    onOpenLanguagePicker = { showLanguagePicker = true },
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
                languageFlag = currentLanguage.flag,
                albumArtUrl = albumArtUrl,
                onTogglePlayback = { viewModel.togglePlayback() },
                onToggleFavorite = { viewModel.toggleFavorite() },
                onOpenStationPicker = { showStationPicker = true },
                onOpenFavorites = { showFavorites = true },
                onOpenLanguagePicker = { showLanguagePicker = true },
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
                        isStationsLoading = isStationsLoading,
                        chooseYourRadioLabel = languageManager.chooseYourRadio,
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
                    clearAllLabel = languageManager.clearAll,
                    noFavoritesLabel = languageManager.noFavoritesYet,
                    noFavoritesHintLabel = languageManager.noFavoritesHint,
                    confirmClearAllLabel = languageManager.confirmClearAll,
                    cancelLabel = languageManager.cancel,
                    onRemove = { viewModel.removeFavorite(it) },
                    onClearAll = { viewModel.clearAllFavorites() }
                )
            }
        }

        // Language picker sheet (like iOS LanguagePickerView)
        if (showLanguagePicker) {
            ModalBottomSheet(
                onDismissRequest = { showLanguagePicker = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = SurfaceDark
            ) {
                LanguagePickerSheet(
                    title = languageManager.selectLanguage,
                    currentLanguage = currentLanguage,
                    onSelectLanguage = {
                        languageManager.setLanguage(it)
                        showLanguagePicker = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguagePickerSheet(
    title: String,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        val sortedLanguages = AppLanguage.values()
            .sortedWith(compareBy({ it != AppLanguage.LB }, { it.displayName }))
        sortedLanguages.forEach { language ->
            Surface(
                onClick = { onSelectLanguage(language) },
                color = if (language == currentLanguage) AccentBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 20.dp)
                ) {
                    Text(text = language.flag, fontSize = 28.sp)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = language.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(Modifier.weight(1f))
                    if (language == currentLanguage) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
