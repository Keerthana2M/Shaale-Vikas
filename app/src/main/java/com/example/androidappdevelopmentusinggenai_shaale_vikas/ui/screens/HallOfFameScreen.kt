package com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.Donor
import com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel.SchoolViewModel

@Composable
fun HallOfFameScreen(viewModel: SchoolViewModel) {
    val donors by viewModel.donors.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val myPledges by viewModel.currentUserPledges.collectAsState(initial = emptyList())

    // Group donors by name to calculate total contribution for leaderboard
    val rankedDonors = donors.groupBy { it.name }
        .map { (name, pledges) ->
            val total = pledges.sumOf { it.amountPledged }
            val latestPledge = pledges.first()
            latestPledge.copy(amountPledged = total)
        }
        .sortedByDescending { it.amountPledged }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Alumni Leaderboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Recognizing our most committed supporters", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
            }
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            itemsIndexed(rankedDonors) { index, donor ->
                DonorRankCard(donor, rank = index + 1, isMe = donor.name == currentUserName)
            }
        }
    }
}

@Composable
fun DonorRankCard(donor: Donor, rank: Int, isMe: Boolean) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMe) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "#$rank", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = rankColor, modifier = Modifier.width(45.dp))
            
            Box(
                modifier = Modifier.size(45.dp).clip(CircleShape).background(rankColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(donor.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = rankColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = if (isMe) "${donor.name} (You)" else donor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = donor.batch, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Text(text = "₹${donor.amountPledged.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
