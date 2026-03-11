package com.florentin.letzlisten.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun SettingsSheet(
    languageManager: LanguageManager,
    currentLanguage: AppLanguage,
    hasFavorites: Boolean,
    continuousPlayback: Boolean,
    appVersion: String,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetContinuousPlayback: (Boolean) -> Unit,
    exportData: () -> ByteArray?,
    onImportBytes: (ByteArray) -> Int,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    var showConfirmClear by remember { mutableStateOf(false) }
    var importFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(importFeedback) {
        if (importFeedback != null) {
            delay(3000)
            importFeedback = null
        }
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

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp)
    ) {
        Text(
            text = languageManager.settings,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        // ── Section Paramètres ──────────────────────────────────────
        SettingsSectionLabel(languageManager.settings.uppercase())

        SettingsCard {
            // Language row
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
                        val sorted = AppLanguage.values()
                            .sortedWith(compareBy({ it != AppLanguage.LB }, { it.displayName }))
                        sorted.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.flag}  ${lang.displayName}") },
                                onClick = {
                                    onSetLanguage(lang)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Continuous playback toggle
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
            // Export
            TextButton(
                onClick = { shareExportFile(context, exportData) },
                enabled = hasFavorites,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languageManager.exportFavorites,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (hasFavorites) TextPrimary else TextPrimary.copy(alpha = 0.3f)
                    )
                }
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Import
            TextButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languageManager.importFavorites,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Import feedback
            if (importFeedback != null) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = importFeedback!!,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Clear all
            TextButton(
                onClick = { showConfirmClear = true },
                enabled = hasFavorites,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languageManager.clearAll,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (hasFavorites) AccentRed else AccentRed.copy(alpha = 0.4f)
                    )
                }
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
                    color = TextSecondary
                )
            }
        }
    }
}

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

private fun shareExportFile(context: Context, exportData: () -> ByteArray?) {
    val bytes = exportData() ?: return
    val file = File(context.cacheDir, "favoris-letzlisten.json")
    file.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
