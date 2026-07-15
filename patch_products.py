import sys

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.Share" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Edit", "import androidx.compose.material.icons.filled.Edit\nimport androidx.compose.material.icons.filled.Share")

new_list_row = """                val context = androidx.compose.ui.platform.LocalContext.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "la.g./space/product/${product.id}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir producto"))
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.primary)
                    }
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar Producto")
                        }
                    }
                }"""

old_list_row = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar Producto")
                        }
                    }
                }"""
content = content.replace(old_list_row, new_list_row)

new_grid_row = """                val context = androidx.compose.ui.platform.LocalContext.current
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "la.g./space/product/${product.id}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir producto"))
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Share, contentDescription = "Compartir", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar Producto", modifier = Modifier.size(16.dp))
                        }
                    }
                }"""

old_grid_row = """                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar Producto", modifier = Modifier.size(16.dp))
                        }
                    }
                }"""
content = content.replace(old_grid_row, new_grid_row)

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'w') as f:
    f.write(content)

