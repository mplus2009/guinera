with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

old = """                        viewModel.addSpaceProduct(spaceId, name, description, price, currency, imageUris) {
                            isUploading = false
                            onBack()
                        }"""

new = """                        viewModel.addSpaceProduct(spaceId, name, description, price, currency, imageUris) { success, error ->
                            isUploading = false
                            if (success) {
                                android.widget.Toast.makeText(context, "Producto añadido", android.widget.Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                android.widget.Toast.makeText(context, "Error: ${error}", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }"""

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)
