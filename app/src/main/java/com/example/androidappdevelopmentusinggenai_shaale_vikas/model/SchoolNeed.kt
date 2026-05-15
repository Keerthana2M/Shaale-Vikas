package com.example.androidappdevelopmentusinggenai_shaale_vikas.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class NeedCategory {
    INFRASTRUCTURE,
    SANITATION,
    EDUCATION_MATERIAL,
    SPORTS,
    OTHERS;

    val icon: ImageVector
        get() = when (this) {
            INFRASTRUCTURE -> Icons.Default.Build
            SANITATION -> Icons.Default.Face
            EDUCATION_MATERIAL -> Icons.Default.Edit
            SPORTS -> Icons.Default.PlayArrow
            OTHERS -> Icons.AutoMirrored.Filled.List
        }
}

enum class UrgencyLevel(val label: String, val color: Color) {
    HIGH("Urgent", Color(0xFFE53935)),
    MEDIUM("Priority", Color(0xFFFB8C00)),
    LOW("Ongoing", Color(0xFF43A047))
}

data class SchoolNeed(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: NeedCategory = NeedCategory.OTHERS,
    val urgency: UrgencyLevel = UrgencyLevel.MEDIUM,
    val costEstimate: Double = 0.0,
    val fundsCollected: Double = 0.0,
    val imageUrl: String = "",
    val beforeImageUrl: String? = null,
    val afterImageUrl: String? = null,
    val isCompleted: Boolean = false,
    val donorCount: Int = 0,
    val quantity: String = "",
    val itemsPledgedCount: Int = 0,
    val targetItems: Int = 0
) {
    val progress: Float
        get() = if (targetItems > 0) {
            (itemsPledgedCount.toFloat() / targetItems.toFloat()).coerceIn(0f, 1f)
        } else if (costEstimate > 0) {
            (fundsCollected / costEstimate).toFloat().coerceIn(0f, 1f)
        } else 0f
}

data class Donor(
    val id: String = "",
    val name: String = "",
    val batch: String = "Unknown",
    val amountPledged: Double = 0.0,
    val itemsPledged: String = "",
    val message: String = "",
    val needId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
