package com.example.data

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.example.BuildConfig
import android.util.Base64
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AppRepository(private val appDao: AppDao, private val context: android.content.Context) {
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    private val storage: FirebaseStorage? = try { FirebaseStorage.getInstance() } catch (e: Exception) { null }

    private val client = OkHttpClient()

    suspend fun uploadImage(uri: Uri): String? = kotlinx.coroutines.Dispatchers.IO.let {
        return kotlinx.coroutines.withContext(it) {
            try {
                // Read bytes from URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes == null) {
                    android.util.Log.e("ImgBB", "Could not read bytes from URI")
                    return@withContext null
                }

                // Check for API key
                // INYECTADO DIRECTAMENTE EN EL CÓDIGO
                val apiKey = "48d2204b8ac85d4a6e3b956e8d597843"
                if (apiKey.isEmpty()) {
                    android.util.Log.e("ImgBB", "API Key de ImgBB no configurada en el código.")
                    return@withContext null
                }

                // Convert to Base64
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                // Build Request
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", apiKey)
                    .addFormDataPart("image", base64Image)
                    .build()

                val request = Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    if (json.optBoolean("success", false)) {
                        return@withContext json.getJSONObject("data").getString("url")
                    }
                }
                android.util.Log.e("ImgBB", "Upload failed: $responseBody")
                null
            } catch (e: Exception) {
                android.util.Log.e("ImgBB", "Exception during upload", e)
                null
            }
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

    fun getBusinessSpaces(): Flow<List<BusinessSpace>> = callbackFlow {
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("business_spaces")
            .addSnapshotListener { snapshot, _ ->
                val spaces = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(BusinessSpace::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(spaces)
            }
        awaitClose { listener.remove() }
    }

    fun addBusinessSpace(space: BusinessSpace, onComplete: (String) -> Unit) {
        if (firestore == null) return
        val docRef = firestore.collection("business_spaces").document()
        space.id = docRef.id
        docRef.set(space).addOnSuccessListener { onComplete(docRef.id) }
    }

    fun updateBusinessSpace(space: BusinessSpace, onComplete: () -> Unit) {
        if (firestore == null) return
        firestore.collection("business_spaces").document(space.id).set(space).addOnSuccessListener { onComplete() }
    }


    fun getSpaceProducts(spaceId: String): Flow<List<SpaceProduct>> = callbackFlow {
        if (firestore == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("space_products")
            .whereEqualTo("spaceId", spaceId)
            .addSnapshotListener { snapshot, _ ->
                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SpaceProduct::class.java)?.apply { id = doc.id }
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { listener.remove() }
    }

    fun addSpaceProduct(product: SpaceProduct) {
        if (firestore == null) return
        val docRef = firestore.collection("space_products").document()
        product.id = docRef.id
        docRef.set(product)
    }

    
    suspend fun getSpaceProduct(productId: String): SpaceProduct? {
        if (firestore == null) return null
        return try {
            firestore.collection("space_products").document(productId).get().await().toObject(SpaceProduct::class.java)
        } catch (e: Exception) { null }
    }

    fun updateSpaceProduct(product: SpaceProduct) {
        if (firestore == null) return
        firestore.collection("space_products").document(product.id).set(product)
    }

    fun deleteSpaceProduct(productId: String) {
        if (firestore == null) return
        firestore.collection("space_products").document(productId).delete()
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
                    doc.toObject(ChatMessage::class.java)?.apply { id = doc.id; isPending = doc.metadata.hasPendingWrites() }
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


    fun getBusinessChatsAsClient(userId: String): Flow<List<BusinessChat>> = callbackFlow {
        if (firestore == null) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("business_chats").whereEqualTo("clientId", userId)
            .addSnapshotListener { snapshot, _ ->
                val chats = snapshot?.documents?.mapNotNull { it.toObject(BusinessChat::class.java)?.apply { id = it.id } } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    fun getBusinessChatsAsOwner(userId: String): Flow<List<BusinessChat>> = callbackFlow {
        if (firestore == null) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("business_chats").whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, _ ->
                val chats = snapshot?.documents?.mapNotNull { it.toObject(BusinessChat::class.java)?.apply { id = it.id } } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }
    
    fun getBusinessMessages(chatId: String): Flow<List<BusinessMessage>> = callbackFlow {
        if (firestore == null) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("business_chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val msgs = snapshot?.documents?.mapNotNull { it.toObject(BusinessMessage::class.java)?.apply { id = it.id; isPending = it.metadata.hasPendingWrites() } } ?: emptyList()
                trySend(msgs)
            }
        awaitClose { listener.remove() }
    }
    
    fun sendBusinessMessage(chat: BusinessChat, message: BusinessMessage) {
        if (firestore == null) return
        val chatRef = firestore.collection("business_chats").document(chat.id)
        chatRef.set(chat)
        val msgRef = chatRef.collection("messages").document()
        message.id = msgRef.id
        msgRef.set(message)
    }

}
