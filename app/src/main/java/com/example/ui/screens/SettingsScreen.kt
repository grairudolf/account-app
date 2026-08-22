package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.SupportFeedbackDialog
import com.example.ui.components.TermsAndConditionsDialog
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.services.sync.SyncProgress
import com.example.services.sync.SyncStage

@Composable
fun SettingsScreen(
    strings: AppStrings,
    user: UserEntity?,
    currentLanguage: AppLanguage,
    currentTheme: ThemeMode,
    reminders: List<ReminderEntity>,
    isSyncing: Boolean = false,
    syncProgress: SyncProgress = SyncProgress(),
    onSyncCloudData: () -> Unit = {},
    onUpdateLanguage: (AppLanguage) -> Unit,
    onUpdateTheme: (ThemeMode) -> Unit,
    onUpdateProfileImage: (String) -> Unit = {},
    onUpdateProfile: (fullName: String, email: String, assembly: String, maker: String, phone: String, conversionDate: String, accountabilityDays: String) -> Unit,
    onAddReminder: (context: Context, domainId: String, title: String, msg: String, h: Int, m: Int) -> Unit,
    onEditReminder: (context: Context, domainId: String, title: String, msg: String, h: Int, m: Int, id: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onToggleReminder: (context: Context, ReminderEntity, Boolean) -> Unit = { _, _, _ -> },
    onDeleteReminder: (context: Context, String) -> Unit,
    onSignOut: () -> Unit
) {
    var showProfileDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val isFrench = currentLanguage == AppLanguage.FRENCH
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { sourceUri ->
            try {
                val file = java.io.File(context.filesDir, "user_avatar_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    java.io.FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                onUpdateProfileImage(file.absolutePath)
            } catch (e: Exception) {
                onUpdateProfileImage(sourceUri.toString())
            }
        }
    }

    val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val selectedDaysList = remember(user?.accountabilityDays) {
        user?.accountabilityDays?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: daysOfWeek
    }

    // Spiritual age calculation
    val spiritualAgeText = remember(user?.conversionDate) {
        val dateStr = user?.conversionDate
        if (!dateStr.isNullOrBlank()) {
            try {
                val convDate = LocalDate.parse(dateStr)
                val days = ChronoUnit.DAYS.between(convDate, LocalDate.now())
                if (days >= 0) {
                    val years = days / 365
                    val remDays = days % 365
                    if (years > 0) "$years years, $remDays days ($days days total)" else "$days days in Christ"
                } else "Date in future"
            } catch (e: Exception) {
                "Converted: $dateStr"
            }
        } else "Not specified"
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.05f),
                radius = w * 0.55f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.15f)
            )
            drawCircle(
                color = AccentPurple.copy(alpha = 0.04f),
                radius = w * 0.6f,
                center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.85f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Top Profile Banner Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = PrimaryBlueDark,
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .testTag("settings_profile_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Decorative Canvas Circles
                    androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = size.width * 0.4f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.2f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.03f),
                            radius = size.width * 0.5f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.8f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Large Avatar Box with Camera Overlay
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { photoPickerLauncher.launch("image/*") }
                                .testTag("profile_image_picker"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user?.profileImageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = user?.profileImageUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue,
                                border = BorderStroke(2.dp, Color.White),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        // User Name
                        Text(
                            text = user?.fullName?.ifBlank { strings.discipleProfile } ?: strings.discipleProfile,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Signed-In Email or Guest Badge
                        if (!user?.email.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = user!!.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        } else if (user?.isGuest == true) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonOutline,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.9f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Guest Mode (Offline)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Subtitle Location & Conversion Date Info (if set)
                        if (!user?.localAssembly.isNullOrBlank() || !user?.conversionDate.isNullOrBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!user?.localAssembly.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                        Text(
                                            text = user!!.localAssembly,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                if (!user?.conversionDate.isNullOrBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                        Text(
                                            text = user!!.conversionDate,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Profile Information Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.profileInformation,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Full Name Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.fullNameLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = user?.fullName?.ifBlank { strings.notSet } ?: strings.notSet,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                        // Email Address Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(strings.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = if (!user?.email.isNullOrBlank()) {
                                    user!!.email
                                } else if (user?.isGuest == true) {
                                    "Guest / Not Linked"
                                } else {
                                    strings.notSet
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (!user?.email.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                        // Local Assembly Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.localAssembly, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = user?.localAssembly?.ifBlank { strings.notSet } ?: strings.notSet,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                        // Disciple Maker Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.discipleMakerName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = user?.discipleMaker?.ifBlank { strings.notSet } ?: strings.notSet,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                        // Date of Conversion Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.conversionDate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = user?.conversionDate?.ifBlank { strings.notSet } ?: strings.notSet,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Spiritual Age / Time in Christ Row
                        if (!user?.conversionDate.isNullOrBlank()) {
                            HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.spiritualAgeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = spiritualAgeText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (!user?.phoneNumber.isNullOrBlank()) {
                            HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                            // Phone Number Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = user!!.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { showProfileDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_profile_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.editProfile, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Settings Section Card (Language & Dark Mode)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .testTag("settings_options_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.settings,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Language Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { languageDropdownExpanded = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            Text(strings.language, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.testTag("language_dropdown_trigger")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(if (currentLanguage == AppLanguage.FRENCH) "🇫🇷" else "🇬🇧")
                                    Text(currentLanguage.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = languageDropdownExpanded,
                                onDismissRequest = { languageDropdownExpanded = false }
                            ) {
                                AppLanguage.entries.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text(if (lang == AppLanguage.FRENCH) "🇫🇷" else "🇬🇧")
                                                Text(lang.displayName)
                                            }
                                        },
                                        onClick = {
                                            onUpdateLanguage(lang)
                                            languageDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                    // Dark Mode Toggle Row
                    val isDark = currentTheme == ThemeMode.DARK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDark) StreakGold else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(strings.darkMode, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        Switch(
                            checked = isDark,
                            onCheckedChange = { checked ->
                                onUpdateTheme(if (checked) ThemeMode.DARK else ThemeMode.LIGHT)
                            },
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }
                }
            }
        }

        // Reminders Section Header & List
        item {
            Row(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = strings.dailyReminders,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable {
                        editingReminder = null
                        showReminderDialog = true
                    }.testTag("add_reminder_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlarm,
                            contentDescription = "Add Reminder",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.addReminderBtn,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = strings.noActiveReminders,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                editingReminder = null
                                showReminderDialog = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.dailyReminders, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(reminders, key = { it.id }) { rem ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (rem.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxWidth()
                        .clickable {
                            editingReminder = rem
                            showReminderDialog = true
                        }
                        .testTag("reminder_item_${rem.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (rem.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (rem.isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = rem.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rem.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = String.format("%02d:%02d • %s", rem.hour, rem.minute, rem.message),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Switch(
                                checked = rem.isEnabled,
                                onCheckedChange = { isChecked ->
                                    onToggleReminder(context, rem, isChecked)
                                },
                                modifier = Modifier.testTag("toggle_reminder_${rem.id}")
                            )
                            IconButton(onClick = { onDeleteReminder(context, rem.id) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Reminder",
                                    tint = StatusError,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

        }

        // Legal & Support Dialog Action Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ListItem(
                        headlineContent = { Text(strings.privacyPolicy, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                        leadingContent = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier
                            .clickable { showPrivacyDialog = true }
                            .testTag("settings_privacy_policy")
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ListItem(
                        headlineContent = { Text(strings.termsConditions, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                        leadingContent = { Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier
                            .clickable { showTermsDialog = true }
                            .testTag("settings_terms_conditions")
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ListItem(
                        headlineContent = { Text(strings.supportFeedback, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface) },
                        leadingContent = { Icon(Icons.Default.HelpCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        modifier = Modifier
                            .clickable { showSupportDialog = true }
                            .testTag("settings_support_feedback")
                    )
                }
            }
        }

        // Need Help? Support CTA Card (High-Contrast in both Light & Dark Theme)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.needHelp,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = strings.needHelpDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { showSupportDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.getSupport, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Account Status, Cloud Sync & Sign Out
        item {
            Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth()) {
                if (user?.isGuest == true) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.CloudOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        "Guest Mode Active (Offline)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Your data is only stored on this device. Sign in to automatically sync and backup your records to the cloud.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(
                                onClick = onSignOut,
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In / Create Account", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cloud Sync Status Card
                        val activelySyncing = isSyncing || syncProgress.isSyncing
                        val animatedProgress by animateFloatAsState(
                            targetValue = if (activelySyncing) syncProgress.progress.coerceIn(0.05f, 1f) else 1f,
                            animationSpec = tween(350),
                            label = "sync_progress"
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                1.dp,
                                if (activelySyncing) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val iconColor = when {
                                        syncProgress.stage == SyncStage.ERROR -> StatusError
                                        activelySyncing -> MaterialTheme.colorScheme.primary
                                        else -> StatusSuccess
                                    }
                                    val icon = when {
                                        syncProgress.stage == SyncStage.DOWNLOADING -> Icons.Default.CloudDownload
                                        syncProgress.stage == SyncStage.UPLOADING -> Icons.Default.CloudUpload
                                        syncProgress.stage == SyncStage.ERROR -> Icons.Default.SyncProblem
                                        else -> Icons.Default.CloudDone
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = iconColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                tint = iconColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                if (activelySyncing) {
                                                    if (syncProgress.stage == SyncStage.DOWNLOADING) "Downloading from Cloud"
                                                    else if (syncProgress.stage == SyncStage.UPLOADING) "Uploading to Cloud"
                                                    else "Synchronizing..."
                                                } else "Cloud Backup & Sync Active",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            if (!user?.email.isNullOrBlank()) "Linked to: ${user!!.email}" else "Connected to Firebase Cloud Database",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (activelySyncing) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.padding(start = 4.dp)
                                        ) {
                                            Text(
                                                "${(animatedProgress * 100).toInt()}%",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                if (activelySyncing) {
                                    // Active Progress Section
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (syncProgress.stageTitle.isNotBlank()) syncProgress.stageTitle else "Syncing records...",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(12.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    strokeWidth = 2.dp
                                                )
                                                Text(
                                                    "Active",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        LinearProgressIndicator(
                                            progress = { animatedProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        )

                                        if (syncProgress.details.isNotBlank()) {
                                            Text(
                                                text = syncProgress.details,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Text(
                                            text = "Background sync active — you can continue using the app while data transfers.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                } else {
                                    // Idle / Finished State
                                    Text(
                                        "All your prayer sessions, scripture readings, fasts, disciples, goals, and reports are backed up in real-time. If you switch phones, simply sign in with this account to restore all your data.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (syncProgress.lastSyncTimeMs != null && syncProgress.lastSyncTimeMs > 0) {
                                        val timeStr = remember(syncProgress.lastSyncTimeMs) {
                                            val diffMin = (System.currentTimeMillis() - syncProgress.lastSyncTimeMs) / 60000
                                            when {
                                                diffMin < 1 -> "Just now"
                                                diffMin < 60 -> "$diffMin min ago"
                                                else -> {
                                                    val dt = java.time.Instant.ofEpochMilli(syncProgress.lastSyncTimeMs)
                                                        .atZone(java.time.ZoneId.systemDefault())
                                                        .toLocalDateTime()
                                                    "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
                                                }
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = StatusSuccess,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                "Last synced: $timeStr",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = onSyncCloudData,
                                        enabled = !activelySyncing,
                                        modifier = Modifier.fillMaxWidth().height(42.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sync & Backup Now", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Sign Out Button
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("sign_out_button"),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, StatusError.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = StatusError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.signOut, color = StatusError, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

    if (showProfileDialog) {
        EditProfileDialog(
            user = user,
            strings = strings,
            onDismiss = { showProfileDialog = false },
            onConfirm = { name, email, assembly, maker, phone, convDate ->
                onUpdateProfile(name, email, assembly, maker, phone, convDate, user?.accountabilityDays ?: "MON,TUE,WED,THU,FRI,SAT,SUN")
                showProfileDialog = false
            }
        )
    }

    if (showReminderDialog) {
        AddOrEditReminderDialog(
            strings = strings,
            initialReminder = editingReminder,
            onDismiss = {
                showReminderDialog = false
                editingReminder = null
            },
            onConfirm = { domainId, title, msg, h, m ->
                if (editingReminder != null) {
                    onEditReminder(context, domainId, title, msg, h, m, editingReminder!!.id)
                } else {
                    onAddReminder(context, domainId, title, msg, h, m)
                }
                showReminderDialog = false
                editingReminder = null
            }
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(isFrench = isFrench, onDismiss = { showPrivacyDialog = false })
    }

    if (showTermsDialog) {
        TermsAndConditionsDialog(isFrench = isFrench, onDismiss = { showTermsDialog = false })
    }

    if (showSupportDialog) {
        SupportFeedbackDialog(isFrench = isFrench, onDismiss = { showSupportDialog = false })
    }
}

@Composable
fun EditProfileDialog(
    user: UserEntity?,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, assembly: String, maker: String, phone: String, convDate: String) -> Unit
) {
    var name by remember { mutableStateOf(user?.fullName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var assembly by remember { mutableStateOf(user?.localAssembly ?: "") }
    var maker by remember { mutableStateOf(user?.discipleMaker ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var convDate by remember { mutableStateOf(user?.conversionDate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editProfile, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.fullNameLabel) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(strings.email) },
                    placeholder = { Text("email@example.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_email_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = assembly,
                    onValueChange = { assembly = it },
                    label = { Text(strings.localAssembly) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_assembly_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maker,
                    onValueChange = { maker = it },
                    label = { Text(strings.discipleMakerName) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_maker_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = convDate,
                    onValueChange = { convDate = it },
                    label = { Text(strings.conversionDate) },
                    placeholder = { Text("YYYY-MM-DD (e.g. 2021-04-15)") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_conversion_date_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(strings.phoneNumber) },
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, assembly, maker, phone, convDate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun AddOrEditReminderDialog(
    strings: AppStrings,
    initialReminder: ReminderEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (domainId: String, title: String, msg: String, hour: Int, minute: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialReminder?.title ?: "Daily DDEWG") }
    var msg by remember { mutableStateOf(initialReminder?.message ?: "Time for daily encounter with God!") }
    var hourText by remember { mutableStateOf(String.format("%02d", initialReminder?.hour ?: 6)) }
    var minText by remember { mutableStateOf(String.format("%02d", initialReminder?.minute ?: 0)) }

    val isEditing = initialReminder != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) strings.editReminderTitle else strings.addReminderTitle, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.reminderTitleLabel) },
                    modifier = Modifier.fillMaxWidth().testTag("reminder_title_input")
                )
                OutlinedTextField(
                    value = msg,
                    onValueChange = { msg = it },
                    label = { Text(strings.messageLabel) },
                    modifier = Modifier.fillMaxWidth().testTag("reminder_message_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it },
                        label = { Text(strings.hourLabel) },
                        modifier = Modifier.weight(1f).testTag("reminder_hour_input")
                    )
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it },
                        label = { Text(strings.minuteLabel) },
                        modifier = Modifier.weight(1f).testTag("reminder_min_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val h = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 6
                    val m = minText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    onConfirm(initialReminder?.domainId ?: "ddewg", title, msg, h, m)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandDarkNavy, contentColor = BrandBrightYellow),
                modifier = Modifier.testTag("save_reminder_button")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

private fun String?.isNull_Blank(): Boolean = this == null || this.isBlank()

