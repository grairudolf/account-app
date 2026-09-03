package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.local.BibleMetadata
import com.example.ui.screens.BibleReadingSegment
import com.example.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReadingSelector(
    strings: AppStrings,
    isSpanMode: Boolean,
    onSpanModeChange: (Boolean) -> Unit,
    startBook: String,
    onStartBookChange: (String) -> Unit,
    startChapter: Int,
    onStartChapterChange: (Int) -> Unit,
    endBook: String,
    onEndBookChange: (String) -> Unit,
    endChapter: Int,
    onEndChapterChange: (Int) -> Unit,
    discreteSegments: List<BibleReadingSegment>,
    onAddSegment: () -> Unit,
    onRemoveSegment: (Int) -> Unit,
    onUpdateSegment: (Int, BibleReadingSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode Selector: Continuous Span vs Discrete Segments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = isSpanMode,
                onClick = { onSpanModeChange(true) },
                label = { Text(strings.readingModeSpan, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        Icons.Default.AutoStories,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            FilterChip(
                selected = !isSpanMode,
                onClick = { onSpanModeChange(false) },
                label = { Text(strings.readingModeSegments, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        if (isSpanMode) {
            // Continuous Multi-Book Span Selection Mode
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Point Section
                    Text(
                        text = strings.startPoint.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Book Dropdown
                        var startBookExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = startBookExpanded,
                            onExpandedChange = { startBookExpanded = !startBookExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = strings.getBibleBookName(startBook),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.startBook) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startBookExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = startBookExpanded,
                                onDismissRequest = { startBookExpanded = false }
                            ) {
                                BibleMetadata.BOOKS.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text("${strings.getBibleBookName(b.name)} (${b.chapters} ch)") },
                                        onClick = {
                                            onStartBookChange(b.name)
                                            onStartChapterChange(1)
                                            startBookExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Start Chapter Dropdown
                        val startBookInfo = BibleMetadata.getBook(startBook) ?: BibleMetadata.BOOKS.first()
                        var startChExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = startChExpanded,
                            onExpandedChange = { startChExpanded = !startChExpanded },
                            modifier = Modifier.weight(0.9f)
                        ) {
                            OutlinedTextField(
                                value = "Ch. $startChapter",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.startChapter) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startChExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = startChExpanded,
                                onDismissRequest = { startChExpanded = false }
                            ) {
                                (1..startBookInfo.chapters).forEach { ch ->
                                    DropdownMenuItem(
                                        text = { Text("Ch. $ch") },
                                        onClick = {
                                            onStartChapterChange(ch)
                                            startChExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    // End Point Section
                    Text(
                        text = strings.endPoint.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // End Book Dropdown
                        var endBookExpanded by remember { mutableStateOf(false) }
                        val startIdx = BibleMetadata.getBookIndex(startBook)
                        ExposedDropdownMenuBox(
                            expanded = endBookExpanded,
                            onExpandedChange = { endBookExpanded = !endBookExpanded },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = strings.getBibleBookName(endBook),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.endBook) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endBookExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = endBookExpanded,
                                onDismissRequest = { endBookExpanded = false }
                            ) {
                                BibleMetadata.BOOKS.forEachIndexed { idx, b ->
                                    val isPast = idx >= startIdx
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${strings.getBibleBookName(b.name)} (${b.chapters} ch)",
                                                fontWeight = if (isPast) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            onEndBookChange(b.name)
                                            val maxCh = b.chapters
                                            val defaultEnd = if (b.name.equals(startBook, ignoreCase = true)) {
                                                startChapter.coerceAtMost(maxCh)
                                            } else {
                                                maxCh
                                            }
                                            onEndChapterChange(defaultEnd)
                                            endBookExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // End Chapter Dropdown
                        val endBookInfo = BibleMetadata.getBook(endBook) ?: BibleMetadata.BOOKS.first()
                        var endChExpanded by remember { mutableStateOf(false) }
                        val minEndChapter = if (endBook.equals(startBook, ignoreCase = true)) startChapter else 1
                        ExposedDropdownMenuBox(
                            expanded = endChExpanded,
                            onExpandedChange = { endChExpanded = !endChExpanded },
                            modifier = Modifier.weight(0.9f)
                        ) {
                            OutlinedTextField(
                                value = "Ch. $endChapter",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.endChapter) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endChExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = endChExpanded,
                                onDismissRequest = { endChExpanded = false }
                            ) {
                                (minEndChapter..endBookInfo.chapters).forEach { ch ->
                                    DropdownMenuItem(
                                        text = { Text("Ch. $ch") },
                                        onClick = {
                                            onEndChapterChange(ch)
                                            endChExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Canonical Breakdown Card
            val breakdown = remember(startBook, startChapter, endBook, endChapter) {
                BibleMetadata.getSpanBreakdown(startBook, startChapter, endBook, endChapter)
            }
            val totalSpanChapters = remember(breakdown) {
                breakdown.sumOf { it.chaptersReadCount }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.spanBreakdown,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = String.format(strings.chaptersAcrossBooks, totalSpanChapters, breakdown.size),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Multi-Book Breakdown Flow
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        breakdown.forEachIndexed { i, seg ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (i > 0) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = if (seg.startChapter == seg.endChapter) {
                                        "${strings.getBibleBookName(seg.bookName)} Ch. ${seg.startChapter} (${seg.chaptersReadCount} ch)"
                                    } else {
                                        "${strings.getBibleBookName(seg.bookName)} Ch. ${seg.startChapter}–${seg.endChapter} (${seg.chaptersReadCount} ch)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

        } else {
            // Discrete Segments Mode (Legacy Retention)
            discreteSegments.forEachIndexed { index, segment ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.bookSegment} #${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (discreteSegments.size > 1) {
                                IconButton(
                                    onClick = { onRemoveSegment(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = strings.removeBookSegment,
                                        tint = StatusError
                                    )
                                }
                            }
                        }

                        var bookExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = bookExpanded,
                            onExpandedChange = { bookExpanded = !bookExpanded }
                        ) {
                            OutlinedTextField(
                                value = strings.getBibleBookName(segment.book),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.selectBibleBook) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = bookExpanded,
                                onDismissRequest = { bookExpanded = false }
                            ) {
                                BibleMetadata.BOOKS.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text("${strings.getBibleBookName(b.name)} (${b.chapters} ch)") },
                                        onClick = {
                                            onUpdateSegment(
                                                index,
                                                segment.copy(book = b.name, startChapter = 1, endChapter = 1)
                                            )
                                            bookExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val bookInfo = BibleMetadata.BOOKS.find { it.name.equals(segment.book, ignoreCase = true) } ?: BibleMetadata.BOOKS.first()
                        val maxCh = bookInfo.chapters

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var startChExpanded by remember { mutableStateOf(false) }
                            var endChExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = startChExpanded,
                                onExpandedChange = { startChExpanded = !startChExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = "Ch. ${segment.startChapter}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(strings.startChapter) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startChExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = startChExpanded,
                                    onDismissRequest = { startChExpanded = false }
                                ) {
                                    (1..maxCh).forEach { ch ->
                                        DropdownMenuItem(
                                            text = { Text("Ch. $ch") },
                                            onClick = {
                                                val newEnd = if (segment.endChapter < ch) ch else segment.endChapter
                                                onUpdateSegment(
                                                    index,
                                                    segment.copy(startChapter = ch, endChapter = newEnd)
                                                )
                                                startChExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = endChExpanded,
                                onExpandedChange = { endChExpanded = !endChExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = "Ch. ${segment.endChapter}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(strings.endChapter) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endChExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = endChExpanded,
                                    onDismissRequest = { endChExpanded = false }
                                ) {
                                    (segment.startChapter..maxCh).forEach { ch ->
                                        DropdownMenuItem(
                                            text = { Text("Ch. $ch") },
                                            onClick = {
                                                onUpdateSegment(
                                                    index,
                                                    segment.copy(endChapter = ch)
                                                )
                                                endChExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onAddSegment,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.addAnotherBook)
            }

            val totalDiscreteChapters = discreteSegments.sumOf { (it.endChapter - it.startChapter + 1).coerceAtLeast(1) }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = String.format(strings.totalChaptersCalculated, totalDiscreteChapters),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
