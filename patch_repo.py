import sys

with open('app/src/main/java/com/example/data/AppRepository.kt', 'r') as f:
    content = f.read()

get_prod = """
    suspend fun getSpaceProduct(productId: String): SpaceProduct? {
        if (firestore == null) return null
        return try {
            firestore.collection("space_products").document(productId).get().await().toObject(SpaceProduct::class.java)
        } catch (e: Exception) { null }
    }
"""

if 'suspend fun getSpaceProduct' not in content:
    content = content.replace("fun updateSpaceProduct(product: SpaceProduct) {", get_prod + "\n    fun updateSpaceProduct(product: SpaceProduct) {")

with open('app/src/main/java/com/example/data/AppRepository.kt', 'w') as f:
    f.write(content)
