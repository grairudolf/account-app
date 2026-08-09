package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryBlue

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.PrivacyTip,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Last updated: August 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Welcome to CMFI Spiritual Accountability. Your privacy and spiritual data confidentiality are paramount to us.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "1. Information We Collect\n" +
                            "• Personal Details: Name, email address, local assembly, and disciple maker contact info provided by you.\n" +
                            "• Spiritual Accountability Records: Entries logged across Bible reading, prayer time, DDEWG devotions, soul winning, and giving.\n" +
                            "• Conversion Date & Schedule Preferences: Conversion date and selected accountability reporting days.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "2. How Your Data Is Used\n" +
                            "• To track personal spiritual growth and streaks.\n" +
                            "• To generate periodic accountability reports for your disciple maker.\n" +
                            "• All data remains stored locally on your device unless you choose to export or share your accountability summary.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "3. Confidentiality & Security\n" +
                            "Your entries are kept private. We do not sell or monetize your personal or spiritual accountability data.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("I Understand")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun TermsAndConditionsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Gavel,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Effective Date: August 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "By using CMFI Spiritual Accountability, you agree to the following terms:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "1. Purpose of Application\n" +
                            "This app is designed as a tool for Christian discipleship, spiritual growth, personal accountability, and tracking daily devotions in accordance with Christian Missionary Fellowship International (CMFI) guidelines.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "2. User Responsibility\n" +
                            "You are responsible for the accuracy of the accountability entries logged. Honesty and integrity are essential components of Christian discipleship.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "3. Reporting & Sharing\n" +
                            "Sharing accountability reports with your disciple maker or mentor is voluntary and done at your discretion.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Accept & Continue")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
