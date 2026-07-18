import re

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

old = """    fun addBusinessSpace(brandName: String, description: String, phoneNumber: String, latitude: Double, longitude: Double, logoUri: Uri?, bannerUri: Uri?, onComplete: (String) -> Unit) {
        viewModelScope.launch {
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
            repository.addBusinessSpace(space, onComplete)
        }
    }"""

new = """    fun addBusinessSpace(brandName: String, description: String, phoneNumber: String, latitude: Double, longitude: Double, logoUri: Uri?, bannerUri: Uri?, onComplete: (String?, String?) -> Unit) {
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

content = content.replace(old, new)

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)
