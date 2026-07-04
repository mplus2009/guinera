package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ChatMessage
import com.example.data.Product
import com.example.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockScreen(
    blockName: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onLoginRedirect: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Productos", "Comunidad")

    val products by viewModel.products.collectAsState()
    val chats by viewModel.chats.collectAsState()
    
    val userSession by viewModel.userSession.collectAsState()
    val isAnonymous = userSession?.isAnonymous ?: true
    var showAddProductWarning by remember { mutableStateOf(false) }

    if (showAddProductWarning) {
        AlertDialog(
            onDismissRequest = { showAddProductWarning = false },
            title = { Text("Función Restringida") },
            text = {
                Text("Para poder publicar tus productos en La Güinera y vender a tus vecinos, debes iniciar sesión con una cuenta de Google.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAddProductWarning = false
                        onLoginRedirect()
                    }
                ) {
                    Text("Acceder con Google")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductWarning = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(blockName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        if (isAnonymous) {
                            showAddProductWarning = true
                        } else {
                            onAddProduct()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir Producto")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
            
            when (selectedTabIndex) {
                0 -> ProductsList(
                    products = products, 
                    currentUserId = viewModel.currentUserId,
                    onAddToCart = { product -> viewModel.addToCart(product) },
                    onDeleteProduct = { product -> viewModel.deleteProduct(product) },
                    onRateProduct = { product, rating -> viewModel.rateProduct(product, rating) }
                )
                1 -> ChatList(
                    chats = chats, 
                    currentUserId = viewModel.currentUserId,
                    displayName = userSession?.displayName ?: "",
                    isAnonymous = isAnonymous,
                    onLoginRedirect = onLoginRedirect,
                    onSend = { author, message, replyToId ->
                        viewModel.addChatMessage(author, message, replyToId)
                    },
                    onReact = { chatId, emoji -> viewModel.reactToChat(chatId, emoji) },
                    onRemoveReaction = { chatId -> viewModel.removeReaction(chatId) }
                )
            }
        }
    }
}

@Composable
fun ProductsList(
    products: List<Product>, 
    currentUserId: String,
    onAddToCart: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onRateProduct: (Product, Float) -> Unit
) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no hay productos en esta zona", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(products) { product ->
                ProductCard(product, currentUserId, onAddToCart, onDeleteProduct, onRateProduct)
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product, 
    currentUserId: String,
    onAddToCart: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onRateProduct: (Product, Float) -> Unit
) {
    var showRatingDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            if (product.imageUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Imagen de ${product.name}",
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (product.creatorId == currentUserId) {
                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$${product.price} CUP", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Disponibles: ${product.quantity}", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (product.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = product.description, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = product.creatorId != currentUserId) {
                            showRatingDialog = true
                        }.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star, 
                            contentDescription = "Rating", 
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", product.getAverageRating()), 
                            style = MaterialTheme.typography.bodyMedium, 
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = " (${product.ratings.size})", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = { onAddToCart(product) },
                        shape = RoundedCornerShape(8.dp),
                        enabled = product.quantity > 0
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir")
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        var newRating by remember { mutableStateOf(product.ratings[currentUserId] ?: 5f) }
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Valorar Producto") },
            text = {
                Column {
                    Text("Selecciona una valoración de 1 a 5:")
                    Slider(
                        value = newRating,
                        onValueChange = { newRating = it },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    Text("Valor: ${newRating.toInt()}", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onRateProduct(product, newRating)
                    showRatingDialog = false
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ChatList(
    chats: List<ChatMessage>, 
    currentUserId: String,
    displayName: String,
    isAnonymous: Boolean,
    onLoginRedirect: () -> Unit,
    onSend: (String, String, String?) -> Unit,
    onReact: (String, String) -> Unit,
    onRemoveReaction: (String) -> Unit
) {
    val author = remember(displayName, currentUserId) {
        if (displayName.isNotBlank()) displayName else "usuario:${currentUserId.take(4)}"
    }
    var message by remember { mutableStateOf("") }
    var replyToMsg by remember { mutableStateOf<ChatMessage?>(null) }
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var showCommentWarning by remember { mutableStateOf(false) }

    if (showCommentWarning) {
        AlertDialog(
            onDismissRequest = { showCommentWarning = false },
            title = { Text("Función Restringida") },
            text = {
                Text("Para poder comentar en la comunidad del barrio, debes iniciar sesión con una cuenta de Google.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCommentWarning = false
                        onLoginRedirect()
                    }
                ) {
                    Text("Acceder con Google")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentWarning = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chats) { chat ->
                val isMe = chat.authorId == currentUserId
                var showReactions by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = chat.author, 
                        style = MaterialTheme.typography.labelMedium, 
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    
                    if (chat.replyToId != null) {
                        val repliedChat = chats.find { it.id == chat.replyToId }
                        if (repliedChat != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp, start = if(isMe) 24.dp else 0.dp, end = if(!isMe) 24.dp else 0.dp)
                            ) {
                                Text(
                                    text = "Respondiendo a ${repliedChat.author}: ${repliedChat.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = if (isMe) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp) else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                        color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clickable { showReactions = !showReactions }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text(
                                text = chat.message, 
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatter.format(Date(chat.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }

                    if (chat.reactions.isNotEmpty()) {
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            chat.reactions.values.distinct().forEach { emoji ->
                                val count = chat.reactions.values.count { it == emoji }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text("$emoji $count", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    if (showReactions) {
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("👍", "❤️", "😂", "😢", "🔥").forEach { emoji ->
                                IconButton(
                                    onClick = {
                                        if (chat.reactions[currentUserId] == emoji) {
                                            onRemoveReaction(chat.id)
                                        } else {
                                            onReact(chat.id, emoji)
                                        }
                                        showReactions = false
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(emoji)
                                }
                            }
                            IconButton(
                                onClick = {
                                    replyToMsg = chat
                                    showReactions = false
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = "Responder", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
        
        Surface(
            shadowElevation = 16.dp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (replyToMsg != null) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Respondiendo a ${replyToMsg!!.author}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { replyToMsg = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancelar", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (!isAnonymous) {
                    Text(
                        text = "Comentando como: $author",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (isAnonymous) "" else message,
                            onValueChange = { if (!isAnonymous) message = it },
                            label = { Text(if (isAnonymous) "Inicia sesión para comentar..." else "Mensaje...") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isAnonymous,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        if (isAnonymous) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { showCommentWarning = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    FilledIconButton(
                        onClick = {
                            if (message.isNotBlank()) {
                                onSend(author, message, replyToMsg?.id)
                                message = ""
                                replyToMsg = null
                            }
                        },
                        enabled = message.isNotBlank() && !isAnonymous,
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}
