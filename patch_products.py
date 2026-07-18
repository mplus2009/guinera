import re

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

old_add = """    fun addSpaceProduct(spaceId: String, name: String, description: String, price: Double, currency: String, imageUris: List<Uri>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val imageUrls = imageUris.mapNotNull { repository.uploadImage(it) }
            repository.addSpaceProduct(
                SpaceProduct(
                    spaceId = spaceId,
                    name = name,
                    description = description,
                    price = price,
                    currency = currency,
                    imageUrls = imageUrls
                )
            )
            onComplete()
        }
    }"""

new_add = """    fun addSpaceProduct(spaceId: String, name: String, description: String, price: Double, currency: String, imageUris: List<Uri>, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val imageUrls = imageUris.mapNotNull { repository.uploadImage(it) }
            if (imageUris.isNotEmpty() && imageUrls.isEmpty()) {
                onComplete(false, "No se pudieron subir las imágenes a Firebase Storage.")
                return@launch
            }
            repository.addSpaceProduct(
                SpaceProduct(
                    spaceId = spaceId,
                    name = name,
                    description = description,
                    price = price,
                    currency = currency,
                    imageUrls = imageUrls
                )
            )
            onComplete(true, null)
        }
    }"""
content = content.replace(old_add, new_add)

old_upd1 = """    fun updateSpaceProduct(product: SpaceProduct, newImageUris: List<Uri>, onComplete: () -> Unit) {
        viewModelScope.launch {
            val additionalImageUrls = newImageUris.mapNotNull { repository.uploadImage(it) }
            val updatedProduct = product.copy(imageUrls = product.imageUrls + additionalImageUrls)
            repository.updateSpaceProduct(updatedProduct)
            onComplete()
        }
    }"""

new_upd1 = """    fun updateSpaceProduct(product: SpaceProduct, newImageUris: List<Uri>, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val additionalImageUrls = newImageUris.mapNotNull { repository.uploadImage(it) }
            if (newImageUris.isNotEmpty() && additionalImageUrls.isEmpty()) {
                onComplete(false, "No se pudieron subir las imágenes a Firebase Storage.")
                return@launch
            }
            val updatedProduct = product.copy(imageUrls = product.imageUrls + additionalImageUrls)
            repository.updateSpaceProduct(updatedProduct)
            onComplete(true, null)
        }
    }"""
content = content.replace(old_upd1, new_upd1)

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)
