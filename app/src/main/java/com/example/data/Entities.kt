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
    var currency: String = "CUP",
    var prices: Map<String, Double> = emptyMap(),
    var quantity: Int = 1,
    var description: String = "",
    var imageUrl: String = "",
    var ratings: Map<String, Float> = emptyMap(),
    var logoUri: String = "",
    var bannerUri: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
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
    var logoUri: String = "",
    var bannerUri: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
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
    var logoUri: String = "",
    var bannerUri: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
)

data class AppSurvey(
    var id: String = "",
    var question: String = "",
    var imageUrl: String = "",
    var options: List<String> = emptyList(),
    var results: Map<String, Int> = emptyMap(),
    var isActive: Boolean = true,
    var logoUri: String = "",
    var bannerUri: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
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
    val timestamp: Long = System.currentTimeMillis(),
    val isPending: Boolean = false
)

data class BusinessSpace(
    var id: String = "",
    var ownerId: String = "",
    var brandName: String = "",
    var description: String = "",
    var phoneNumber: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var logoUri: String = "",
    var bannerUri: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
)

data class SpaceProduct(
    var id: String = "",
    var spaceId: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var currency: String = "CUP",
    var imageUrls: List<String> = emptyList(),
    var logoUri: String = "",
    var bannerUri: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
)

data class BusinessChat(
    var id: String = "",
    var spaceId: String = "",
    var clientId: String = "",
    var ownerId: String = "",
    var spaceName: String = "",
    var clientName: String = "",
    var ownerName: String = "",
    var lastMessage: String = "",
    var lastMessageTime: Long = System.currentTimeMillis()
)

data class BusinessMessage(
    var id: String = "",
    var chatId: String = "",
    var senderId: String = "",
    var message: String = "",
    var imageUrl: String = "",
    var audioUrl: String = "",
    var attachedProductId: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var isPending: Boolean = false
)
