package com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.NeedCategory
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.SchoolNeed
import com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel.SchoolViewModel
import java.util.Locale
import java.util.UUID

@Composable
fun AdminScreen(viewModel: SchoolViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedNeedToComplete by remember { mutableStateOf<SchoolNeed?>(null) }
    var needToDelete by remember { mutableStateOf<SchoolNeed?>(null) }
    
    val needs by viewModel.needs.collectAsState()
    val donors by viewModel.donors.collectAsState()
    
    val totalNeeds = needs.size
    val completedCount = needs.count { it.isCompleted }
    val totalFunds = needs.sumOf { it.fundsCollected }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Headmaster Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard("Total Needs", totalNeeds.toString(), Modifier.weight(1f))
                AdminStatCard("Completed", completedCount.toString(), Modifier.weight(1f))
                AdminStatCard("Collected", "₹${totalFunds.toInt()}", Modifier.weight(1.2f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Project Pipeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(needs) { need ->
                    AdminNeedCard(
                        need = need, 
                        onCompleteClick = { selectedNeedToComplete = need },
                        onDeleteClick = { needToDelete = need }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recent Alumni Commitments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                items(donors.take(10)) { donor ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${donor.name} (${donor.batch})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    if (donor.itemsPledged.isNotBlank()) "Donated: ${donor.itemsPledged}" 
                                    else "Pledged: ₹${donor.amountPledged.toInt()}", 
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        LargeFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post New Need")
        }
    }

    if (showAddDialog) {
        AddNeedDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newNeed ->
                viewModel.addNeed(newNeed)
                showAddDialog = false
            }
        )
    }

    selectedNeedToComplete?.let { need ->
        CompleteProjectDialog(
            need = need,
            onDismiss = { selectedNeedToComplete = null },
            onConfirm = { before, after ->
                viewModel.markAsCompleted(need.id, before, after)
                selectedNeedToComplete = null
            }
        )
    }

    if (needToDelete != null) {
        AlertDialog(
            onDismissRequest = { needToDelete = null },
            title = { Text("Delete Need") },
            text = { Text("Are you sure you want to delete '${needToDelete?.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        needToDelete?.let { viewModel.deleteNeed(it.id) }
                        needToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { needToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun AdminNeedCard(
    need: SchoolNeed, 
    onCompleteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (need.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = need.category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = need.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (need.targetItems > 0) {
                    Text(text = "Target: ${need.targetItems} ${need.quantity}", style = MaterialTheme.typography.labelSmall)
                }
                if (!need.isCompleted) {
                    LinearProgressIndicator(
                        progress = { need.progress },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
            
            if (need.isCompleted) {
                Icon(Icons.Default.Check, contentDescription = "Completed", tint = Color.Green)
            } else {
                Button(
                    onClick = onCompleteClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Finish", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNeedDialog(onDismiss: () -> Unit, onConfirm: (SchoolNeed) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isItemBased by remember { mutableStateOf(false) }
    var quantityType by remember { mutableStateOf("") }
    var targetCount by remember { mutableStateOf("") }
    var costEstimate by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NeedCategory.INFRASTRUCTURE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("List a School Need", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item/Repair Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    NeedCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { 
                                Text(
                                    category.name.lowercase().replaceFirstChar { char -> 
                                        if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString() 
                                    },
                                    fontSize = 10.sp
                                ) 
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isItemBased, onCheckedChange = { isItemBased = it })
                    Text("Is this for specific items? (e.g. 5 Desks)")
                }

                if (isItemBased) {
                    OutlinedTextField(
                        value = targetCount,
                        onValueChange = { targetCount = it },
                        label = { Text("Target Quantity (Number)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantityType,
                        onValueChange = { quantityType = it },
                        label = { Text("Unit (e.g. Sets, Units, Sq Ft)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = costEstimate,
                        onValueChange = { costEstimate = it },
                        label = { Text("Cost Estimate (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Initial Photo URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = costEstimate.toDoubleOrNull() ?: 0.0
                    val target = targetCount.toIntOrNull() ?: 0
                    if (title.isNotBlank()) {
                        onConfirm(
                            SchoolNeed(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                category = selectedCategory,
                                costEstimate = cost,
                                targetItems = target,
                                quantity = quantityType,
                                imageUrl = imageUrl
                            )
                        )
                    }
                }
            ) {
                Text("Post Need")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CompleteProjectDialog(
    need: SchoolNeed,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var afterUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Project Impact Proof") },
        text = {
            Column {
                Text("Post the 'After' photo for ${need.title} to show transparency to donors.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = afterUrl,
                    onValueChange = { afterUrl = it },
                    label = { Text("Completed Project Photo URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (afterUrl.isNotBlank()) {
                        onConfirm(need.imageUrl, afterUrl)
                    }
                }
            ) {
                Text("Finish & Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
