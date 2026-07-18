with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

new_fun = """    fun sendBusinessImageMessage(chat: BusinessChat, imageUri: Uri, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val uploadedUrl = repository.uploadImage(imageUri)
            if (uploadedUrl == null) {
                onComplete(false, "No se pudo subir la imagen. Falta el API Key de ImgBB en Secrets.")
                return@launch
            }
            sendBusinessMessageExisting(chat, "", imageUrl = uploadedUrl)
            onComplete(true, null)
        }
    }"""

content = content.replace('fun sendBusinessMessageExisting', new_fun + '\n\n    fun sendBusinessMessageExisting')

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)
