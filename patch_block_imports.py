with open('app/src/main/java/com/example/ui/screens/BlockScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.material.icons.Icons', 'import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Schedule')

with open('app/src/main/java/com/example/ui/screens/BlockScreen.kt', 'w') as f:
    f.write(content)
