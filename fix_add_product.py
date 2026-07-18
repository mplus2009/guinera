import re
with open('app/src/main/java/com/example/ui/screens/AddProductScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.ui.unit.dp', 'import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.platform.LocalContext')

old = """fun AddProductScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val userSession by viewModel.userSession.collectAsState()"""

new = """fun AddProductScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userSession by viewModel.userSession.collectAsState()"""
content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/AddProductScreen.kt', 'w') as f:
    f.write(content)
