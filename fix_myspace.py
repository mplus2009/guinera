import re
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

old = """fun AddSpaceProductScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }"""

new = """fun AddSpaceProductScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf("") }"""
content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)
