package com.florentin.letzlisten.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florentin.letzlisten.data.Favorite
import com.florentin.letzlisten.ui.theme.AccentBlue
import com.florentin.letzlisten.ui.theme.AccentRed
import com.florentin.letzlisten.ui.theme.TextPrimary
import com.florentin.letzlisten.ui.theme.TextSecondary
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FavoritesSheet(
    favorites: List<Favorite>,
    favoritesLabel: String,
    noFavoritesLabel: String,
    noFavoritesHintLabel: String,
    doneLabel: String,
    onDismiss: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        // Header iOS-style : titre centré + bouton "Terminé" à droite
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = favoritesLabel,
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

        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

        if (favorites.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        noFavoritesLabel,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = noFavoritesHintLabel,
                        fontSize = 15.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites, key = { it.id }) { fav ->
                    FavoriteRow(fav = fav, onRemove = { onRemove(fav.id) })
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(fav: Favorite, onRemove: () -> Unit) {
    val context = LocalContext.current
    val dateStr = remember(fav.timestamp) {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(fav.timestamp))
    }
    // Card arrondie individuelle — comme iOS FavoriteRowView
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // Zone cliquable → recherche Google (comme iOS searchOnWeb)
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable {
                    val query = URLEncoder.encode("${fav.artist} ${fav.title}", "UTF-8")
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$query"))
                    )
                }
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp)
        ) {
            Text(
                text = fav.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = fav.artist,
                fontSize = 15.sp,
                color = TextPrimary.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            // Station — en bleu comme iOS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = null,
                    tint = AccentBlue.copy(alpha = 0.8f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = fav.stationName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentBlue.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            // Date — séparée comme iOS
            Text(
                text = dateStr,
                fontSize = 13.sp,
                color = TextPrimary.copy(alpha = 0.5f)
            )
        }
        // Bouton suppression — corbeille rouge comme iOS
        IconButton(
            onClick = onRemove,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer",
                tint = AccentRed.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
