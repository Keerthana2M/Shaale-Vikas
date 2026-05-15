package com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.Donor
import com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel.SchoolViewModel
import com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel.UserRole

@Composable
fun ProfileScreen(
    viewModel: SchoolViewModel,
    onLogout: () -> Unit
) {
    val currentUserName by viewModel.currentUserName.collectAsState()
    val currentUserBatch by viewModel.currentUserBatch.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val myPledges by viewModel.currentUserPledges.collectAsState(initial = emptyList())
    val totalImpact = myPledges.sumOf { it.amountPledged }

    val impactLevel = when {
        totalImpact >= 5000 -> "School Guardian"
        totalImpact >= 2000 -> "Active Supporter"
        totalImpact > 0 -> "Rising Contributor"
        else -> "Alumni Member"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Profile Header
        val headerBg = if (userRole == UserRole.HEADMASTER) 
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(top = 48.dp, bottom = 32.dp, start = 32.dp, end = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (userRole == UserRole.HEADMASTER) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (userRole == UserRole.HEADMASTER) Icons.Default.School else Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentUserName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (totalImpact > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    text = if (userRole == UserRole.HEADMASTER) "School Administrator" else "Batch of $currentUserBatch • $impactLevel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout Session", fontSize = 14.sp)
                }
            }
        }

        // Impact Summary (Only for Alumni)
        if (userRole == UserRole.ALUMNI) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImpactStatCard(
                    label = "Total Pledged",
                    value = "₹${totalImpact.toInt()}",
                    modifier = Modifier.weight(1f)
                )
                ImpactStatCard(
                    label = "Commitments",
                    value = myPledges.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        Text(
            text = if (userRole == UserRole.HEADMASTER) "Management Activity" else "Your Contribution History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        if (userRole == UserRole.ALUMNI) {
            if (myPledges.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No contributions yet. Start building your legacy!",
                        color = Color.Gray,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myPledges) { pledge ->
                        MyPledgeCard(pledge)
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("As Headmaster, you can manage school needs and view community contributions in the 'Headmaster' tab.")
            }
        }
    }
}

@Composable
fun ImpactStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MyPledgeCard(pledge: Donor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val pledgeText = if (pledge.itemsPledged.isNotBlank()) pledge.itemsPledged else "₹${pledge.amountPledged.toInt()}"
                Text(text = "Pledged $pledgeText", fontWeight = FontWeight.Bold)
                Text(
                    text = "Legacy Commitment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "VERIFIED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
