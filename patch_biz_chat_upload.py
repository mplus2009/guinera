with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.BusinessChat"""
content = content.replace('import com.example.data.BusinessChat', imports)

old_vars = """    var showAttachDialog by remember { mutableStateOf(false) }
    var showCatalogDialog by remember { mutableStateOf(false) }"""

new_vars = """    val context = LocalContext.current
    var showAttachDialog by remember { mutableStateOf(false) }
    var showCatalogDialog by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && chat != null) {
            isUploadingImage = true
            viewModel.sendBusinessImageMessage(chat!!, uri) { success, error ->
                isUploadingImage = false
                if (success) {
                    // Success is silent for messages, user sees it in chat
                } else {
                    android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }"""
content = content.replace(old_vars, new_vars)

old_attach = """                ListItem(
                    headlineContent = { Text("Fotos y Videos") },
                    leadingContent = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable { showAttachDialog = false /* TODO */ }
                )"""

new_attach = """                ListItem(
                    headlineContent = { Text("Fotos y Videos") },
                    leadingContent = { Icon(Icons.Filled.CameraAlt, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showAttachDialog = false
                        imagePickerLauncher.launch("image/*") 
                    }
                )"""
content = content.replace(old_attach, new_attach)

old_send = """                    if (messageText.isNotBlank()) {
                        FloatingActionButton(
                            onClick = {"""

new_send = """                    if (isUploadingImage) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp), color = MaterialTheme.colorScheme.primary)
                    } else if (messageText.isNotBlank()) {
                        FloatingActionButton(
                            onClick = {"""
content = content.replace(old_send, new_send)

with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'w') as f:
    f.write(content)
