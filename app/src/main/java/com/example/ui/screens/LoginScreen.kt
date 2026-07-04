package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.ui.AppViewModel

@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var showAccountChooser by remember { mutableStateOf(false) }
    var showCustomAccountDialog by remember { mutableStateOf(false) }
    var pendingGoogleAccount by remember { mutableStateOf<GoogleAccountSim?>(null) }
    
    // Custom Account Form States
    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }
    var customError by remember { mutableStateOf<String?>(null) }
    var customUID by remember { mutableStateOf("") }
    var loginUID by remember { mutableStateOf("") }
    var showAnonUidDialog by remember { mutableStateOf(false) }
    var anonUID by remember { mutableStateOf("") }

    val predefinedAccounts = listOf(
        GoogleAccountSim("Carmen Valdés", "carmen.v@gmail.com", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150", "carmen_valdes"),
        GoogleAccountSim("Maikel Pérez", "maikel.p@gmail.com", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150", "maikel_perez"),
        GoogleAccountSim("Alejandro Gómez", "alejandro.g@gmail.com", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150", "alejandro_gomez")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 48.dp)
            ) {
                // Beautiful brand logo from generated asset
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_app_logo),
                    contentDescription = "Logo de La Güinera",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(24.dp))
                        .padding(0.dp),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "La Güinera",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Red comunitaria de comercio y vecinos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Middle Action Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Elige cómo unirte",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Iniciar con Google te da acceso completo a publicar productos, encuestas y comentarios.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Google Sign-In Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .clickable { showAccountChooser = true },
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // High-quality Custom SVG/Vector-like Google 'G' Icon built with shapes and custom color text
                            Text(
                                text = "G",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(
                                text = "Iniciar sesión con Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF757575)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Anonymous Sign-In Button
                    OutlinedButton(
                        onClick = {
                            anonUID = "anon_${java.util.UUID.randomUUID().toString().take(4)}"
                            showAnonUidDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Entrar de forma anónima",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Acceso anónimo restringido",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Bottom Info/Footer
            Text(
                text = "La Güinera se compromete a proteger tu privacidad y fomentar el comercio local honesto.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }

    // Google Account Chooser Simulated Native Dialog
    if (showAccountChooser) {
        Dialog(onDismissRequest = { showAccountChooser = false }) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Google Branding
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Elige una cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "para continuar a La Güinera",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // List of predefined neighborhood simulation accounts
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        predefinedAccounts.forEach { account ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        pendingGoogleAccount = account
                                        loginUID = account.suggestedUid
                                        showAccountChooser = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = account.avatarUrl,
                                    contentDescription = account.name,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = account.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = account.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Add Custom/New Account button to enter custom details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showAccountChooser = false
                                    showCustomAccountDialog = true
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Usar otra cuenta...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    // Custom Account input dialog
    if (showCustomAccountDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAccountDialog = false },
            title = { Text("Iniciar con Google") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Configura tu cuenta de Google simulada para interactuar en la comunidad.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nombre y Apellidos") },
                        placeholder = { Text("Ej: Maikel Pérez") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Correo Electrónico Google") },
                        placeholder = { Text("ejemplo@gmail.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customUID,
                        onValueChange = { customUID = it.lowercase().replace(" ", "_").replace("@", "") },
                        label = { Text("Nombre Único uID (@usuario)") },
                        placeholder = { Text("ej: maikel_p") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (customError != null) {
                        Text(
                            text = customError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isBlank()) {
                            customError = "Por favor ingresa un nombre"
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(customEmail).matches()) {
                            customError = "Ingresa un correo de Google válido"
                        } else if (customUID.trim().isBlank()) {
                            customError = "Por favor ingresa tu uID único (@usuario)"
                        } else {
                            pendingGoogleAccount = GoogleAccountSim(
                                name = customName.trim(),
                                email = customEmail.trim().lowercase(),
                                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                                suggestedUid = customUID.trim().lowercase()
                            )
                            loginUID = customUID.trim().lowercase()
                            showCustomAccountDialog = false
                        }
                    }
                ) {
                    Text("Acceder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAccountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Custom Google permissions consent prompt dialog
    pendingGoogleAccount?.let { pending ->
        Dialog(
            onDismissRequest = { pendingGoogleAccount = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Google Brand Colored text
                        Row(
                            modifier = Modifier.padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 28.sp)
                            Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 28.sp)
                            Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Black, fontSize = 28.sp)
                            Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 28.sp)
                            Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Black, fontSize = 28.sp)
                            Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 28.sp)
                        }

                        Text(
                            text = "Solicitud de Acceso",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "La Güinera solicita conectarse con tu cuenta de Google",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // User profile info box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = pending.avatarUrl,
                                    contentDescription = pending.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = pending.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = pending.email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Permissions list card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Para continuar, La Güinera te solicita los siguientes accesos:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Item 1: Profile picture
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Acceso a tu foto de perfil",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Se usará para identificarte de forma amigable frente a tus vecinos.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Item 2: Name and surname (or just name)
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Tu nombre y apellidos, o solo tu nombre",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Se utilizará como tu identidad visible en tus comentarios y productos del barrio.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Item 3: Email address
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Tu dirección de correo electrónico",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Se registrará para la autenticación única y segura de tu cuenta comunitaria.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Unique Username uID Input in Google Login Flow
                        OutlinedTextField(
                            value = loginUID,
                            onValueChange = { loginUID = it.lowercase().replace(" ", "_").replace("@", "") },
                            label = { Text("Crea tu uID único (@usuario)") },
                            placeholder = { Text("ej: carmen_v") },
                            singleLine = true,
                            leadingIcon = { Text("@", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp)) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { pendingGoogleAccount = null },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancelar", style = MaterialTheme.typography.bodyLarge)
                            }

                            Button(
                                onClick = {
                                    if (loginUID.trim().isBlank()) {
                                        android.widget.Toast.makeText(context, "Por favor ingresa tu uID único", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.loginWithGoogle(
                                            displayName = pending.name,
                                            email = pending.email,
                                            avatarUrl = pending.avatarUrl,
                                            uID = loginUID.trim().lowercase(),
                                            context = context
                                        )
                                        pendingGoogleAccount = null
                                        onLoginSuccess()
                                    }
                                },
                                modifier = Modifier.weight(1.5f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Aceptar y Permitir", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAnonUidDialog) {
        AlertDialog(
            onDismissRequest = { showAnonUidDialog = false },
            title = { Text("Configura tu Identidad Anónima") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Para poder usar la zona de chats directos, necesitas crear un nombre único (@usuario).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = anonUID,
                        onValueChange = { anonUID = it.lowercase().replace(" ", "_").replace("@", "") },
                        label = { Text("Tu uID único (@usuario)") },
                        placeholder = { Text("ej: anonimo_vecino") },
                        singleLine = true,
                        leadingIcon = { Text("@", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (anonUID.trim().isBlank()) {
                            android.widget.Toast.makeText(context, "Por favor ingresa tu uID único", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.loginAnonymously(anonUID.trim().lowercase(), context)
                            showAnonUidDialog = false
                            onLoginSuccess()
                        }
                    }
                ) {
                    Text("Entrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnonUidDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

data class GoogleAccountSim(
    val name: String,
    val email: String,
    val avatarUrl: String,
    val suggestedUid: String = ""
)
