import sys

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

bad_sig = """fun MySpaceMapScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onCreateSpace: () -> Unit,
    onManageBusinesses: () -> Unit,
    onSpaceSelected: (String) -> Unit
) {"""
good_sig = """fun MySpaceMapScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onCreateSpace: () -> Unit,
    onManageBusinesses: () -> Unit,
    onSpaceSelected: (String) -> Unit,
    onOpenBusinessChats: () -> Unit = {}
) {"""

content = content.replace(bad_sig, good_sig)

bad_top = """                title = { Text("Mi Espacio - Mapa de Cuba") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )"""
good_top = """                title = { Text("Mi Espacio - Mapa de Cuba") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenBusinessChats) {
                        Icon(androidx.compose.material.icons.Icons.Filled.Chat, contentDescription = "Chats de Negocios")
                    }
                }
            )"""

content = content.replace(bad_top, good_top)

if "import androidx.compose.material.icons.filled.Chat" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Store", "import androidx.compose.material.icons.filled.Store\nimport androidx.compose.material.icons.filled.Chat")

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)

