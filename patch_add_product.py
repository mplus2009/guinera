import re

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

old = """    fun addProduct(blockName: String, name: String, prices: Map<String, Double>, quantity: Int, description: String, imageUri: Uri?, onComplete: () -> Unit) {
        viewModelScope.launch {
            val imageUrl = imageUri?.let { repository.uploadImage(it) } ?: ""
            
            // Set the main price and currency for backward compatibility (pick the first one, default CUP)
            val mainPriceEntry = prices.entries.firstOrNull()
            val mainPrice = mainPriceEntry?.value ?: 0.0
            val mainCurrency = mainPriceEntry?.key ?: "CUP"
            
            repository.addProduct(
                Product(
                    blockName = blockName,
                    creatorId = currentUserId,
                    name = name,
                    price = mainPrice,
                    currency = mainCurrency,
                    prices = prices,
                    quantity = quantity,
                    description = description,
                    imageUrl = imageUrl
                )
            )
            onComplete()
        }
    }"""

new = """    fun addProduct(blockName: String, name: String, prices: Map<String, Double>, quantity: Int, description: String, imageUri: Uri?, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val uploadedUrl = imageUri?.let { repository.uploadImage(it) }
            if (imageUri != null && uploadedUrl == null) {
                onComplete(false, "No se pudo subir la imagen a Firebase Storage.")
                return@launch
            }
            
            val imageUrl = uploadedUrl ?: ""
            
            // Set the main price and currency for backward compatibility (pick the first one, default CUP)
            val mainPriceEntry = prices.entries.firstOrNull()
            val mainPrice = mainPriceEntry?.value ?: 0.0
            val mainCurrency = mainPriceEntry?.key ?: "CUP"
            
            repository.addProduct(
                Product(
                    blockName = blockName,
                    creatorId = currentUserId,
                    name = name,
                    price = mainPrice,
                    currency = mainCurrency,
                    prices = prices,
                    quantity = quantity,
                    description = description,
                    imageUrl = imageUrl
                )
            )
            onComplete(true, null)
        }
    }"""
content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)
