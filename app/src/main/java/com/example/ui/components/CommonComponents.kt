package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

fun formatDate(ms: Long): String {
  val sdf = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRENCH)
  return sdf.format(Date(ms))
}

fun formatShortDate(ms: Long, isEnglish: Boolean = false): String {
  val locale = if (isEnglish) Locale.US else Locale.FRENCH
  val pattern = if (isEnglish) "MM/dd/yyyy" else "dd/MM/yyyy"
  return SimpleDateFormat(pattern, locale).format(Date(ms))
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
  Column(modifier = Modifier.padding(vertical = 8.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.primary
    )
    if (subtitle != null) {
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
