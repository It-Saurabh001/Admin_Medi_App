package com.saurabh.mediadminapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Rounded image with themed placeholder for loading, error and missing url. */
@Composable
fun ThumbImage(url: String?, fallback: ImageVector, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    val shape = MaterialTheme.shapes.small
    val placeholder = @Composable {
        Box(
            Modifier.size(size).clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) { Icon(fallback, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (url.isNullOrBlank()) { Box(modifier) { placeholder() }; return }
    SubcomposeAsyncImage(
        model = url, contentDescription = null, contentScale = ContentScale.Crop,
        modifier = modifier.size(size).clip(shape),
        loading = { placeholder() }, error = { placeholder() }
    )
}

private val DAY_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)
private val TIME_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM, h:mm a", Locale.ENGLISH)

fun Instant?.dayLabel(): String = this?.atZone(ZoneId.systemDefault())?.format(DAY_FMT) ?: "Unknown date"
fun Instant?.dateTimeLabel(): String = this?.atZone(ZoneId.systemDefault())?.format(TIME_FMT) ?: "-"
