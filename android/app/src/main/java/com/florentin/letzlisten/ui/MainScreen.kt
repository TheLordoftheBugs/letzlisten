package com.florentin.letzlisten.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.florentin.letzlisten.player.RadioViewModel

@Composable
fun MainScreen(viewModel: RadioViewModel) {
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val currentStation by viewModel.currentStation.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()

    // Use adaptive layout: sidebar on tablets, bottom sheet on phones
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp

        if (isTablet) {
            // Tablet: persistent sidebar + player
            Row(modifier = Modifier.fillMaxSize()) {
                StationListPanel(
                    stations = stations,
                    currentStation = currentStation,
                    onStationSelected = { viewModel.switchStation(it) },
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                )

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                PlayerScreen(
                    currentStation = currentStation,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    artworkSize = 280,
                    onTogglePlayback = { viewModel.togglePlayback() },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Phone: full-screen player + bottom sheet station picker
            var showStationPicker by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            PlayerScreen(
                currentStation = currentStation,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                isLoading = isLoading,
                onTogglePlayback = { viewModel.togglePlayback() },
                modifier = Modifier.fillMaxSize()
            )

            if (showStationPicker) {
                ModalBottomSheet(
                    onDismissRequest = { showStationPicker = false },
                    sheetState = sheetState
                ) {
                    StationListPanel(
                        stations = stations,
                        currentStation = currentStation,
                        onStationSelected = {
                            viewModel.switchStation(it)
                            showStationPicker = false
                        },
                        modifier = Modifier.fillMaxHeight(0.85f)
                    )
                }
            }
        }
    }
}
