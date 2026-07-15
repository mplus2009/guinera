package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.BusinessChat
import com.example.data.BusinessMessage
import com.example.data.SpaceProduct
import com.example.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessChatsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit
) {
    val chats by viewModel.getBusinessChats().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats de Negocios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (chats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay chats de negocios todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(chats) { chat ->
                    BusinessChatListItem(
                        chat = chat,
                        currentUserId = viewModel.currentUserId,
                        onClick = { onOpenChat(chat.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BusinessChatListItem(chat: BusinessChat, currentUserId: String, onClick: () -> Unit) {
    val isOwner = chat.ownerId == currentUserId
    val otherName = if (isOwner) chat.clientName else chat.spaceName

    ListItem(
        headlineContent = { Text(otherName, fontWeight = FontWeight.Bold) },
        supportingContent = {
            Text(
                text = chat.lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            val date = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(chat.lastMessageTime))
            Text(date, style = MaterialTheme.typography.bodySmall)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessChatDetailScreen(
    chatId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.getBusinessMessages(chatId).collectAsState(initial = emptyList())
    val chats by viewModel.getBusinessChats().collectAsState(initial = emptyList())
    val chat = chats.find { it.id == chatId }
    
    var messageText by remember { mutableStateOf("") }
    val currentUserId = viewModel.currentUserId
    var showAttachDialog by remember { mutableStateOf(false) }
    var showCatalogDialog by remember { mutableStateOf(false) }
    
    val otherName = if (chat?.ownerId == currentUserId) chat?.clientName else chat?.spaceName
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherName ?: "Chat", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Call */ }) {
                        Icon(Icons.Filled.Call, contentDescription = "Llamar")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAttachDialog = true }) {
                        Icon(Icons.Filled.AttachFile, contentDescription = "Adjuntar")
                    }
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (messageText.isNotBlank()) {
                        FloatingActionButton(
                            onClick = {
                                chat?.let {
                                    viewModel.sendBusinessMessageExisting(it, messageText)
                                    messageText = ""
                                }
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.Send, contentDescription = "Enviar")
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { /* TODO: Record Audio */ },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Filled.Mic, contentDescription = "Grabar audio")
                        }
                    }
                }
            }
        }
    ) { padding ->
        val spaceProducts by remember(chat?.spaceId) { 
            if (chat?.spaceId != null) viewModel.getSpaceProducts(chat.spaceId) 
            else kotlinx.coroutines.flow.flowOf(emptyList()) 
        }.collectAsState(initial = emptyList())
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                MessageBubble(message = msg, isMe = msg.senderId == currentUserId, spaceProducts = spaceProducts)
            }
        }
    }
    
    if (showAttachDialog) {
        ModalBottomSheet(onDismissRequest = { showAttachDialog = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Fotos y Videos") },
                    leadingContent = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable { showAttachDialog = false /* TODO */ }
                )
                ListItem(
                    headlineContent = { Text("Adjuntar producto del catálogo") },
                    leadingContent = { Icon(Icons.Filled.Inventory, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showAttachDialog = false
                        showCatalogDialog = true
                    }
                )
            }
        }
    }
    
    if (showCatalogDialog && chat != null) {
        CatalogSelectionDialog(
            spaceId = chat.spaceId,
            viewModel = viewModel,
            onDismiss = { showCatalogDialog = false },
            onProductSelected = { product ->
                viewModel.sendBusinessMessageExisting(chat, "", attachedProductId = product.id)
                showCatalogDialog = false
            }
        )
    }
}

@Composable
fun MessageBubble(message: BusinessMessage, isMe: Boolean, spaceProducts: List<SpaceProduct>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.message.isNotBlank()) {
                    Text(message.message, color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (message.attachedProductId.isNotBlank()) {
                    val product = spaceProducts.find { it.id == message.attachedProductId }
                    if (product != null) {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                if (product.imageUrls.isNotEmpty()) {
                                    AsyncImage(
                                        model = product.imageUrls.first(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(product.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${product.price} ${product.currency}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun CatalogSelectionDialog(
    spaceId: String,
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onProductSelected: (SpaceProduct) -> Unit
) {
    val products by remember { viewModel.getSpaceProducts(spaceId) }.collectAsState(initial = emptyList())
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)) {
                Text("Seleccionar Producto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(products) { product ->
                        ProductListItem(
                            product = product,
                            onEdit = null,
                            modifier = Modifier.clickable { onProductSelected(product) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancelar")
                }
            }
        }
    }
}
