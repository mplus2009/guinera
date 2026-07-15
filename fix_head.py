import sys

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.material.icons.filled.Delete\nimport coil.imageLoader\npackage com.example.ui.screens", "package com.example.ui.screens\nimport androidx.compose.material.icons.filled.Delete\nimport coil.imageLoader")
content = content.replace("package com.example.ui.screens\npackage com.example.ui.screens", "package com.example.ui.screens")

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'r') as f:
    p_content = f.read()

p_content = p_content.replace('Icon(Icons.Filled.Edit', 'Icon(androidx.compose.material.icons.Icons.Filled.Edit')

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'w') as f:
    f.write(p_content)
