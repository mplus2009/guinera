package com.example.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AppRepository(private val appDao: AppDao) {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }

    suspend fun uploadImage(uri: Uri): String? {
        if (storage == null) return null
        return try {
            val ref = storage.reference.child("images/${java.util.UUID.randomUUID()}")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    fun getProducts(blockName: String): Flow<List<Product>> = callbackFlow {
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("products")
            .whereEqualTo("blockName", blockName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    fun addProduct(product: Product) {
        if (firestore == null) return
        val docRef = firestore.collection("products").document()
        product.id = docRef.id
        docRef.set(product)
    }

    fun deleteProduct(productId: String) {
        firestore?.collection("products")?.document(productId)?.delete()
    }

    fun rateProduct(productId: String, userId: String, rating: Float) {
        firestore?.collection("products")?.document(productId)
            ?.update("ratings.$userId", rating)
    }

    fun getChats(blockName: String): Flow<List<ChatMessage>> = callbackFlow {
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("chats")
            .whereEqualTo("blockName", blockName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.apply { id = doc.id }
                }?.sortedBy { it.timestamp } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    fun addChatMessage(chat: ChatMessage) {
        if (firestore == null) return
        val docRef = firestore.collection("chats").document()
        chat.id = docRef.id
        docRef.set(chat)
    }
    
    fun reactToChat(chatId: String, userId: String, emoji: String) {
        firestore?.collection("chats")?.document(chatId)
            ?.update("reactions.$userId", emoji)
    }
    
    fun removeReaction(chatId: String, userId: String) {
        // Can't use FieldValue.delete() easily with dot notation in a simple map update without building a map.
        // Let's use FieldValue.delete()
        firestore?.collection("chats")?.document(chatId)
            ?.update("reactions.$userId", com.google.firebase.firestore.FieldValue.delete())
    }

    fun getCartItems(): Flow<List<CartItem>> = appDao.getCartItems()
    suspend fun insertCartItem(cartItem: CartItem) = appDao.insertCartItem(cartItem)
    suspend fun deleteCartItem(cartItem: CartItem) = appDao.deleteCartItem(cartItem)
    
    fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.apply { id = doc.id }
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    fun updateProductQuantity(productId: String, quantity: Int) {
        firestore?.collection("products")?.document(productId)?.update("quantity", quantity)
    }

    fun addNotification(notification: AppNotification) {
        if (firestore == null) return
        val docRef = firestore.collection("notifications").document()
        notification.id = docRef.id
        docRef.set(notification)
    }

    fun updateNotification(notificationId: String, updates: Map<String, Any>) {
        firestore?.collection("notifications")?.document(notificationId)?.update(updates)
    }

    fun deleteNotification(notificationId: String) {
        firestore?.collection("notifications")?.document(notificationId)?.delete()
    }

    fun getNotifications(): Flow<List<AppNotification>> = callbackFlow {
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.apply { id = doc.id }
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    fun addSurvey(survey: AppSurvey) {
        if (firestore == null) return
        val docRef = firestore.collection("surveys").document()
        survey.id = docRef.id
        docRef.set(survey)
    }

    fun updateSurvey(surveyId: String, updates: Map<String, Any>) {
        firestore?.collection("surveys")?.document(surveyId)?.update(updates)
    }

    fun deleteSurvey(surveyId: String) {
        firestore?.collection("surveys")?.document(surveyId)?.delete()
    }

    fun getSurveys(): Flow<List<AppSurvey>> = callbackFlow {
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("surveys")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val surveys = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppSurvey::class.java)?.apply { id = doc.id }
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(surveys)
            }
        awaitClose { listener.remove() }
    }

    fun voteSurvey(surveyId: String, optionIndexStr: String) {
        firestore?.collection("surveys")?.document(surveyId)?.update(
            "results.$optionIndexStr",
            com.google.firebase.firestore.FieldValue.increment(1)
        )
    }

    fun getLocalProductConfig(productId: String) = appDao.getLocalProductConfig(productId)
    suspend fun insertLocalProductConfig(config: LocalProductConfig) = appDao.insertLocalProductConfig(config)
    fun getAllLocalProductConfigs() = appDao.getAllLocalProductConfigs()
    
    fun getSeenNotifications() = appDao.getSeenNotifications()
    suspend fun insertSeenNotification(seen: SeenNotification) = appDao.insertSeenNotification(seen)

    fun getP2PContacts(): Flow<List<P2PContact>> = appDao.getP2PContacts()
    suspend fun insertP2PContact(contact: P2PContact) = appDao.insertP2PContact(contact)
    fun getP2PMessages(user1: String, user2: String): Flow<List<P2PMessage>> = appDao.getP2PMessages(user1, user2)
    suspend fun insertP2PMessage(message: P2PMessage) = appDao.insertP2PMessage(message)
}
