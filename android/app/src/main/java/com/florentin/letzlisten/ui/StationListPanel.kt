package com.florentin.letzlisten.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florentin.letzlisten.data.RadioStation
import com.florentin.letzlisten.ui.theme.AccentBlue
import com.florentin.letzlisten.ui.theme.SurfaceDark
import com.florentin.letzlisten.ui.theme.TextPrimary
import com.florentin.letzlisten.ui.theme.TextSecondary

@Composable
fun StationListPanel(
    stations: List<RadioStation>,
    currentStation: RadioStation?,
    chooseYourRadioLabel: String,
    onStationSelected: (RadioStation) -> Unit,
    doneLabel: String? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceDark)
    ) {
        if (doneLabel != null && onDismiss != null) {
            // Header iOS-style (sheet mobile) : titre centré + bouton "Terminé" à droite
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = chooseYourRadioLabel,
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
                        text = doneLabel,
                        color = AccentBlue,
                        fontSize = 17.sp
                    )
                }
            }
        } else {
            // Sidebar tablette : titre simple à gauche
            Text(
                text = chooseYourRadioLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            )
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(stations, key = { it.id }) { station ->
                StationRow(
                    station = station,
                    isSelected = station.id == currentStation?.id,
                    onClick = { onStationSelected(station) }
                )
            }
        }
    }
}

@Composable
private fun StationRow(
    station: RadioStation,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        StationLogo(
            station = station,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = station.name,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = TextPrimary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
