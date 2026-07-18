with open('app/src/main/java/com/example/ui/screens/AddProductScreen.kt', 'r') as f:
    content = f.read()

old = """fun AddProductScreen(
    blockName: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var selectedBlock by remember { mutableStateOf(if (blockName == "none") "Bloque 1" else blockName) }"""

new = """fun AddProductScreen(
    blockName: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedBlock by remember { mutableStateOf(if (blockName == "none") "Bloque 1" else blockName) }"""

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/AddProductScreen.kt', 'w') as f:
    f.write(content)
