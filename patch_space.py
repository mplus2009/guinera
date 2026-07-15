import sys

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.Share" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Delete", "import androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Share")

new_row = """                val context = androidx.compose.ui.platform.LocalContext.current
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(space.brandName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "la.g./space/${space.id}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir negocio"))
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Share, contentDescription = "Compartir Negocio", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (space.ownerId == currentUserId) {
                    OutlinedButton(onClick = onEditBusiness) {
                        Text("Editar Negocio")
                    }
                }"""

old_row = """                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(space.brandName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (space.ownerId == currentUserId) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onEditBusiness) {
                            Text("Editar Negocio")
                        }
                    }
                }"""

content = content.replace(old_row, new_row)

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)

