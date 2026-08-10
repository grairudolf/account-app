package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.domain.models.AccountabilityDomainModel
import com.example.ui.theme.*

@Composable
fun DomainsScreen(
    strings: AppStrings,
    domains: List<AccountabilityDomainModel>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDomainClick: (String) -> Unit,
    onAddCustomDomain: (name: String, desc: String, icon: String, unit: String) -> Unit,
    onSetDomainGoal: (domainId: String, title: String, target: Double, unit: String, freq: String) -> Unit = { _, _, _, _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGoalDomain by remember { mutableStateOf<AccountabilityDomainModel?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.05f),
                radius = w * 0.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.15f)
            )
            drawCircle(
                color = AccentPurple.copy(alpha = 0.04f),
                radius = w * 0.6f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.75f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Field with sleek 28.dp rounded background
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(strings.searchDomains) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("domains_search_input"),
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = DividerColor
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.spiritualDisciplines,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("add_custom_domain_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addCustomDomain)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(domains) { domain ->
                DomainCard(
                    domain = domain,
                    strings = strings,
                    onClick = { onDomainClick(domain.id) },
                    onConfigureGoal = { selectedGoalDomain = domain }
                )
            }
        }
    }

    if (showAddDialog) {
        AddCustomDomainDialog(
            strings = strings,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, icon, unit ->
                onAddCustomDomain(name, desc, icon, unit)
                showAddDialog = false
            }
        )
    }

    selectedGoalDomain?.let { dom ->
        SetDomainGoalDialog(
            domain = dom,
            strings = strings,
            onDismiss = { selectedGoalDomain = null },
            onConfirm = { title, target, unit, freq ->
                onSetDomainGoal(dom.id, title, target, unit, freq)
                selectedGoalDomain = null
            }
        )
    }
}
}

@Composable
fun DomainCard(
    domain: AccountabilityDomainModel,
    strings: AppStrings,
    onClick: () -> Unit,
    onConfigureGoal: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, DividerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("domain_card_${domain.id}")
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (domain.isCustom) PurpleContainer else LightBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (domain.id) {
                        "ddewg" -> Icons.Default.MenuBook
                        "bible_reading" -> Icons.Default.AutoStories
                        "prayer_alone", "prayer_with_others" -> Icons.Default.SelfImprovement
                        "fasting" -> Icons.Default.Restaurant
                        "soul_winning" -> Icons.Default.Groups
                        "giving" -> Icons.Default.VolunteerActivism
                        else -> Icons.Default.Bookmark
                    },
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.getDomainTitle(domain.titleKey),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.getDomainDesc(domain.descKey),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onConfigureGoal) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Set Goal",
                    tint = PrimaryBlue
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SetDomainGoalDialog(
    domain: AccountabilityDomainModel,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (title: String, target: Double, unit: String, freq: String) -> Unit
) {
    val defaultTitle = strings.getDomainTitle(domain.titleKey)
    val unitLabel = domain.measurementUnit.ifBlank { "Count" }
    var title by remember { mutableStateOf("Target $defaultTitle Goal") }
    var targetText by remember { mutableStateOf("1") }
    var selectedFreq by remember { mutableStateOf("DAILY") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Discipline Goal: $defaultTitle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Quantity ($unitLabel)") },
                    singleLine = true
                )
                Text("Target Frequency:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DAILY", "WEEKLY", "MONTHLY").forEach { freq ->
                        FilterChip(
                            selected = selectedFreq == freq,
                            onClick = { selectedFreq = freq },
                            label = { Text(freq) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 1.0
                    onConfirm(title, target, unitLabel, selectedFreq)
                }
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun AddCustomDomainDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, icon: String, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Count") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addCustomDomain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Domain Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_domain_name_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_domain_desc_input")
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Measurement Unit (e.g., Pages, Minutes)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_domain_unit_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, desc, "ic_custom", unit)
                    }
                },
                modifier = Modifier.testTag("confirm_custom_domain_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
