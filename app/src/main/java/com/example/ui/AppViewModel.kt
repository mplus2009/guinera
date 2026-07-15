package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.CartItem
import com.example.data.ChatMessage
import com.example.data.Product
import com.example.data.AppNotification
import com.example.data.AppSurvey
import com.example.data.LocalProductConfig
import com.example.data.SeenNotification
import com.example.data.UserSession
import com.example.data.P2PContact
import com.example.data.P2PMessage
import com.example.data.BusinessSpace
import com.example.data.SpaceProduct
import com.example.data.BusinessChat
import com.example.data.BusinessMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(private val repository: AppRepository, val userId: String, initialSession: UserSession?) : ViewModel() {

    private val _userSession = MutableStateFlow<UserSession?>(initialSession)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    val currentUserId: String
        get() = _userSession.value?.userId ?: userId

    private val _currentBlock = MutableStateFlow("Bloque 1")
    val currentBlock: StateFlow<String> = _currentBlock.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _businessSpaces = MutableStateFlow<List<BusinessSpace>>(emptyList())
    val businessSpaces: StateFlow<List<BusinessSpace>> = _businessSpaces.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chats: StateFlow<List<ChatMessage>> = _chats.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _surveys = MutableStateFlow<List<AppSurvey>>(emptyList())
    val surveys: StateFlow<List<AppSurvey>> = _surveys.asStateFlow()

    private val _localConfigs = MutableStateFlow<List<LocalProductConfig>>(emptyList())
    val localConfigs: StateFlow<List<LocalProductConfig>> = _localConfigs.asStateFlow()

    private val _seenNotifications = MutableStateFlow<List<SeenNotification>>(emptyList())
    
    val unseenNotifications = combine(_notifications, _seenNotifications) { notifs, seen ->
        val seenIds = seen.map { it.notificationId }.toSet()
        notifs.filter { it.id !in seenIds }
    }

    init {
        viewModelScope.launch {
            _currentBlock.collectLatest { block ->
                launch {
                    repository.getProducts(block).collectLatest {
                        _products.value = it
                    }
                }
                launch {
                    repository.getChats(block).collectLatest {
                        _chats.value = it
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.getCartItems().collectLatest {
                _cartItems.value = it
            }
        }
        viewModelScope.launch {
            repository.getAllProducts().collectLatest {
                _allProducts.value = it
            }
        }
        viewModelScope.launch {
            repository.getNotifications().collectLatest {
                _notifications.value = it
            }
        }
        viewModelScope.launch {
            repository.getSurveys().collectLatest {
                _surveys.value = it
            }
        }
        viewModelScope.launch {
            repository.getAllLocalProductConfigs().collectLatest {
                _localConfigs.value = it
            }
        }
        viewModelScope.launch {
            repository.getSeenNotifications().collectLatest {
                _seenNotifications.value = it
            }
        }
        viewModelScope.launch {
            repository.getP2PContacts().collectLatest { contacts ->
                if (contacts.isEmpty()) {
                    repository.insertP2PContact(P2PContact("carmen_valdes", "Carmen Valdés", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150"))
                    repository.insertP2PContact(P2PContact("maikel_perez", "Maikel Pérez", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"))
                    repository.insertP2PContact(P2PContact("alejandro_gomez", "Alejandro Gómez", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150"))
                }
            }
        }
        viewModelScope.launch {
            repository.getBusinessSpaces().collectLatest {
                _businessSpaces.value = it
            }
        }
    }

    fun addBusinessSpace(brandName: String, description: String, phoneNumber: String, latitude: Double, longitude: Double, logoUri: Uri?, bannerUri: Uri?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val uploadedLogoUrl = logoUri?.let { repository.uploadImage(it) } ?: ""
            val uploadedBannerUrl = bannerUri?.let { repository.uploadImage(it) } ?: ""
            val space = BusinessSpace(
                ownerId = currentUserId,
                brandName = brandName,
                description = description,
                phoneNumber = phoneNumber,
                latitude = latitude,
                longitude = longitude,
                logoUri = uploadedLogoUrl,
                bannerUri = uploadedBannerUrl
            )
            repository.addBusinessSpace(space, onComplete)
        }
    }

    fun updateBusinessSpace(space: BusinessSpace, newLogoUri: Uri?, newBannerUri: Uri?, onComplete: () -> Unit) {
        viewModelScope.launch {
            val logoUrl = newLogoUri?.let { repository.uploadImage(it) } ?: space.logoUri
            val bannerUrl = newBannerUri?.let { repository.uploadImage(it) } ?: space.bannerUri
            val updatedSpace = space.copy(logoUri = logoUrl, bannerUri = bannerUrl)
            repository.updateBusinessSpace(updatedSpace, onComplete)
        }
    }

    fun updateSpaceProduct(product: SpaceProduct, newImageUris: List<Uri>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val additionalImageUrls = newImageUris.mapNotNull { repository.uploadImage(it) }
            val updatedProduct = product.copy(imageUrls = product.imageUrls + additionalImageUrls)
            repository.updateSpaceProduct(updatedProduct)
            onComplete()
        }
    }

    fun updateSpaceProduct(product: SpaceProduct, onComplete: () -> Unit) {
        repository.updateSpaceProduct(product)
        onComplete()
    }

    fun deleteSpaceProduct(productId: String) {
        repository.deleteSpaceProduct(productId)
    }

    fun getSpaceProducts(spaceId: String) = repository.getSpaceProducts(spaceId)

    suspend fun getSpaceProduct(productId: String): SpaceProduct? {
        return repository.getSpaceProduct(productId)
    }


    fun addSpaceProduct(spaceId: String, name: String, description: String, price: Double, currency: String, imageUris: List<Uri>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val imageUrls = imageUris.mapNotNull { repository.uploadImage(it) }
            repository.addSpaceProduct(
                SpaceProduct(
                    spaceId = spaceId,
                    name = name,
                    description = description,
                    price = price,
                    currency = currency,
                    imageUrls = imageUrls
                )
            )
            onComplete()
        }
    }

    fun setBlock(blockName: String) {
        _currentBlock.value = blockName
    }

    fun addProduct(blockName: String, name: String, prices: Map<String, Double>, quantity: Int, description: String, imageUri: Uri?, onComplete: () -> Unit) {
        viewModelScope.launch {
            val imageUrl = imageUri?.let { repository.uploadImage(it) } ?: ""
            
            // Set the main price and currency for backward compatibility (pick the first one, default CUP)
            val mainPriceEntry = prices.entries.firstOrNull()
            val mainPrice = mainPriceEntry?.value ?: 0.0
            val mainCurrency = mainPriceEntry?.key ?: "CUP"
            
            repository.addProduct(
                Product(
                    blockName = blockName,
                    creatorId = currentUserId,
                    name = name,
                    price = mainPrice,
                    currency = mainCurrency,
                    prices = prices,
                    quantity = quantity,
                    description = description,
                    imageUrl = imageUrl
                )
            )
            onComplete()
        }
    }

    fun deleteProduct(product: Product) {
        if (product.creatorId == currentUserId) {
            repository.deleteProduct(product.id)
        }
    }

    fun rateProduct(product: Product, rating: Float) {
        repository.rateProduct(product.id, currentUserId, rating)
    }

    fun addChatMessage(author: String, message: String, replyToId: String? = null) {
        viewModelScope.launch {
            repository.addChatMessage(
                ChatMessage(
                    blockName = _currentBlock.value,
                    authorId = currentUserId,
                    author = author,
                    message = message,
                    replyToId = replyToId
                )
            )
        }
    }

    fun reactToChat(chatId: String, emoji: String) {
        repository.reactToChat(chatId, currentUserId, emoji)
    }

    fun removeReaction(chatId: String) {
        repository.removeReaction(chatId, currentUserId)
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            repository.insertCartItem(
                CartItem(
                    productId = product.id,
                    blockName = product.blockName,
                    name = product.name,
                    price = product.price
                )
            )
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            repository.deleteCartItem(cartItem)
        }
    }

    fun updateProductQuantity(product: Product, quantity: Int) {
        if (product.creatorId == userId) {
            repository.updateProductQuantity(product.id, quantity)
        }
    }

    fun addNotification(title: String, message: String, buttonText: String = "", buttonUrl: String = "") {
        repository.addNotification(AppNotification(title = title, message = message, buttonText = buttonText, buttonUrl = buttonUrl))
    }

    fun updateNotification(notificationId: String, updates: Map<String, Any>) {
        repository.updateNotification(notificationId, updates)
    }

    fun deleteNotification(notificationId: String) {
        repository.deleteNotification(notificationId)
    }

    fun markNotificationSeen(notificationId: String) {
        viewModelScope.launch {
            repository.insertSeenNotification(SeenNotification(notificationId))
        }
    }

    fun addSurvey(question: String, options: List<String>, imageUrl: String = "", imageUri: Uri? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val finalImageUrl = if (imageUri != null) {
                repository.uploadImage(imageUri) ?: ""
            } else {
                imageUrl
            }
            repository.addSurvey(AppSurvey(question = question, options = options, imageUrl = finalImageUrl))
            onComplete()
        }
    }

    fun updateSurvey(surveyId: String, updates: Map<String, Any>) {
        repository.updateSurvey(surveyId, updates)
    }

    fun deleteSurvey(surveyId: String) {
        repository.deleteSurvey(surveyId)
    }

    fun voteSurvey(surveyId: String, optionIndexStr: String) {
        repository.voteSurvey(surveyId, optionIndexStr)
    }

    fun updateLocalOriginalPrice(productId: String, originalPrice: Double) {
        viewModelScope.launch {
            repository.insertLocalProductConfig(LocalProductConfig(productId, originalPrice))
        }
    }

    fun loginWithGoogle(displayName: String, email: String, avatarUrl: String, uID: String, context: android.content.Context) {
        val newUserId = uID
        val session = UserSession(
            userId = newUserId,
            displayName = displayName,
            email = email,
            avatarUrl = avatarUrl,
            isAnonymous = false,
            uID = uID
        )
        _userSession.value = session
        
        val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("session_user_id", newUserId)
            putString("session_name", displayName)
            putString("session_email", email)
            putString("session_avatar", avatarUrl)
            putBoolean("session_is_anonymous", false)
            putString("session_uid", uID)
        }.apply()
    }

    fun loginAnonymously(uID: String, context: android.content.Context) {
        val anonymousUserId = "anon_${UUID.randomUUID().toString().take(6)}"
        val session = UserSession(
            userId = anonymousUserId,
            displayName = "Vecino Anónimo",
            email = "anonimo@laguinera.com",
            avatarUrl = "",
            isAnonymous = true,
            uID = uID
        )
        _userSession.value = session
        
        val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("session_user_id", anonymousUserId)
            putString("session_name", "Vecino Anónimo")
            putString("session_email", "anonimo@laguinera.com")
            putString("session_avatar", "")
            putBoolean("session_is_anonymous", true)
            putString("session_uid", uID)
        }.apply()
    }

    fun logout(context: android.content.Context) {
        _userSession.value = null
        val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            remove("session_user_id")
            remove("session_name")
            remove("session_email")
            remove("session_avatar")
            remove("session_is_anonymous")
            remove("session_uid")
        }.apply()
    }

    fun getP2PContacts() = repository.getP2PContacts()

    fun searchAndAddContact(contactUid: String, displayName: String): kotlinx.coroutines.flow.Flow<P2PContact?> = kotlinx.coroutines.flow.flow {
        val normalized = contactUid.trim().removePrefix("@").lowercase()
        if (normalized.isNotBlank()) {
            val contact = P2PContact(
                uID = normalized,
                displayName = displayName.ifBlank { "Vecino @$normalized" },
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                isCustom = true
            )
            repository.insertP2PContact(contact)
            emit(contact)
        } else {
            emit(null)
        }
    }

    fun getP2PMessages(contactUid: String) = repository.getP2PMessages(userSession.value?.uID ?: "", contactUid)

    fun sendP2PMessage(receiverUid: String, messageText: String, type: String = "text", mediaUri: String = "", duration: Int = 0) {
        val sender = userSession.value?.uID ?: "anon"
        viewModelScope.launch {
            val p2pMsg = P2PMessage(
                senderUid = sender,
                receiverUid = receiverUid,
                message = messageText,
                type = type,
                mediaUri = mediaUri,
                duration = duration,
                timestamp = System.currentTimeMillis()
            )
            repository.insertP2PMessage(p2pMsg)
            
            // Simulating direct P2P local response when chatting with neighborhood contacts
            if (receiverUid == "carmen_valdes" || receiverUid == "maikel_perez" || receiverUid == "alejandro_gomez") {
                kotlinx.coroutines.delay(1500)
                val replyText = when (type) {
                    "audio" -> "¡He recibido tu audio! Se escucha excelente."
                    "photo" -> "Qué buena foto del barrio, gracias por compartirla."
                    "video" -> "¡Excelente video del barrio! Lo guardaré."
                    "call" -> "Gracias por la llamada, ¡hablamos luego!"
                    else -> "Hola, vecino. Recibí tu mensaje: \"$messageText\". ¡Qué buena idea tener este chat directo de móvil a móvil!"
                }
                val autoMsg = P2PMessage(
                    senderUid = receiverUid,
                    receiverUid = sender,
                    message = replyText,
                    type = "text",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertP2PMessage(autoMsg)
            }
        }
    }


    fun getBusinessChats(): Flow<List<BusinessChat>> {
        val clientIdChats = repository.getBusinessChatsAsClient(currentUserId)
        val ownerIdChats = repository.getBusinessChatsAsOwner(currentUserId)
        return kotlinx.coroutines.flow.combine(clientIdChats, ownerIdChats) { clientChats, ownerChats ->
            (clientChats + ownerChats).distinctBy { it.id }.sortedByDescending { it.lastMessageTime }
        }
    }

    fun getBusinessMessages(chatId: String) = repository.getBusinessMessages(chatId)

    fun sendBusinessMessage(chatId: String, space: BusinessSpace, messageText: String, imageUrl: String? = null, audioUrl: String? = null, attachedProductId: String? = null) {
        val chat = BusinessChat(
            id = chatId,
            spaceId = space.id,
            clientId = currentUserId,
            ownerId = space.ownerId,
            spaceName = space.brandName,
            clientName = userSession.value?.displayName ?: "Usuario",
            ownerName = "Negocio",
            lastMessage = if (messageText.isNotBlank()) messageText else if (imageUrl != null) "Imagen" else if (audioUrl != null) "Audio" else "Catálogo adjunto",
            lastMessageTime = System.currentTimeMillis()
        )
        val message = BusinessMessage(
            chatId = chatId,
            senderId = currentUserId,
            message = messageText,
            imageUrl = imageUrl ?: "",
            audioUrl = audioUrl ?: "",
            attachedProductId = attachedProductId ?: "",
            timestamp = System.currentTimeMillis()
        )
        repository.sendBusinessMessage(chat, message)
    }

    fun sendBusinessMessageExisting(chat: BusinessChat, messageText: String, imageUrl: String? = null, audioUrl: String? = null, attachedProductId: String? = null) {
        val updatedChat = chat.copy(
            lastMessage = if (messageText.isNotBlank()) messageText else if (imageUrl != null) "Imagen" else if (audioUrl != null) "Audio" else "Catálogo adjunto",
            lastMessageTime = System.currentTimeMillis()
        )
        val message = BusinessMessage(
            chatId = chat.id,
            senderId = currentUserId,
            message = messageText,
            imageUrl = imageUrl ?: "",
            audioUrl = audioUrl ?: "",
            attachedProductId = attachedProductId ?: "",
            timestamp = System.currentTimeMillis()
        )
        repository.sendBusinessMessage(updatedChat, message)
    }
}
