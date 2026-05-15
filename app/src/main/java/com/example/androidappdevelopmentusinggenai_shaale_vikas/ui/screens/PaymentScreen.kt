package com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PaymentScreen(
    amount: Double,
    needTitle: String,
    onPaymentSuccess: () -> Unit
) {
    var step by remember { mutableStateOf(0) } // 0: Select, 1: Processing, 2: Success

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (step) {
            0 -> {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Confirm Your Pledge", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "You are committing ₹${amount.toInt()} for:",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = needTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text("Select Simulated Payment Method", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { step = 1 },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pay via UPI / Card (Demo)")
                }
                
                TextButton(onClick = { /* Handle cancel */ }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Cancel Transaction", color = Color.Gray)
                }
            }
            1 -> {
                LaunchedEffect(Unit) {
                    delay(2000)
                    step = 2
                    delay(1500)
                    onPaymentSuccess()
                }
                CircularProgressIndicator(modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Securing your connection...", style = MaterialTheme.typography.bodyLarge)
                Text("Processing payment simulation", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            2 -> {
                val scale by animateFloatAsState(
                    targetValue = 1.2f,
                    animationSpec = repeatable(
                        iterations = 1,
                        animation = tween(durationMillis = 300)
                    ), label = "success_scale"
                )
                
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).scale(scale),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Pledge Confirmed!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    "Building a better school together.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
