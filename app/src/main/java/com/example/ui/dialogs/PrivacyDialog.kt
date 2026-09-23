package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyDialog(
    isBengali: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "প্রাইভেসি ও লোকাল ডাটাবেস" else "Privacy & Local Database",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isBengali)
                        "এই অ্যাপটি সম্পূর্ণ অফলাইন ও প্রাইভেসি-ফোকাসড ওপেন সোর্স আর্কিটেকচারে তৈরি।"
                    else
                        "This app is built with a 100% offline, privacy-first open-source architecture.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                PrivacyItem(
                    icon = Icons.Default.Lock,
                    title = if (isBengali) "১০০% লোকাল স্টোরেজ" else "100% Local Storage",
                    desc = if (isBengali)
                        "সব অ্যালার্ম, রিমাইন্ডার এবং নোট সরাসরি আপনার ফোনের নিজস্ব Room SQLite ডাটাবেসে সংরক্ষিত থাকে।"
                    else
                        "All alarms, reminders, and notes stay entirely inside your phone's Room SQLite database."
                )

                PrivacyItem(
                    icon = Icons.Default.Storage,
                    title = if (isBengali) "কোনো ক্লাউড বা ইন্টারনেট নয়" else "Zero Cloud Tracking",
                    desc = if (isBengali)
                        "কোনো রিমোট সার্ভার বা থার্ড পার্টি ট্র্যাকার ব্যবহার করা হয়নি। ডেটা সম্পূর্ণ নিরাপদ।"
                    else
                        "No tracking, no external servers, and no analytics."
                )

                PrivacyItem(
                    icon = Icons.Default.Code,
                    title = if (isBengali) "ওপেন সোর্স ফ্রেন্ডলি" else "Open Source Ready",
                    desc = if (isBengali)
                        "ক্লিন আর্কিটেকচার ও স্ট্যান্ডার্ড অ্যান্ড্রয়েড লাইব্রেরি দিয়ে তৈরি যা ওপেন সোর্স হিসেবে প্রকাশের জন্য প্রস্তুত।"
                    else
                        "Built with clean architecture, ready for open-source publishing."
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(if (isBengali) "ঠিক আছে" else "Got It")
            }
        }
    )
}

@Composable
private fun PrivacyItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
    }
}
