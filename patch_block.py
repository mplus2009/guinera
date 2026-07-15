import sys

with open('app/src/main/java/com/example/ui/screens/BlockScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Share")

new_row = """                val context = androidx.compose.ui.platform.LocalContext.current
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "la.g./winery/product/${product.id}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir producto"))
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (product.creatorId == currentUserId) {
                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }"""

old_row = """                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                }"""

content = content.replace(old_row, new_row)

with open('app/src/main/java/com/example/ui/screens/BlockScreen.kt', 'w') as f:
    f.write(content)

