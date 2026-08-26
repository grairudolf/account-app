package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpCenter
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
    isFrench: Boolean = false,
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
                text = if (isFrench) "Politique de Confidentialité" else "Privacy Policy",
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
                    text = if (isFrench) "Dernière mise à jour : Août 2026" else "Last updated: August 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isFrench)
                        "Bienvenue sur CMFI Accap. Votre confidentialité et la sécurité de vos données spirituelles sont primordiales pour nous."
                    else
                        "Welcome to CMFI Accap. Your privacy and spiritual data confidentiality are paramount to us.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isFrench)
                        "1. Informations collectées\n" +
                                "• Données personnelles : Nom, adresse e-mail, assemblée locale et contact du faiseur de disciples fournis par vous-même.\n" +
                                "• Enregistrements de redevabilité spirituelle : Entrées enregistrées pour la lecture de la Bible, le temps de prière, les dévotions DDEWG, l'évangélisation et les offrandes.\n" +
                                "• Date de conversion et préférences de calendrier."
                    else
                        "1. Information We Collect\n" +
                                "• Personal Details: Name, email address, local assembly, and disciple maker contact info provided by you.\n" +
                                "• Spiritual Accountability Records: Entries logged across Bible reading, prayer time, DDEWG devotions, soul winning, and giving.\n" +
                                "• Conversion Date & Schedule Preferences: Conversion date and selected accountability reporting days.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (isFrench)
                        "2. Utilisation de vos données\n" +
                                "• Pour suivre la croissance spirituelle personnelle et la continuité.\n" +
                                "• Pour générer des rapports périodiques de redevabilité pour votre faiseur de disciples.\n" +
                                "• Toutes les données restent stockées localement sur votre appareil à moins que vous ne choisissiez d'exporter ou de partager votre résumé."
                    else
                        "2. How Your Data Is Used\n" +
                                "• To track personal spiritual growth and streaks.\n" +
                                "• To generate periodic accountability reports for your disciple maker.\n" +
                                "• All data remains stored locally on your device unless you choose to export or share your accountability summary.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (isFrench)
                        "3. Confidentialité et Sécurité\n" +
                                "Vos entrées sont strictement privées. Nous ne vendons ni ne monétisons vos données personnelles ou spirituelles."
                    else
                        "3. Confidentiality & Security\n" +
                                "Your entries are kept private. We do not sell or monetize your personal or spiritual accountability data.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (isFrench) "J'ai compris" else "I Understand")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun TermsAndConditionsDialog(
    isFrench: Boolean = false,
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
                text = if (isFrench) "Conditions Générales d'Utilisation" else "Terms & Conditions",
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
                    text = if (isFrench) "Date d'entrée en vigueur : Août 2026" else "Effective Date: August 2026",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isFrench)
                        "En utilisant CMFI Accap, vous acceptez les conditions suivantes :"
                    else
                        "By using CMFI Accap, you agree to the following terms:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isFrench)
                        "1. Objectif de l'application\n" +
                                "Cette application est conçue comme un outil pour le discipulat chrétien, la croissance spirituelle, la redevabilité personnelle et le suivi des dévotions quotidiennes conformément aux directives de la Communauté Missionnaire Chrétienne Internationale (CMFI)."
                    else
                        "1. Purpose of Application\n" +
                                "This app is designed as a tool for Christian discipleship, spiritual growth, personal accountability, and tracking daily devotions in accordance with Christian Missionary Fellowship International (CMFI) guidelines.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (isFrench)
                        "2. Responsabilité de l'utilisateur\n" +
                                "Vous êtes responsable de l'exactitude des entrées enregistrées. L'honnêteté et l'intégrité sont des éléments essentiels du discipulat chrétien."
                    else
                        "2. User Responsibility\n" +
                                "You are responsible for the accuracy of the accountability entries logged. Honesty and integrity are essential components of Christian discipleship.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (isFrench)
                        "3. Rapports et Partage\n" +
                                "Le partage des rapports de redevabilité avec votre faiseur de disciples ou mentor est volontaire et se fait à votre seule discrétion."
                    else
                        "3. Reporting & Sharing\n" +
                                "Sharing accountability reports with your disciple maker or mentor is voluntary and done at your discretion.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (isFrench) "Accepter et continuer" else "Accept & Continue")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun SupportFeedbackDialog(
    isFrench: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.HelpCenter,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (isFrench) "Support & Commentaires" else "Support & Feedback",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isFrench)
                        "Avez-vous besoin d'aide ou souhaitez-vous suggérer des améliorations pour CMFI Accap ?"
                    else
                        "Need assistance or have suggestions for CMFI Accap?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isFrench) "Email Officiel de Support :" else "Official Support Email:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "support.cmfiaccap@gmail.com",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("support.cmfiaccap@gmail.com"))
                                    android.widget.Toast.makeText(context, if (isFrench) "Email copié !" else "Email copied!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Email",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:support.cmfiaccap@gmail.com")
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "[CMFI Accap] Support & Feedback")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "support.cmfiaccap@gmail.com", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isFrench) "Envoyer un Email" else "Send Email")
                }

                Text(
                    text = if (isFrench)
                        "• Communion CMFI : Contactez votre assemblée locale ou votre faiseur de disciples pour un accompagnement spirituel direct.\n" +
                                "• Vos commentaires sont précieux pour nous aider à améliorer cet outil de discipulat."
                    else
                        "• CMFI Fellowship: Contact your local assembly or disciple maker for direct spiritual guidance.\n" +
                                "• Your feedback helps us continuously improve this discipleship tool.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isFrench) "Fermer" else "Close")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
