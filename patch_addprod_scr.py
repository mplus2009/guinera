with open('app/src/main/java/com/example/ui/screens/AddProductScreen.kt', 'r') as f:
    content = f.read()

old = """                        viewModel.addProduct(selectedBlock, name, prices, qty, description, imageUri) {
                            onBack()
                        }"""

new = """                        viewModel.addProduct(selectedBlock, name, prices, qty, description, imageUri) { success, error ->
                            isUploading = false
                            if (success) {
                                android.widget.Toast.makeText(context, "Publicado", android.widget.Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }"""

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/AddProductScreen.kt', 'w') as f:
    f.write(content)
