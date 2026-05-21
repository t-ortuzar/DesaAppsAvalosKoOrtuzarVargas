package com.example.desaappsavaloskoortuzarvargas.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Reusable card header image used by GameCard, DiscountCard, NewsCard.
 * Shows a placeholder with the game name when image URL is empty or fails to load.
 */
@Composable
fun CardHeaderImage(
    imageUrl: String,
    contentDescription: String,
    height: Dp = 180.dp,
    cornerRadius: Dp = 8.dp
) {
    if (imageUrl.isEmpty()) {
        // Placeholder for games without images (non-Steam exclusives)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contentDescription,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Handles loading → empty → content states used across multiple screens.
 */
@Composable
fun <T> LoadingContent(
    isLoading: Boolean,
    items: List<T>,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    content: @Composable (List<T>) -> Unit
) {
    when {
        isLoading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
        }
        items.isEmpty() -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { Text(emptyMessage) }
        }
        else -> content(items)
    }
}

/**
 * Reusable settings card wrapper with consistent elevation and shape.
 */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Reusable section header (bold title) used in GameDetailScreen and SettingsScreen.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

/**
 * Detail section with title and content text, used in GameDetailScreen.
 */
@Composable
fun DetailSection(
    title: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        SectionHeader(title)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (valueColor == Color.Unspecified)
                MaterialTheme.colorScheme.onBackground
            else valueColor
        )
    }
}

/**
 * Horizontally scrollable tag chips used in GameCard and GameDetailScreen.
 */
@Composable
fun TagChips(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty()) return
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.forEach { tag ->
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Price/status badge used in DiscountCard.
 */
@Composable
fun PriceBadge(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/**
 * Row with label and switch, used in notification preference dialogs.
 */
@Composable
fun LabeledSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

