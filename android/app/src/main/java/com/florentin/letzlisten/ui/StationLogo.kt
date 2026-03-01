package com.florentin.letzlisten.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.florentin.letzlisten.R
import com.florentin.letzlisten.data.RadioStation
import com.florentin.letzlisten.ui.theme.TextPrimary

/**
 * Maps a logoImageName (from JSON) to a bundled Android drawable resource,
 * mirroring the images stored in iOS Assets.xcassets.
 */
@DrawableRes
fun bundledLogoRes(logoImageName: String?): Int? = when (logoImageName) {
    "CountryLogo" -> R.drawable.countrylogo
    "CrazyPoisonsLogo" -> R.drawable.crazypoisonslogo
    "RGLLogo" -> R.drawable.rgllogo
    else -> null
}

/**
 * Ordered list of logo URLs to try for a station (mirrors iOS FaviconFetcher priority):
 * Facebook → Graph API; others → apple-touch-icon variants → favicon.ico → Google Favicon.
 */
fun stationLogoUrls(station: RadioStation): List<String> {
    val website = station.websiteUrl ?: return emptyList()
    return try {
        val parsed = java.net.URL(website)
        val host = parsed.host
        val base = "${parsed.protocol}://$host"
        if (host.contains("facebook.com")) {
            val page = parsed.path.trim('/')
            if (page.isNotEmpty())
                listOf("https://graph.facebook.com/$page/picture?type=large&width=500&height=500")
            else emptyList()
        } else {
            listOf(
                "$base/apple-touch-icon.png",
                "$base/apple-touch-icon-precomposed.png",
                "$base/favicon.ico",
                "https://www.google.com/s2/favicons?domain=$host&sz=256"
            )
        }
    } catch (_: Exception) { emptyList() }
}

/**
 * Displays a station logo for use in the station list rows, mirroring iOS FaviconFetcher priority:
 *  1. Bundled drawable (if logoImageName maps to a local asset)
 *  2. URL cascade: Facebook Graph API / apple-touch-icon / favicon.ico / Google Favicon
 *  3. Text placeholder with station initials
 */
@Composable
fun StationLogo(station: RadioStation, modifier: Modifier = Modifier) {
    val bundledRes = remember(station.logoImageName) { bundledLogoRes(station.logoImageName) }
    val logoUrls = remember(station.id) { stationLogoUrls(station) }
    var logoIndex by remember(station.id) { mutableIntStateOf(0) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(Color.White.copy(alpha = 0.1f))
    ) {
        when {
            bundledRes != null -> AsyncImage(
                model = bundledRes,
                contentDescription = station.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            logoUrls.getOrNull(logoIndex) != null -> AsyncImage(
                model = logoUrls[logoIndex],
                contentDescription = station.name,
                contentScale = ContentScale.Fit,
                onError = { logoIndex++ },
                modifier = Modifier.fillMaxSize()
            )
            else -> Text(
                text = station.name
                    .split(" ", "-")
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .take(2)
                    .joinToString(""),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary.copy(alpha = 0.7f)
            )
        }
    }
}
