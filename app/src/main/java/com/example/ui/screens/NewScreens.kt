package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import com.example.data.Product
import com.example.data.AppNotification
import com.example.data.AppSurvey
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val allProducts by viewModel.allProducts.collectAsState()

    val filteredProducts = if (query.isBlank()) {
        emptyList()
    } else {
        allProducts.filter { it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar producto...") },
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredProducts) { product ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Bloque: ${product.blockName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Precio: $${product.price} CUP", style = MaterialTheme.typography.bodyMedium)
                        Text("Cantidad disponible: ${product.quantity}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (query.isNotBlank() && filteredProducts.isEmpty()) {
                item {
                    Text("No se encontraron productos.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var buttonText by remember { mutableStateOf("") }
    var buttonUrl by remember { mutableStateOf("") }
    
    var question by remember { mutableStateOf("") }
    var surveyImageUrl by remember { mutableStateOf("") }
    var surveyImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPublishingSurvey by remember { mutableStateOf(false) }
    val surveyOptions = remember { mutableStateListOf<String>("", "") }

    val surveyImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        surveyImageUri = uri
    }

    val allProducts by viewModel.allProducts.collectAsState()
    val surveys by viewModel.surveys.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var editingNotification by remember { mutableStateOf<AppNotification?>(null) }
    var editingSurvey by remember { mutableStateOf<AppSurvey?>(null) }

    // Dialog to edit a notification
    if (editingNotification != null) {
        val notif = editingNotification!!
        var editTitle by remember(notif.id) { mutableStateOf(notif.title) }
        var editMessage by remember(notif.id) { mutableStateOf(notif.message) }
        var editBtnText by remember(notif.id) { mutableStateOf(notif.buttonText) }
        var editBtnUrl by remember(notif.id) { mutableStateOf(notif.buttonUrl) }
        
        AlertDialog(
            onDismissRequest = { editingNotification = null },
            title = { Text("Editar Notificación", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editMessage, onValueChange = { editMessage = it }, label = { Text("Mensaje") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = editBtnText, onValueChange = { editBtnText = it }, label = { Text("Texto del botón (Opcional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editBtnUrl, onValueChange = { editBtnUrl = it }, label = { Text("URL del botón (Opcional)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank() && editMessage.isNotBlank()) {
                            viewModel.updateNotification(
                                notif.id,
                                mapOf(
                                    "title" to editTitle,
                                    "message" to editMessage,
                                    "buttonText" to editBtnText,
                                    "buttonUrl" to editBtnUrl
                                )
                            )
                            editingNotification = null
                        }
                    },
                    enabled = editTitle.isNotBlank() && editMessage.isNotBlank()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNotification = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog to edit a survey
    if (editingSurvey != null) {
        val s = editingSurvey!!
        var editQuestion by remember(s.id) { mutableStateOf(s.question) }
        var editImgUrl by remember(s.id) { mutableStateOf(s.imageUrl) }
        val editOptions = remember(s.id) { mutableStateListOf<String>().apply { addAll(s.options) } }
        
        AlertDialog(
            onDismissRequest = { editingSurvey = null },
            title = { Text("Editar Encuesta", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = editQuestion, onValueChange = { editQuestion = it }, label = { Text("Pregunta") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editImgUrl, onValueChange = { editImgUrl = it }, label = { Text("URL de la imagen (Opcional)") }, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Opciones de la encuesta:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    editOptions.forEachIndexed { index, opt ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = opt,
                                onValueChange = { editOptions[index] = it },
                                label = { Text("Opción ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                            if (editOptions.size > 2) {
                                IconButton(onClick = { editOptions.removeAt(index) }) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Eliminar Opción", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    
                    TextButton(onClick = { editOptions.add("") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Add, contentDescription = "Añadir Opción")
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir Opción")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validOptions = editOptions.filter { it.isNotBlank() }
                        if (editQuestion.isNotBlank() && validOptions.size >= 2) {
                            viewModel.updateSurvey(
                                s.id,
                                mapOf(
                                    "question" to editQuestion,
                                    "imageUrl" to editImgUrl,
                                    "options" to validOptions
                                )
                            )
                            editingSurvey = null
                        }
                    },
                    enabled = editQuestion.isNotBlank() && editOptions.filter { it.isNotBlank() }.size >= 2
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSurvey = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Desarrollador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Estadísticas del Sistema", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatCard("Productos", allProducts.size.toString(), Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    StatCard("Encuestas", surveys.size.toString(), Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    StatCard("Notif.", notifications.size.toString(), Modifier.weight(1f))
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Create Notification Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear Notificación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título de la notificación") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Mensaje") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        OutlinedTextField(value = buttonText, onValueChange = { buttonText = it }, label = { Text("Texto del botón (Opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = buttonUrl, onValueChange = { buttonUrl = it }, label = { Text("URL del botón (Opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(
                            onClick = {
                                if (title.isNotBlank() && message.isNotBlank()) {
                                    viewModel.addNotification(title, message, buttonText, buttonUrl)
                                    title = ""
                                    message = ""
                                    buttonText = ""
                                    buttonUrl = ""
                                }
                            }, 
                            modifier = Modifier.fillMaxWidth(),
                            enabled = title.isNotBlank() && message.isNotBlank()
                        ) {
                            Text("Enviar Notificación")
                        }
                    }
                }
            }
            
            // List of Notifications
            if (notifications.isNotEmpty()) {
                item { Text("Notificaciones Creadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(notifications) { notif ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(if (notif.isActive) "Activa" else "Inactiva") },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (notif.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        labelColor = if (notif.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Text(notif.message, style = MaterialTheme.typography.bodyMedium)
                            if (notif.buttonText.isNotBlank() && notif.buttonUrl.isNotBlank()) {
                                Text("Botón: ${notif.buttonText} (${notif.buttonUrl})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Visibilidad:", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = notif.isActive, 
                                    onCheckedChange = { viewModel.updateNotification(notif.id, mapOf("isActive" to it)) }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { editingNotification = notif }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.deleteNotification(notif.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // Create Survey Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear Encuesta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Pregunta de la encuesta") }, modifier = Modifier.fillMaxWidth())
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = surveyImageUrl,
                                onValueChange = { 
                                    surveyImageUrl = it
                                    if (it.isNotBlank()) {
                                        surveyImageUri = null
                                    }
                                },
                                label = { Text("URL de la imagen (Opcional)") },
                                modifier = Modifier.weight(1f),
                                enabled = surveyImageUri == null
                            )
                            
                            Button(
                                onClick = { surveyImagePickerLauncher.launch("image/*") },
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Galería")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Galería")
                            }
                        }
                        
                        surveyImageUri?.let { uri ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    coil.compose.AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected Survey Image",
                                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Imagen seleccionada", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Se subirá al publicar la encuesta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { surveyImageUri = null }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Quitar imagen", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                        
                        Text("Opciones de la encuesta:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        surveyOptions.forEachIndexed { index, option ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = option,
                                    onValueChange = { surveyOptions[index] = it },
                                    label = { Text("Opción ${index + 1}") },
                                    modifier = Modifier.weight(1f)
                                )
                                if (surveyOptions.size > 2) {
                                    IconButton(onClick = { surveyOptions.removeAt(index) }) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Eliminar Opción", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                        
                        TextButton(onClick = { surveyOptions.add("") }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir Opción")
                            Spacer(Modifier.width(4.dp))
                            Text("Añadir Opción")
                        }
                        
                        Button(
                            onClick = {
                                val validOptions = surveyOptions.filter { it.isNotBlank() }
                                if (question.isNotBlank() && validOptions.size >= 2) {
                                    isPublishingSurvey = true
                                    viewModel.addSurvey(question, validOptions, surveyImageUrl, surveyImageUri) {
                                        isPublishingSurvey = false
                                        question = ""
                                        surveyImageUrl = ""
                                        surveyImageUri = null
                                        surveyOptions.clear()
                                        surveyOptions.addAll(listOf("", ""))
                                    }
                                }
                            }, 
                            modifier = Modifier.fillMaxWidth(),
                            enabled = question.isNotBlank() && surveyOptions.filter { it.isNotBlank() }.size >= 2 && !isPublishingSurvey
                        ) {
                            if (isPublishingSurvey) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Publicar Encuesta")
                            }
                        }
                    }
                }
            }
            
            // List of Surveys
            if (surveys.isNotEmpty()) {
                item { Text("Encuestas Creadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(surveys) { survey ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(survey.question, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(if (survey.isActive) "Activa" else "Inactiva") },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (survey.isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                        labelColor = if (survey.isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            
                            if (survey.imageUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = survey.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 4.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                            
                            Text("Resultados actuales:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            
                            val totalVotes = survey.results.values.sum()
                            survey.options.forEachIndexed { idx, opt ->
                                val votes = survey.results[idx.toString()] ?: 0
                                val percentage = if (totalVotes > 0) votes.toFloat() / totalVotes else 0f
                                val percentageText = "${(percentage * 100).toInt()}%"
                                
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("• $opt", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                        Text(
                                            text = "$votes votos ($percentageText)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = percentage,
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.surface
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text("Visibilidad:", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = survey.isActive, 
                                    onCheckedChange = { viewModel.updateSurvey(survey.id, mapOf("isActive" to it)) }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { editingSurvey = survey }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.deleteSurvey(survey.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    viewModel: AppViewModel, 
    onBack: () -> Unit, 
    onAddNew: () -> Unit,
    onLoginRedirect: () -> Unit
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val localConfigs by viewModel.localConfigs.collectAsState()
    val userSession by viewModel.userSession.collectAsState()
    val isAnonymous = userSession?.isAnonymous ?: true
    
    var showWarning by remember { mutableStateOf(false) }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text("Función Restringida") },
            text = {
                Text("Para poder publicar productos en La Güinera, debes iniciar sesión con una cuenta de Google.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWarning = false
                        onLoginRedirect()
                    }
                ) {
                    Text("Acceder con Google")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarning = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val myProducts = allProducts.filter { it.creatorId == viewModel.currentUserId }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Productos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isAnonymous) {
                        showWarning = true
                    } else {
                        onAddNew()
                    }
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(myProducts) { product ->
                val config = localConfigs.find { it.productId == product.id }
                var originalPriceStr by remember(config) { mutableStateOf(config?.originalPrice?.toString() ?: "") }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Bloque: ${product.blockName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Precio de venta: $${product.price} CUP", style = MaterialTheme.typography.bodyMedium)
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Cantidad: ${product.quantity}")
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { if (product.quantity > 0) viewModel.updateProductQuantity(product, product.quantity - 1) }) {
                                Icon(Icons.Filled.Remove, contentDescription = "Restar")
                            }
                            IconButton(onClick = { viewModel.updateProductQuantity(product, product.quantity + 1) }) {
                                Icon(Icons.Filled.Add, contentDescription = "Sumar")
                            }
                        }
                        
                        OutlinedTextField(
                            value = originalPriceStr,
                            onValueChange = { 
                                originalPriceStr = it
                                val newPrice = it.toDoubleOrNull()
                                if (newPrice != null) {
                                    viewModel.updateLocalOriginalPrice(product.id, newPrice)
                                }
                            },
                            label = { Text("Precio Original (Costo)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        val totalEarnings = product.price * product.quantity
                        Text("Ganancia Total Potencial: $totalEarnings CUP", fontWeight = FontWeight.SemiBold)
                        
                        val originalPrice = config?.originalPrice ?: 0.0
                        if (originalPrice > 0) {
                            val profit = (product.price - originalPrice) * product.quantity
                            Text("Beneficio Neto: $profit CUP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (myProducts.isEmpty()) {
                item {
                    Text("No has publicado ningún producto aún.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
