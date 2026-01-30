package com.betterappsstudio.inkrypt.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays tags as chips.
 */
@Composable
fun TagChips(
    tags: List<String>,
    modifier: Modifier = Modifier,
    onTagClick: ((String) -> Unit)? = null
) {
    if (tags.isEmpty()) return
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(tags) { tag ->
            TagChip(
                tag = tag,
                onClick = { onTagClick?.invoke(tag) }
            )
        }
    }
}

@Composable
private fun TagChip(
    tag: String,
    onClick: (() -> Unit)? = null
) {
    AssistChip(
        onClick = { onClick?.invoke() },
        label = {
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        modifier = Modifier.height(28.dp)
    )
}

