import sys

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'r') as f:
    content = f.read()

content = content.replace("fun ProductListItem(product: SpaceProduct, modifier: Modifier = Modifier)", "fun ProductListItem(product: SpaceProduct, modifier: Modifier = Modifier, onEdit: (() -> Unit)? = null)")

content = content.replace(
"""                Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(""",
"""                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Edit, contentDescription = "Editar Producto")
                        }
                    }
                }
                Surface("""
)

content = content.replace("fun ProductGridItem(product: SpaceProduct, modifier: Modifier = Modifier)", "fun ProductGridItem(product: SpaceProduct, modifier: Modifier = Modifier, onEdit: (() -> Unit)? = null)")

content = content.replace(
"""            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = product.name, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )""",
"""            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                            Icon(androidx.compose.material.icons.Icons.Filled.Edit, contentDescription = "Editar Producto", modifier = Modifier.size(16.dp))
                        }
                    }
                }"""
)

if 'androidx.compose.material.icons.filled.Edit' not in content:
    content = content.replace('import androidx.compose.material.icons.Icons', 'import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Edit')

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'w') as f:
    f.write(content)

