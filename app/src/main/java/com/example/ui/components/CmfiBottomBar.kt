package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings

enum class BottomTab(
    val route: String,
    val icon: ImageVector,
    val getTitle: (AppStrings) -> String
) {
    DASHBOARD("dashboard", Icons.Default.Dashboard, { it.dashboard }),
    DOMAINS("domains", Icons.Default.List, { it.domains }),
    GOALS("goals", Icons.Default.Flag, { it.goals }),
    STATISTICS("statistics", Icons.Default.BarChart, { it.statistics }),
    REPORTS("reports", Icons.AutoMirrored.Filled.Assignment, { it.reports })
}

@Composable
fun CmfiBottomBar(
    currentRoute: String,
    strings: AppStrings,
    onTabSelected: (BottomTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("main_bottom_navigation_bar")
        ) {
            BottomTab.entries.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.getTitle(strings)
                        )
                    },
                    label = {
                        Text(
                            text = tab.getTitle(strings),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.testTag("nav_tab_${tab.route}")
                )
            }
        }
    }
}

