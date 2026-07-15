with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

bad_block = """                    onClick = {
                        if (brandName.isNotBlank() && selectedLocation != null) {
                            isLoading = true
                            viewModel.addBusinessSpace(
                                brandName, 
                                description, 
                                phoneNumber, 
                                selectedLocation!!.latitude, 
                                selectedLocation!!.longitude,
                                logoUri,
                                null
                                logoUri
                                isLoading = false
                                onBack()
                            }
                        } else if (selectedLocation == null) {"""

good_block = """                    onClick = {
                        if (brandName.isNotBlank() && selectedLocation != null) {
                            isLoading = true
                            viewModel.addBusinessSpace(
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
                            }
                        } else if (selectedLocation == null) {"""

content = content.replace(bad_block, good_block)
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)
