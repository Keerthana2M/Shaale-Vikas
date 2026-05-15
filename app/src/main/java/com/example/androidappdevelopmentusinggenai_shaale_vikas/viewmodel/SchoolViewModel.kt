package com.example.androidappdevelopmentusinggenai_shaale_vikas.viewmodel

import androidx.lifecycle.ViewModel
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.Donor
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.Message
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.NeedCategory
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.SchoolNeed
import com.example.androidappdevelopmentusinggenai_shaale_vikas.model.UrgencyLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.util.UUID

enum class UserRole { ALUMNI, HEADMASTER }

data class Notification(val id: String, val message: String, val time: Long = System.currentTimeMillis())

class SchoolViewModel : ViewModel() {
    private val _userRole = MutableStateFlow(UserRole.ALUMNI)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    private val _currentUserName = MutableStateFlow("Guest Alumnus")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserBatch = MutableStateFlow("N/A")
    val currentUserBatch: StateFlow<String> = _currentUserBatch.asStateFlow()

    private val _needs = MutableStateFlow<List<SchoolNeed>>(emptyList())
    val needs: StateFlow<List<SchoolNeed>> = _needs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val filteredNeeds = combine(_needs, _searchQuery) { needs, query ->
        if (query.isBlank()) needs
        else needs.filter { 
            it.title.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true) ||
            it.category.name.contains(query, ignoreCase = true)
        }
    }

    private val _donors = MutableStateFlow<List<Donor>>(emptyList())
    val donors: StateFlow<List<Donor>> = _donors.asStateFlow()

    val currentUserPledges = combine(_donors, _currentUserName) { donors, name ->
        donors.filter { it.name.equals(name, ignoreCase = true) }
    }

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val roofId = UUID.randomUUID().toString()
        val computerId = UUID.randomUUID().toString()
        val waterId = UUID.randomUUID().toString()

        _needs.value = listOf(
            SchoolNeed(
                id = roofId,
                title = "Roof Repair - Block A",
                description = "Monsoon leaks are damaging the library books. Requires immediate patching and waterproofing.",
                category = NeedCategory.INFRASTRUCTURE,
                urgency = UrgencyLevel.HIGH,
                costEstimate = 8500.0,
                fundsCollected = 4000.0,
                imageUrl = "https://images.unsplash.com/photo-1632759162353-066551b9264c?q=80&w=800",
                donorCount = 4
            ),
            SchoolNeed(
                id = computerId,
                title = "Computer Lab Setup",
                description = "We need 5 refurbished monitors and keyboards for the new IT lab to start basic coding classes.",
                category = NeedCategory.EDUCATION_MATERIAL,
                urgency = UrgencyLevel.MEDIUM,
                targetItems = 5,
                itemsPledgedCount = 2,
                quantity = "sets",
                imageUrl = "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=800",
                donorCount = 2
            ),
            SchoolNeed(
                id = waterId,
                title = "Drinking Water Filter",
                description = "Installation of a new RO water purifier for students to ensure clean drinking water.",
                category = NeedCategory.SANITATION,
                urgency = UrgencyLevel.MEDIUM,
                costEstimate = 3000.0,
                fundsCollected = 3000.0,
                imageUrl = "https://images.unsplash.com/photo-1581244277943-fe4a9c777189?q=80&w=800",
                isCompleted = true,
                beforeImageUrl = "https://images.unsplash.com/photo-1518398046578-8cca57782e17?q=80&w=400",
                afterImageUrl = "https://images.unsplash.com/photo-1517646288024-aa252309195d?q=80&w=400",
                donorCount = 5
            )
        )

        _donors.value = listOf(
            Donor(UUID.randomUUID().toString(), "Vikram Hegde", "Batch 1995", 2000.0, "", "Proud to be an alumnus!", roofId),
            Donor(UUID.randomUUID().toString(), "Sneha Patil", "Batch 2008", 1200.0, "", "For the little ones.", roofId),
            Donor(UUID.randomUUID().toString(), "Kiran Kumar", "Batch 2012", 0.0, "1 Monitor", "Happy to support education.", computerId),
            Donor(UUID.randomUUID().toString(), "Amit V", "Batch 2000", 3000.0, "", "Clean water is a right.", waterId)
        )

        _notifications.value = listOf(
            Notification(UUID.randomUUID().toString(), "Vikram Hegde pledged ₹2000 for Roof Repair"),
            Notification(UUID.randomUUID().toString(), "Goal Achieved: Drinking Water Filter is 100% funded!")
        )

        _messages.value = listOf(
            Message(UUID.randomUUID().toString(), "Vikram", "Is the roof repair starting next week?", roofId),
            Message(UUID.randomUUID().toString(), "Headmaster", "Yes, we are just waiting for the funds for materials.")
        )
    }

    fun switchRole(role: UserRole) {
        _userRole.value = role
    }

    fun login(name: String, batch: String) {
        _currentUserName.value = name
        _currentUserBatch.value = batch
        addNotification("Welcome back, $name!")
    }

    fun logout() {
        _userRole.value = UserRole.ALUMNI
        _currentUserName.value = "Guest Alumnus"
        _currentUserBatch.value = "N/A"
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun addNeed(need: SchoolNeed) {
        _needs.value = listOf(need) + _needs.value
        addNotification("New Need Posted: ${need.title}")
    }

    fun deleteNeed(needId: String) {
        _needs.value = _needs.value.filter { it.id != needId }
    }

    fun pledge(needId: String, donorName: String, batch: String, amount: Double, items: String = "") {
        val needTitle = _needs.value.find { it.id == needId }?.title ?: "a project"
        _needs.value = _needs.value.map {
            if (it.id == needId) {
                if (it.targetItems > 0 && items.isNotBlank()) {
                    it.copy(itemsPledgedCount = it.itemsPledgedCount + 1, donorCount = it.donorCount + 1)
                } else {
                    it.copy(fundsCollected = it.fundsCollected + amount, donorCount = it.donorCount + 1)
                }
            } else it
        }
        
        _donors.value = listOf(
            Donor(
                id = UUID.randomUUID().toString(), 
                name = donorName, 
                batch = batch,
                amountPledged = amount, 
                itemsPledged = items,
                message = if (items.isNotBlank()) "Pledged $items" else "Supported with ₹${amount.toInt()}", 
                needId = needId
            )
        ) + _donors.value
        
        val msg = if (items.isNotBlank()) "$donorName pledged $items for $needTitle"
                  else "$donorName pledged ₹${amount.toInt()} for $needTitle"
        addNotification(msg)
    }

    fun sendMessage(text: String, needId: String? = null) {
        if (text.isBlank()) return
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            senderName = _currentUserName.value,
            text = text,
            needId = needId
        )
        _messages.value = _messages.value + newMessage
    }

    fun markAsCompleted(needId: String, beforeUrl: String, afterUrl: String) {
        val needTitle = _needs.value.find { it.id == needId }?.title ?: ""
        _needs.value = _needs.value.map {
            if (it.id == needId) it.copy(isCompleted = true, beforeImageUrl = beforeUrl, afterImageUrl = afterUrl, fundsCollected = it.costEstimate)
            else it
        }
        addNotification("Impact Update: $needTitle has been completed!")
    }
    
    private fun addNotification(message: String) {
        _notifications.value = listOf(Notification(UUID.randomUUID().toString(), message)) + _notifications.value.take(19)
    }

    fun getDonorsForNeed(needId: String): List<Donor> = _donors.value.filter { it.needId == needId }
}
