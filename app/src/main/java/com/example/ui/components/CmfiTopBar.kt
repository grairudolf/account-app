package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.localization.AppLanguage
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmfiTopBar(
    title: String,
    userName: String = "Disciple",
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    var showLanguageMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBlueDark)
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile avatar button to Settings
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onProfileClick() }
                            .testTag("top_bar_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = PrimaryBlueDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            actions = {
                // Language Switcher Dropdown
                Box {
                    Surface(
                        onClick = { showLanguageMenu = true },
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("top_bar_language_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val langPillText = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "🇬🇧 EN"
                                AppLanguage.FRENCH -> "🇫🇷 FR"
                                AppLanguage.SPANISH -> "🇪🇸 ES"
                                AppLanguage.PORTUGUESE -> "🇵🇹 PT"
                                AppLanguage.SWAHILI -> "🇰🇪 SW"
                                AppLanguage.ARABIC -> "🇸🇦 AR"
                            }
                            Text(
                                text = langPillText,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Language",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        val languageList = listOf(
                            Triple("🇬🇧", AppLanguage.ENGLISH, "English"),
                            Triple("🇫🇷", AppLanguage.FRENCH, "Français"),
                            Triple("🇪🇸", AppLanguage.SPANISH, "Español"),
                            Triple("🇵🇹", AppLanguage.PORTUGUESE, "Português"),
                            Triple("🇰🇪", AppLanguage.SWAHILI, "Kiswahili"),
                            Triple("🇸🇦", AppLanguage.ARABIC, "العربية")
                        )
                        languageList.forEach { (flag, lang, name) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(flag, fontSize = 16.sp)
                                        Text(
                                            name,
                                            fontWeight = if (currentLanguage == lang) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                onClick = {
                                    showLanguageMenu = false
                                    onLanguageSelected(lang)
                                },
                                modifier = Modifier.testTag("lang_option_${lang.code}")
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("top_bar_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("top_bar_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PrimaryBlueDark,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }
}

