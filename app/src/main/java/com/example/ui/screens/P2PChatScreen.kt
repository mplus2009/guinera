package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.P2PContact
import com.example.data.P2PMessage
import com.example.ui.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PChatScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val userSession by viewModel.userSession.collectAsState()
    val contacts by viewModel.getP2PContacts().collectAsState(initial = emptyList())
    
    var activeContact by remember { mutableStateOf<P2PContact?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Call Simulators State
    var activeCallType by remember { mutableStateOf<String?>(null) } // "voice", "video"
    var callTimerSeconds by remember { mutableStateOf(0) }
    
    // Recording Simulator State
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    
    // Media attachment chooser
    var showAttachmentMenu by remember { mutableStateOf(false) }
    
    // Photo preview modal
    var expandedPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Navigation back press or close active chat
    val handleBack = {
        if (activeCallType != null) {
            activeCallType = null
        } else if (activeContact != null) {
            activeContact = null
        } else {
            onBack()
        }
    }

    // Call duration timer
    LaunchedEffect(activeCallType) {
        if (activeCallType != null) {
            callTimerSeconds = 0
            while (activeCallType != null) {
                delay(1000)
                callTimerSeconds++
            }
        }
    }

    // Audio recording timer
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingDuration = 0
            while (isRecordingAudio) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    Scaffold(
        topBar = {
            if (activeCallType == null) {
                TopAppBar(
                    title = {
                        if (activeContact != null) {
                            Column {
                                Text(
                                    text = activeContact!!.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${activeContact!!.uID}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                "Chats Directos P2P",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("p2p_chat_title")
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { handleBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                    actions = {
                        if (activeContact != null) {
                            IconButton(onClick = { activeCallType = "voice" }) {
                                Icon(Icons.Default.Call, contentDescription = "Llamada de voz", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { activeCallType = "video" }) {
                                Icon(Icons.Filled.Videocam, contentDescription = "Videollamada", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (activeCallType != null) {
                // Render call simulator full screen
                CallSimulator(
                    contact = activeContact!!,
                    callType = activeCallType!!,
                    durationSeconds = callTimerSeconds,
                    onHangUp = {
                        // Log call in messages
                        viewModel.sendP2PMessage(
                            receiverUid = activeContact!!.uID,
                            messageText = if (activeCallType == "voice") "Llamada de voz finalizada" else "Videollamada finalizada",
                            type = "call",
                            duration = callTimerSeconds
                        )
                        activeCallType = null
                        Toast.makeText(context, "Llamada finalizada", Toast.LENGTH_SHORT).show()
                    }
                )
            } else if (activeContact != null) {
                // Render active chat thread
                val messages by viewModel.getP2PMessages(activeContact!!.uID).collectAsState(initial = emptyList())
                
                ChatThreadView(
                    messages = messages,
                    currentUid = userSession?.uID ?: "",
                    contact = activeContact!!,
                    isRecordingAudio = isRecordingAudio,
                    recordingDuration = recordingDuration,
                    onSendMessage = { text ->
                        viewModel.sendP2PMessage(activeContact!!.uID, text)
                    },
                    onSendAudio = {
                        isRecordingAudio = false
                        viewModel.sendP2PMessage(
                            receiverUid = activeContact!!.uID,
                            messageText = "Audio mensaje (${recordingDuration}s)",
                            type = "audio",
                            duration = recordingDuration
                        )
                        Toast.makeText(context, "Mensaje de voz enviado", Toast.LENGTH_SHORT).show()
                    },
                    onCancelRecording = {
                        isRecordingAudio = false
                    },
                    onStartRecording = {
                        isRecordingAudio = true
                    },
                    onAttachPhoto = {
                        viewModel.sendP2PMessage(
                            receiverUid = activeContact!!.uID,
                            messageText = "Foto del barrio",
                            type = "photo",
                            mediaUri = "https://images.unsplash.com/photo-1590073844006-33379778ae09?w=500"
                        )
                        Toast.makeText(context, "Foto de móvil a móvil enviada", Toast.LENGTH_SHORT).show()
                    },
                    onAttachVideo = {
                        viewModel.sendP2PMessage(
                            receiverUid = activeContact!!.uID,
                            messageText = "Video comunitario",
                            type = "video",
                            mediaUri = "https://assets.mixkit.co/videos/preview/mixkit-community-volunteers-planting-together-41885-large.mp4"
                        )
                        Toast.makeText(context, "Video enviado de forma segura", Toast.LENGTH_SHORT).show()
                    },
                    onPhotoClick = { url ->
                        expandedPhotoUrl = url
                    }
                )
            } else {
                // Render list of contacts
                ContactListView(
                    contacts = contacts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onContactSelected = { activeContact = it },
                    onStartChatWithUid = { handle ->
                        coroutineScope.launch {
                            viewModel.searchAndAddContact(handle, "").collect { newContact ->
                                if (newContact != null) {
                                    activeContact = newContact
                                    searchQuery = ""
                                    Toast.makeText(context, "Chat con @$handle iniciado", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Usuario inválido", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }

            // Expanded Photo Dialog
            if (expandedPhotoUrl != null) {
                AlertDialog(
                    onDismissRequest = { expandedPhotoUrl = null },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            AsyncImage(
                                model = expandedPhotoUrl,
                                contentDescription = "Foto ampliada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { expandedPhotoUrl = null }) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ContactListView(
    contacts: List<P2PContact>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onContactSelected: (P2PContact) -> Unit,
    onStartChatWithUid: (String) -> Unit
) {
    val filteredContacts = contacts.filter {
        it.displayName.contains(searchQuery, ignoreCase = true) ||
        it.uID.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Buscar vecinos por nombre o @uID...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("contact_search_bar"),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredContacts.isEmpty() && searchQuery.isNotBlank()) {
            // Option to search / add direct P2P connection by custom UID handle
            val cleanedHandle = searchQuery.trim().lowercase().removePrefix("@").replace(" ", "_")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "¿Conectar con @$cleanedHandle?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Inicia una sesión de mensajería directa y privada móvil a móvil.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onStartChatWithUid(cleanedHandle) },
                        modifier = Modifier.testTag("add_p2p_contact_btn")
                    ) {
                        Text("Iniciar Chat Directo")
                    }
                }
            }
        }

        Text(
            "Vecinos Disponibles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (filteredContacts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No hay chats iniciados todavía.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Busca un vecino por su uID arriba para conectar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onContactSelected(contact) }
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (contact.avatarUrl.isNotBlank()) {
                                AsyncImage(
                                    model = contact.avatarUrl,
                                    contentDescription = "Avatar de ${contact.displayName}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    contact.displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${contact.uID}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Ver chat",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Security footer
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Seguridad Descentralizada P2P • No guardado en servidores",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadView(
    messages: List<P2PMessage>,
    currentUid: String,
    contact: P2PContact,
    isRecordingAudio: Boolean,
    recordingDuration: Int,
    onSendMessage: (String) -> Unit,
    onSendAudio: () -> Unit,
    onCancelRecording: () -> Unit,
    onStartRecording: () -> Unit,
    onAttachPhoto: () -> Unit,
    onAttachVideo: () -> Unit,
    onPhotoClick: (String) -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showAttachmentMenu by remember { mutableStateOf(false) }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // E2E Notice header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(vertical = 6.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cifrado Móvil a Móvil. No se almacena nada en la nube.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages) { msg ->
                val isOutgoing = msg.senderUid == currentUid
                MessageBubble(
                    msg = msg,
                    isOutgoing = isOutgoing,
                    onPhotoClick = onPhotoClick
                )
            }
        }

        // Recording view or standard input
        if (isRecordingAudio) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Grabando Audio: ${recordingDuration}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = onCancelRecording,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = onSendAudio,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Enviar")
                    }
                }
            }
        } else {
            // Standard Input bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
            ) {
                // Attachment choices dropdown simulation
                AnimatedVisibility(
                    visible = showAttachmentMenu,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action: Photo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showAttachmentMenu = false
                                    onAttachPhoto()
                                }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = "Enviar Foto", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Foto", style = MaterialTheme.typography.labelMedium)
                        }

                        // Action: Video
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showAttachmentMenu = false
                                    onAttachVideo()
                                }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Movie, contentDescription = "Enviar Video", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Video", style = MaterialTheme.typography.labelMedium)
                        }

                        // Action: Audio
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    showAttachmentMenu = false
                                    onStartRecording()
                                }
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = "Grabar Audio", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Audio", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showAttachmentMenu = !showAttachmentMenu },
                        modifier = Modifier.testTag("chat_attachment_btn")
                    ) {
                        Icon(
                            imageVector = if (showAttachmentMenu) Icons.Filled.Close else Icons.Filled.AddCircleOutline,
                            contentDescription = "Adjuntar archivo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it },
                        placeholder = { Text("Mensaje seguro móvil a móvil...") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (textValue.isNotBlank()) {
                                    onSendMessage(textValue.trim())
                                    textValue = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (textValue.isNotBlank()) {
                                onSendMessage(textValue.trim())
                                textValue = ""
                            } else {
                                onStartRecording()
                            }
                        },
                        modifier = Modifier.testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = if (textValue.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Filled.Mic,
                            contentDescription = "Enviar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    msg: P2PMessage,
    isOutgoing: Boolean,
    onPhotoClick: (String) -> Unit
) {
    val bubbleColor = if (isOutgoing) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (isOutgoing) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val alignment = if (isOutgoing) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Column {
                when (msg.type) {
                    "audio" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = textColor)
                            // Waveform simulator
                            Row(
                                modifier = Modifier.width(120.dp).height(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(12) { index ->
                                    val barHeight = when (index % 3) {
                                        0 -> 16.dp
                                        1 -> 8.dp
                                        else -> 12.dp
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(barHeight)
                                            .clip(CircleShape)
                                            .background(textColor.copy(alpha = 0.6f))
                                    )
                                }
                            }
                            Text(
                                text = "${msg.duration}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    "photo" -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onPhotoClick(msg.mediaUri) }
                            ) {
                                AsyncImage(
                                    model = msg.mediaUri,
                                    contentDescription = "Foto recibida",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom", tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            if (msg.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = msg.message, color = textColor, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    "video" -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=250",
                                    contentDescription = "Miniatura",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().alpha(0.5f)
                                )
                                Icon(Icons.Filled.PlayCircleOutline, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(48.dp))
                            }
                            if (msg.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = msg.message, color = textColor, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    "call" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (msg.message.contains("voz", ignoreCase = true)) Icons.Filled.PhoneCallback else Icons.Filled.VideoCall,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                val mins = msg.duration / 60
                                val secs = msg.duration % 60
                                Text(
                                    text = "Duración: ${mins}m ${secs}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = msg.message,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Timestamp text
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateString = sdf.format(Date(msg.timestamp))
        Text(
            text = dateString,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CallSimulator(
    contact: P2PContact,
    callType: String,
    durationSeconds: Int,
    onHangUp: () -> Unit
) {
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    val timeStr = String.format("%02d:%02d", mins, secs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F))
    ) {
        if (callType == "video") {
            // Simulated Video Stream Background
            AsyncImage(
                model = "https://images.unsplash.com/photo-1590073844006-33379778ae09?w=600",
                contentDescription = "Fondo de videollamada",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.6f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Contact Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (contact.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = contact.avatarUrl,
                            contentDescription = contact.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            contact.displayName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = contact.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "@${contact.uID}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Green)
                    Text(
                        text = "Conexión E2E Encriptada Directa",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Duration timer in the center
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (callType == "voice") "Llamando por voz..." else "Videollamada activa...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }

            // Call Controls Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute mic button
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Filled.MicOff, contentDescription = "Silenciar Micrófono", tint = Color.White)
                }

                // Hang Up Button
                IconButton(
                    onClick = onHangUp,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .testTag("hang_up_btn")
                ) {
                    Icon(Icons.Filled.CallEnd, contentDescription = "Colgar", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                // Speaker button
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = "Altavoz", tint = Color.White)
                }
            }
        }
    }
}
