import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

# Replace the state variables
old_states = """    var showManualEmailFlow by remember { mutableStateOf(false) }
    var emailStep by remember { mutableStateOf(1) } // 1: Email, 2: Code, 3: Profile Info
    var emailInput by remember { mutableStateOf("") }
    var verificationCodeInput by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf("") }
    var showCodePopup by remember { mutableStateOf(false) }"""

new_states = """    var showManualEmailFlow by remember { mutableStateOf(false) }
    var emailStep by remember { mutableStateOf(1) } // 1: Email/Password, 2: Profile Info
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }"""

content = content.replace(old_states, new_states)

# Add FirebaseAuth import
if "import com.google.firebase.auth.FirebaseAuth" not in content:
    content = content.replace("import com.example.ui.screens.FirebaseUserResult", "import com.example.ui.screens.FirebaseUserResult\nimport com.google.firebase.auth.FirebaseAuth\nimport com.google.firebase.auth.UserProfileChangeRequest\nimport kotlinx.coroutines.tasks.await")

# Add Lock icon import
if "import androidx.compose.material.icons.filled.Lock" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.Lock\nimport androidx.compose.ui.text.input.PasswordVisualTransformation")

# Find the block where emailStep is handled
old_email_flow = """                        when (emailStep) {
                            1 -> {
                                Text(
                                    text = "Acceso con Correo",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Ingresa tu correo para recibir un código de verificación e iniciar tu registro de perfil.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Correo Electrónico") },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showManualEmailFlow = false },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Atrás")
                                    }

                                    Button(
                                        onClick = {
                                            if (emailInput.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                                                generatedCode = (1000..9999).random().toString()
                                                showCodePopup = true
                                            } else {
                                                android.widget.Toast.makeText(context, "Por favor, ingresa un correo válido.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Enviar código")
                                    }
                                }
                            }
                            2 -> {
                                Text(
                                    text = "Verificación de Correo",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Introduce el código de verificación de 4 dígitos enviado a:\n$emailInput",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = verificationCodeInput,
                                    onValueChange = { verificationCodeInput = it },
                                    label = { Text("Código de 4 dígitos") },
                                    leadingIcon = { Icon(Icons.Filled.Security, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { emailStep = 1 },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Atrás")
                                    }

                                    Button(
                                        onClick = {
                                            if (verificationCodeInput == generatedCode) {
                                                emailStep = 3
                                            } else {
                                                android.widget.Toast.makeText(context, "Código incorrecto. Revisa el código de simulación.", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Verificar")
                                    }
                                }
                            }
                            3 -> {"""
                            
new_email_flow = """                        when (emailStep) {
                            1 -> {
                                Text(
                                    text = "Acceso con Correo",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Ingresa tu correo y contraseña para iniciar sesión o registrarte con tu propia cuenta.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it; authError = null },
                                    label = { Text("Correo Electrónico") },
                                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it; authError = null },
                                    label = { Text("Contraseña") },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                )
                                
                                if (authError != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = authError!!,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showManualEmailFlow = false },
                                        modifier = Modifier.height(48.dp),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Text("Atrás")
                                    }

                                    if (isAuthenticating) {
                                        Box(modifier = Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (emailInput.isNotBlank() && passwordInput.length >= 6) {
                                                    scope.launch {
                                                        isAuthenticating = true
                                                        try {
                                                            val auth = FirebaseAuth.getInstance()
                                                            val result = auth.signInWithEmailAndPassword(emailInput, passwordInput).await()
                                                            if (result.user != null) {
                                                                val user = result.user!!
                                                                viewModel.loginWithEmail(
                                                                    displayName = user.displayName ?: "Vecino",
                                                                    email = user.email ?: emailInput,
                                                                    avatarUrl = user.photoUrl?.toString() ?: "",
                                                                    uID = user.uid,
                                                                    context = context
                                                                )
                                                                onLoginSuccess()
                                                            }
                                                        } catch (e: Exception) {
                                                            authError = "Error al iniciar sesión: ¿Contraseña incorrecta o cuenta no existe?"
                                                        } finally {
                                                            isAuthenticating = false
                                                        }
                                                    }
                                                } else {
                                                    authError = "El correo no puede estar vacío y la contraseña debe tener al menos 6 caracteres."
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Text("Iniciar sesión")
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                TextButton(
                                    onClick = { 
                                        if (emailInput.isNotBlank() && passwordInput.length >= 6) {
                                            emailStep = 2 
                                        } else {
                                            authError = "Ingresa tu correo y una contraseña de al menos 6 caracteres antes de continuar con el registro."
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("¿No tienes cuenta? Regístrate aquí")
                                }
                            }
                            2 -> {"""
                            
content = content.replace(old_email_flow, new_email_flow)

old_register = """                                                        viewModel.loginWithEmail(
                                                            displayName = "$firstNameInput $lastNameInput",
                                                            email = emailInput,
                                                            avatarUrl = finalAvatarUrl,
                                                            context = context
                                                        )"""
                                                        
new_register = """                                                        val auth = FirebaseAuth.getInstance()
                                                        val result = auth.createUserWithEmailAndPassword(emailInput, passwordInput).await()
                                                        val user = result.user
                                                        if (user != null) {
                                                            val profileUpdates = UserProfileChangeRequest.Builder()
                                                                .setDisplayName("$firstNameInput $lastNameInput")
                                                                .setPhotoUri(Uri.parse(finalAvatarUrl))
                                                                .build()
                                                            user.updateProfile(profileUpdates).await()
                                                            
                                                            viewModel.loginWithEmail(
                                                                displayName = "$firstNameInput $lastNameInput",
                                                                email = emailInput,
                                                                avatarUrl = finalAvatarUrl,
                                                                uID = user.uid,
                                                                context = context
                                                            )
                                                        } else {
                                                            throw Exception("No se pudo crear el usuario en Firebase.")
                                                        }"""
                                                        
content = content.replace(old_register, new_register)

# Remove the code popup
old_popup_start = "            if (showCodePopup) {"
old_popup_end = "            // Bottom Info/Footer"

start_idx = content.find(old_popup_start)
end_idx = content.find(old_popup_end)

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + content[end_idx:]

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(content)

