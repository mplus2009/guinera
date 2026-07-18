import re

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

# For addBusinessSpace
old_add = """    fun addBusinessSpace(brandName: String, description: String, phoneNumber: String, latitude: Double, longitude: Double, logoUri: Uri?, bannerUri: Uri?, onComplete: (String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val uploadedLogoUrl = logoUri?.let { repository.uploadImage(it) } ?: ""
                val uploadedBannerUrl = bannerUri?.let { repository.uploadImage(it) } ?: ""
                val space = BusinessSpace(
                    ownerId = currentUserId,
                    brandName = brandName,
                    description = description,
                    phoneNumber = phoneNumber,
                    latitude = latitude,
                    longitude = longitude,
                    logoUri = uploadedLogoUrl,
                    bannerUri = uploadedBannerUrl
                )
                repository.addBusinessSpace(space) { id ->
                    onComplete(id, null)
                }
            } catch (e: Exception) {
                onComplete(null, e.message)
            }
        }
    }"""

new_add = """    fun addBusinessSpace(brandName: String, description: String, phoneNumber: String, latitude: Double, longitude: Double, logoUri: Uri?, bannerUri: Uri?, onComplete: (String?, String?) -> Unit) {
        viewModelScope.launch {
            val uploadedLogoUrl = logoUri?.let { repository.uploadImage(it) }
            val uploadedBannerUrl = bannerUri?.let { repository.uploadImage(it) }
            
            if ((logoUri != null && uploadedLogoUrl == null) || (bannerUri != null && uploadedBannerUrl == null)) {
                onComplete(null, "Error al subir las imágenes. Revisa que Firebase Storage esté activado y tenga permisos correctos.")
                return@launch
            }
            
            val space = BusinessSpace(
                ownerId = currentUserId,
                brandName = brandName,
                description = description,
                phoneNumber = phoneNumber,
                latitude = latitude,
                longitude = longitude,
                logoUri = uploadedLogoUrl ?: "",
                bannerUri = uploadedBannerUrl ?: ""
            )
            repository.addBusinessSpace(space) { id ->
                onComplete(id, null)
            }
        }
    }"""
content = content.replace(old_add, new_add)

# For updateBusinessSpace
old_update = """    fun updateBusinessSpace(space: BusinessSpace, newLogoUri: Uri?, newBannerUri: Uri?, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val logoUrl = newLogoUri?.let { repository.uploadImage(it) } ?: space.logoUri
                val bannerUrl = newBannerUri?.let { repository.uploadImage(it) } ?: space.bannerUri
                val updatedSpace = space.copy(logoUri = logoUrl, bannerUri = bannerUrl)
                repository.updateBusinessSpace(updatedSpace) {
                    onComplete(true, null)
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }"""

new_update = """    fun updateBusinessSpace(space: BusinessSpace, newLogoUri: Uri?, newBannerUri: Uri?, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val uploadedLogo = newLogoUri?.let { repository.uploadImage(it) }
            val uploadedBanner = newBannerUri?.let { repository.uploadImage(it) }
            
            if ((newLogoUri != null && uploadedLogo == null) || (newBannerUri != null && uploadedBanner == null)) {
                onComplete(false, "Error al subir la imagen. Firebase Storage puede estar fallando.")
                return@launch
            }
            
            val logoUrl = uploadedLogo ?: space.logoUri
            val bannerUrl = uploadedBanner ?: space.bannerUri
            
            val updatedSpace = space.copy(logoUri = logoUrl, bannerUri = bannerUrl)
            repository.updateBusinessSpace(updatedSpace) {
                onComplete(true, null)
            }
        }
    }"""
content = content.replace(old_update, new_update)

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)
