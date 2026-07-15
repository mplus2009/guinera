import sys

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'r') as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.Filled.Edit", "androidx.compose.material.icons.filled.Edit")
content = content.replace("Icon(androidx.compose.material.icons.filled.Edit", "Icon(androidx.compose.material.icons.Icons.Filled.Edit")
content = content.replace("Icon(androidx.compose.material.icons.Icons.Filled.Edit", "Icon(androidx.compose.material.icons.Icons.Filled.Edit")

if "import androidx.compose.material.icons.filled.Edit" not in content:
    content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Edit")
    
content = content.replace("Icon(androidx.compose.material.icons.Icons.Filled.Edit", "Icon(Icons.Filled.Edit")

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'w') as f:
    f.write(content)
