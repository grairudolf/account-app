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
        item {
            Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = strings.settings,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Disciple Profile & Spiritual Age Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .testTag("settings_profile_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(LightBlueContainer)
                                    .clickable { photoPickerLauncher.launch("image/*") }
                                    .testTag("profile_image_picker"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!user?.profileImageUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = user?.profileImageUri,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlue,
                                    modifier = Modifier.align(Alignment.BottomEnd).size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = Color.White,
                                        modifier = Modifier.padding(3.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = user?.fullName?.ifBlank { strings.discipleProfile } ?: strings.discipleProfile,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${strings.localAssembly}: ${user?.localAssembly?.ifBlank { strings.notSet } ?: strings.notSet}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${strings.discipleMakerName}: ${user?.discipleMaker?.ifBlank { strings.notSet } ?: strings.notSet}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("edit_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = PrimaryBlue)
                        }
                    }

                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))

                    // Conversion Date / Spiritual Age
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = StreakGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = strings.spiritualJourney,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = spiritualAgeText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = { showProfileDialog = true }) {
                            Text(strings.setDate, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Language Selector Dropdown Card
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_language_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PastelLavenderContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = DarkPillBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.language,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = currentLanguage.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Dropdown Container
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, DividerColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { languageDropdownExpanded = true }
                                .testTag("language_dropdown_trigger")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.FRENCH) "🇫🇷" else "🇬🇧",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = currentLanguage.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = if (languageDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Language",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = languageDropdownExpanded,
                            onDismissRequest = { languageDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                val isSelected = currentLanguage == lang
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(if (lang == AppLanguage.FRENCH) "🇫🇷" else "🇬🇧")
                                            Text(
                                                text = lang.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) BrandDarkNavy else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = BrandDarkNavy,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onUpdateLanguage(lang)
                                        languageDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("lang_menu_item_${lang.code}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Theme Toggle Switch Card
        item {
            val isDark = currentTheme == ThemeMode.DARK
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_theme_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isDark) BrandDarkNavy else BrandBrightYellow.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isDark) BrandBrightYellow else BrandDarkNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Dark Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDark) "Dark background active" else "Light background active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isDark,
                        onCheckedChange = { checked ->
                            onUpdateTheme(if (checked) ThemeMode.DARK else ThemeMode.LIGHT)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrandBrightYellow,
                            checkedTrackColor = BrandDarkNavy,
                            uncheckedThumbColor = BrandDarkNavy,
                            uncheckedTrackColor = DividerColor
                        ),
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Reminders Section with full CRUD
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        tint = BrandDarkNavy,
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
                    Icon(Icons.Default.AddAlarm, contentDescription = "Add Reminder", tint = BrandDarkNavy)
                }
            }
        }

        if (reminders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.noActiveReminders,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(reminders, key = { it.id }) { rem ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (rem.isEnabled) BrandDarkNavy.copy(alpha = 0.3f) else DividerColor),
                    modifier = Modifier
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
                                    .background(if (rem.isEnabled) BrandDarkNavy else DividerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = if (rem.isEnabled) BrandBrightYellow else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BrandBrightYellow,
                                    checkedTrackColor = BrandDarkNavy
                                ),
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

        // Legal & Support Options
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    ListItem(
                        headlineContent = { Text(strings.privacyPolicy, fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.clickable { showPrivacyDialog = true }.testTag("settings_privacy_policy")
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                    ListItem(
                        headlineContent = { Text(strings.termsConditions, fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.Gavel, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.clickable { showTermsDialog = true }.testTag("settings_terms_conditions")
                    )
                    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f))
                    ListItem(
                        headlineContent = { Text(strings.supportFeedback, fontWeight = FontWeight.SemiBold) },
                        leadingContent = { Icon(Icons.Default.HelpCenter, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.clickable { showSupportDialog = true }.testTag("settings_support_feedback")
                    )
                }
            }
        }

        // Account Status & Sign Out
        item {
            Spacer(modifier = Modifier.height(12.dp))
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

