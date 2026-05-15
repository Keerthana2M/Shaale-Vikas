package com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.NeedCategory
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.SchoolNeed
import com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel.SchoolViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedsDashboardScreen(
    viewModel: SchoolViewModel,
    onNeedClick: (String) -> Unit
) {
    val needs by viewModel.filteredNeeds.collectAsState(initial = emptyList())
    val currentUserBatch by viewModel.currentUserBatch.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    
    var selectedNeed by remember { mutableStateOf<SchoolNeed?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<NeedCategory?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }
    
    val totalRaised = needs.sumOf { it.fundsCollected }
    val totalGoal = needs.sumOf { it.costEstimate }

    Column(modifier = Modifier.fillMaxSize()) {
        SummaryHeader(totalRaised, totalGoal)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.setSearchQuery(it)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search repairs or items...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Quick Tips", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }
                items(NeedCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { 
                            Text(category.name.lowercase().replaceFirstChar { 
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
                            }) 
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (currentUserName != "Guest Alumnus" && searchQuery.isEmpty() && selectedCategory == null) {
                    item {
                        WelcomeCard(currentUserName)
                    }
                }

                val displayNeeds = needs.filter { 
                    !it.isCompleted && (selectedCategory == null || it.category == selectedCategory)
                }

                if (displayNeeds.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null) {
                    item {
                        FeaturedNeedSection(displayNeeds.first(), onNeedClick)
                    }
                    item {
                        Text(
                            text = "Current Priority List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(displayNeeds.drop(1)) { need ->
                        NeedCard(
                            need = need, 
                            onPledgeClick = { selectedNeed = need },
                            onClick = { onNeedClick(need.id) }
                        )
                    }
                } else if (displayNeeds.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isEmpty() && selectedCategory == null) "All needs fulfilled! Check the Impact tab."
                                else "No matching needs found.",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(displayNeeds) { need ->
                        NeedCard(
                            need = need, 
                            onPledgeClick = { selectedNeed = need },
                            onClick = { onNeedClick(need.id) }
                        )
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    selectedNeed?.let { need ->
        PledgeDialog(
            need = need,
            defaultName = currentUserName,
            onDismiss = { selectedNeed = null },
            onConfirm = { name, amount, items ->
                viewModel.pledge(need.id, name, currentUserBatch, amount, items)
                selectedNeed = null
            }
        )
    }
}

@Composable
fun WelcomeCard(name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Welcome back, $name!", fontWeight = FontWeight.Bold)
                Text("Your support transforms lives.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("How to Support") },
        text = {
            Column {
                Text("• Browse micro-needs listed by the school.")
                Text("• 'Pledge' to commit an item or funds (simulated).")
                Text("• Join 'Chat' to ask questions or coordinate.")
                Text("• View visual proof in the 'Impact' tab.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Admin Credentials: admin / shaale123", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        }
    )
}

@Composable
fun FeaturedNeedSection(need: SchoolNeed, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(need.id) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = need.urgency.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = need.urgency.color
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(need.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(need.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { need.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            AsyncImage(
                model = need.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun SummaryHeader(raised: Double, goal: Double) {
    val progress = if (goal > 0) (raised / goal).toFloat().coerceIn(0f, 1f) else 0f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Community Impact Score",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", raised)}",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " raised",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = Color(0xFFFFEB3B),
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Help us reach our sem-goal: ₹${String.format(Locale.getDefault(), "%,.0f", goal)}",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun NeedCard(
    need: SchoolNeed, 
    onPledgeClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = need.imageUrl,
                    contentDescription = need.title,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = need.urgency.color.copy(alpha = 0.9f),
                    contentColor = Color.White
                ) {
                    Text(
                        text = need.urgency.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = need.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = need.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = if (need.targetItems > 0) "${need.itemsPledgedCount}/${need.targetItems} items pledged" else "₹${need.fundsCollected.toInt()} pledged",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (need.targetItems > 0) "Goal: ${need.targetItems} ${need.quantity}" else "Target: ₹${need.costEstimate.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(need.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { need.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onPledgeClick() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (need.targetItems > 0) "Pledge Items" else "Pledge Support")
                }
            }
        }
    }
}

@Composable
fun PledgeDialog(
    need: SchoolNeed,
    defaultName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(if (defaultName != "Guest Alumnus") defaultName else "") }
    var amount by remember { mutableStateOf(if (need.targetItems > 0) "" else "500") }
    var itemsPledged by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp)) },
        title = { Text("Old Student Pledge", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Commitment for '${need.title}'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pledging As (Name)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                if (need.targetItems > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = itemsPledged,
                        onValueChange = { itemsPledged = it },
                        label = { Text("Quantity/Items donating") },
                        placeholder = { Text("e.g. 2 monitors") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This pledge is a serious commitment. Alumni from your batch will see this to coordinate.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && (amountVal > 0 || itemsPledged.isNotBlank())) {
                        onConfirm(name, amountVal, itemsPledged)
                    }
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirm Pledge")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
