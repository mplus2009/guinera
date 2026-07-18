import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

# I will find the block starting with "when (emailStep) {"
# And replacing everything up to "2 -> {"

start_str = "                        when (emailStep) {"
end_str = "                            2 -> {\n                                Text(\n                                    text = \"Completa tu Perfil\","

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    new_flow = """                        when (emailStep) {
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
"""
    content = content[:start_idx] + new_flow + content[end_idx:]
else:
    print("Could not find start/end")

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(content)
