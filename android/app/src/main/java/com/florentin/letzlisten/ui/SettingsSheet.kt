package com.florentin.letzlisten.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.florentin.letzlisten.player.AppLanguage
import com.florentin.letzlisten.player.LanguageManager
import com.florentin.letzlisten.ui.theme.AccentBlue
import com.florentin.letzlisten.ui.theme.AccentRed
import com.florentin.letzlisten.ui.theme.TextPrimary
import com.florentin.letzlisten.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun SettingsSheet(
    languageManager: LanguageManager,
    currentLanguage: AppLanguage,
    favoritesCount: Int,
    continuousPlayback: Boolean,
    appVersion: String,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetContinuousPlayback: (Boolean) -> Unit,
    exportData: () -> ByteArray?,
    onImportBytes: (ByteArray) -> Int,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val hasFavorites = favoritesCount > 0
    val context = LocalContext.current
    var showConfirmClear by remember { mutableStateOf(false) }
    var importFeedback by remember { mutableStateOf<String?>(null) }
    var saveFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(importFeedback) {
        if (importFeedback != null) {
            delay(3000)
            importFeedback = null
        }
    }
    LaunchedEffect(saveFeedback) {
        if (saveFeedback != null) {
            delay(3000)
            saveFeedback = null
        }
    }

    fun shareExport(bytes: ByteArray) {
        val file = File(context.cacheDir, "favoris-letzlisten.json")
        file.writeBytes(bytes)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
        saveFeedback = languageManager.exportSuccess(favoritesCount)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@rememberLauncherForActivityResult
            val count = onImportBytes(bytes)
            importFeedback = if (count < 0) languageManager.importFailed else languageManager.importSuccess(count)
        } catch (_: Exception) {
            importFeedback = languageManager.importFailed
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            text = { Text(languageManager.confirmClearAll, color = TextPrimary) },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmClear = false
                    onClearAll()
                }) { Text(languageManager.clearAll, color = AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text(languageManager.cancel, color = TextSecondary)
                }
            }
        )
    }

    // Header iOS-style : titre centré + bouton "Terminé" à droite
    Column(modifier = Modifier.fillMaxHeight(0.85f)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = languageManager.settings,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.align(Alignment.Center)
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(
                    text = languageManager.done,
                    color = AccentBlue,
                    fontSize = 17.sp
                )
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        // ── Section Paramètres ──────────────────────────────────────
        SettingsSectionLabel(languageManager.settings.uppercase())

        SettingsCard {
            // Langue
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Text(
                    text = languageManager.language,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(
                            text = "${currentLanguage.flag}  ${currentLanguage.displayName}",
                            color = AccentBlue,
                            fontSize = 15.sp
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AppLanguage.values()
                            .sortedWith(compareBy({ it != AppLanguage.LB }, { it.displayName }))
                            .forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.flag}  ${lang.displayName}") },
                                    onClick = { onSetLanguage(lang); expanded = false }
                                )
                            }
                    }
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Lecture continue
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = languageManager.continuousPlayback,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = languageManager.continuousPlaybackHint,
                        fontSize = 13.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = continuousPlayback,
                    onCheckedChange = onSetContinuousPlayback,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentBlue
                    )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Section Favoris ─────────────────────────────────────────
        SettingsSectionLabel(languageManager.favorites.uppercase())

        SettingsCard {
            // Export — share sheet (comme iOS)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (hasFavorites) Modifier.settingsRowClickable {
                            val bytes = exportData()
                            if (bytes != null) shareExport(bytes)
                        } else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Text(
                    text = languageManager.exportFavorites,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasFavorites) TextPrimary else TextPrimary.copy(alpha = 0.3f),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.IosShare,
                    contentDescription = null,
                    tint = if (hasFavorites) AccentBlue else AccentBlue.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Feedback save
            AnimatedVisibility(
                visible = saveFeedback != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = saveFeedback ?: "",
                        fontSize = 14.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Import
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .settingsRowClickable { importLauncher.launch(arrayOf("application/json", "*/*")) }
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Text(
                    text = languageManager.importFavorites,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Feedback import
            AnimatedVisibility(
                visible = importFeedback != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Text(
                        text = importFeedback ?: "",
                        fontSize = 14.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Tout supprimer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (hasFavorites) Modifier.settingsRowClickable { showConfirmClear = true } else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 13.dp)
            ) {
                Text(
                    text = languageManager.clearAll,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasFavorites) AccentRed else AccentRed.copy(alpha = 0.4f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Section À propos ────────────────────────────────────────
        SettingsSectionLabel(languageManager.about.uppercase())

        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageManager.version,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = appVersion,
                    fontSize = 17.sp,
                    color = TextSecondary.copy(alpha = 0.5f)
                )
            }
        }

    } // fin Column scrollable
    } // fin Column fillMaxHeight
}

// Extension pour rendre une Row cliquable
private fun Modifier.settingsRowClickable(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary.copy(alpha = 0.45f),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(12.dp)
            ),
        content = content
    )
}

