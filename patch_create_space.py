with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

old = """                            viewModel.addBusinessSpace(
                                brandName, 
                                description, 
                                phoneNumber, 
                                selectedLocation!!.latitude, 
                                selectedLocation!!.longitude,
                                logoUri,
                                null
                            ) {
                                isLoading = false
                                onBack()
                            }"""

new = """                            viewModel.addBusinessSpace(
                                brandName, 
                                description, 
                                phoneNumber, 
                                selectedLocation!!.latitude, 
                                selectedLocation!!.longitude,
                                logoUri,
                                null
                            ) { id, error ->
                                isLoading = false
                                if (id != null) {
                                    android.widget.Toast.makeText(context, "Espacio creado", android.widget.Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    android.widget.Toast.makeText(context, "Error: ${error ?: "Error al crear"}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }"""

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)
