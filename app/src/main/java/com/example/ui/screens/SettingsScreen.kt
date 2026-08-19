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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun SettingsScreen(
    strings: AppStrings,
    user: UserEntity?,
    currentLanguage: AppLanguage,
    currentTheme: ThemeMode,
    reminders: List<ReminderEntity>,
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
                        // Email Row
                        if (!user?.email.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(strings.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = user!!.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                        }

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
                                    .background(LightBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
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
                                    .background(if (isDark) PrimaryBlueDark else LightBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDark) StreakGold else PrimaryBlue,
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
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = strings.dailyReminders,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = {
                        editingReminder = null
                        showReminderDialog = true
                    },
                    modifier = Modifier.testTag("add_reminder_button")
                ) {
                    Icon(Icons.Default.AddAlarm, contentDescription = "Add Reminder", tint = PrimaryBlue)
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier
                        .widthIn(max = 840.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = strings.noActiveReminders,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(reminders, key = { it.id }) { rem ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (rem.isEnabled) PrimaryBlue.copy(alpha = 0.3f) else DividerColor),
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (rem.isEnabled) PrimaryBlue else DividerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
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
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ListItem(
                        headlineContent = { Text(strings.privacyPolicy, fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = PrimaryBlue) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .clickable { showPrivacyDialog = true }
                            .testTag("settings_privacy_policy")
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                    ListItem(
                        headlineContent = { Text(strings.termsConditions, fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.Gavel, contentDescription = null, tint = PrimaryBlue) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .clickable { showTermsDialog = true }
                            .testTag("settings_terms_conditions")
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                    ListItem(
                        headlineContent = { Text(strings.supportFeedback, fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.HelpCenter, contentDescription = null, tint = PrimaryBlue) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .clickable { showSupportDialog = true }
                            .testTag("settings_support_feedback")
                    )
                }
            }
        }

        // Need Help? Support CTA Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = LightBlueContainer,
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
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
                        color = PrimaryBlueDark
                    )
                    Text(
                        text = strings.needHelpDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { showSupportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueDark),
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

        // Account Status & Sign Out
        item {
            Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth()) {
                if (user?.isGuest == true) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.OfflinePin, contentDescription = null, tint = PrimaryBlue)
                                Column {
                                    Text("Guest Mode Active", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    Text("Your entries are saved locally on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Button(
                                onClick = onSignOut,
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sign In / Create Account", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sign_out_button"),
                        shape = RoundedCornerShape(20.dp)
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(strings.email) })
                OutlinedTextField(value = assembly, onValueChange = { assembly = it }, label = { Text(strings.localAssembly) })
                OutlinedTextField(value = maker, onValueChange = { maker = it }, label = { Text(strings.discipleMakerName) })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(strings.phoneNumber) })
                OutlinedTextField(
                    value = convDate,
                    onValueChange = { convDate = it },
                    label = { Text(strings.conversionDate) },
                    placeholder = { Text("2021-04-15") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, email, assembly, maker, phone, convDate) }) {
                Text(strings.save)
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

