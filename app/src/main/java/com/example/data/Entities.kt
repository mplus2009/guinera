package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class UserSession(
    val userId: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String,
    val isAnonymous: Boolean,
    val uID: String = ""
)

// Firestore data models
data class Product(
    var id: String = "",
    var blockName: String = "",
    var creatorId: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var quantity: Int = 1,
    var description: String = "",
    var imageUrl: String = "",
    var ratings: Map<String, Float> = emptyMap(),
    var timestamp: Long = System.currentTimeMillis()
) {
    fun getAverageRating(): Float {
        return if (ratings.isEmpty()) 0f else ratings.values.average().toFloat()
    }
}

data class ChatMessage(
    var id: String = "",
    var blockName: String = "",
    var authorId: String = "",
    var author: String = "",
    var message: String = "",
    var replyToId: String? = null,
    var reactions: Map<String, String> = emptyMap(),
    var timestamp: Long = System.currentTimeMillis()
)

// Room database for Shopping Cart
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: String,
    val blockName: String,
    val name: String,
    val price: Double
)

@Entity(tableName = "local_product_config")
data class LocalProductConfig(
    @PrimaryKey val productId: String,
    val originalPrice: Double
)

@Entity(tableName = "seen_notifications")
data class SeenNotification(
    @PrimaryKey val notificationId: String
)

data class AppNotification(
    var id: String = "",
    var title: String = "",
    var message: String = "",
    var buttonText: String = "",
    var buttonUrl: String = "",
    var isActive: Boolean = true,
    var timestamp: Long = System.currentTimeMillis()
)

data class AppSurvey(
    var id: String = "",
    var question: String = "",
    var imageUrl: String = "",
    var options: List<String> = emptyList(),
    var results: Map<String, Int> = emptyMap(),
    var isActive: Boolean = true,
    var timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "p2p_contacts")
data class P2PContact(
    @PrimaryKey val uID: String,
    val displayName: String,
    val avatarUrl: String,
    val isCustom: Boolean = false
)

@Entity(tableName = "p2p_messages")
data class P2PMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderUid: String,
    val receiverUid: String,
    val message: String,
    val type: String, // "text", "audio", "photo", "video", "call"
    val mediaUri: String = "",
    val duration: Int = 0, // for call duration or audio message duration
    val timestamp: Long = System.currentTimeMillis()
)
