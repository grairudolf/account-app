package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.localization.AppLanguage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmfiTopBar(
    title: String,
    userName: String = "Disciple",
    profileImageUri: String? = null,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    unreadCount: Int = 0
) {
    var showLanguageMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile avatar button to Settings
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.5.dp, DividerColor, CircleShape)
                            .clickable { onProfileClick() }
                            .testTag("top_bar_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profileImageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = profileImageUri,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(LightBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                // Language Switcher Dropdown (Pill Badge)
                Box {
                    Surface(
                        onClick = { showLanguageMenu = true },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, DividerColor),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("top_bar_language_selector")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val langPillText = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "🇬🇧 EN"
                                AppLanguage.FRENCH -> "🇫🇷 FR"
                            }
                            Text(
                                text = langPillText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Language",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        val languageList = listOf(
                            Triple("🇬🇧", AppLanguage.ENGLISH, "English"),
                            Triple("🇫🇷", AppLanguage.FRENCH, "Français")
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

                // Rounded Icon Action Buttons
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("top_bar_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("top_bar_notification_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = StatusError,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}


