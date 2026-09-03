package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.ui.theme.*

enum class BottomTab(
    val route: String,
    val icon: ImageVector,
    val getTitle: (AppStrings) -> String
) {
    DASHBOARD("dashboard", Icons.Default.Home, { it.dashboard }),
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
    val selectedIndex = remember(currentRoute) {
        val idx = BottomTab.entries.indexOfFirst { it.route == currentRoute }
        if (idx >= 0) idx else 0
    }

    val isDark = isAppInDarkTheme()
    val navBarColor = if (isDark) DarkNavBarBackground else LightNavBarBackground
    val shadowAmbient = if (isDark) Color(0x66000000) else BrandDarkNavy.copy(alpha = 0.40f)
    val shadowSpot = if (isDark) Color(0x99000000) else BrandDarkNavy.copy(alpha = 0.30f)
    val inactiveIconColor = if (isDark) BrandLightText else Color(0xFFD6DCED)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("main_bottom_navigation_bar"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(16.dp, RoundedCornerShape(34.dp), ambientColor = shadowAmbient, spotColor = shadowSpot)
                .clip(RoundedCornerShape(34.dp)),
            color = navBarColor
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val tabCount = BottomTab.entries.size
                val tabWidth = maxWidth / tabCount
                val pillWidth = (tabWidth - 8.dp).coerceAtLeast(46.dp)

                // Smooth sliding active pill indicator
                val targetOffset = tabWidth * selectedIndex + (tabWidth - pillWidth) / 2
                val animatedPillOffset by animateDpAsState(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "pill_slide"
                )

                // The sliding active background pill
                Box(
                    modifier = Modifier
                        .offset(x = animatedPillOffset)
                        .size(width = pillWidth, height = 48.dp)
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(BrandWarmGold, BrandVibrantYellow, BrandBrightYellow)
                            )
                        )
                )

                // Tab items layer
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomTab.entries.forEachIndexed { index, tab ->
                        val isSelected = selectedIndex == index

                        // Scale animation for active tab icon
                        val iconScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.18f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "icon_scale_${tab.route}"
                        )

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) BrandDarkNavy else inactiveIconColor,
                            animationSpec = tween(200),
                            label = "icon_color_${tab.route}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, color = BrandVibrantYellow.copy(alpha = 0.2f))
                                ) { onTabSelected(tab) }
                                .testTag("nav_tab_${tab.route}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.getTitle(strings),
                                tint = iconColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(iconScale)
                            )
                        }
                    }
                }
            }
        }
    }
}


