with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

old = """                        viewModel.updateBusinessSpace(space, logoUri, bannerUri) {
                            isLoading = false
                            onBack()
                        }"""
                        
new = """                        viewModel.updateBusinessSpace(space, logoUri, bannerUri) { success, error ->
                            isLoading = false
                            if (success) {
                                android.widget.Toast.makeText(context, "Espacio actualizado", android.widget.Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                android.widget.Toast.makeText(context, "Error: ${error ?: "No se pudo subir la imagen"}", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }"""

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)
