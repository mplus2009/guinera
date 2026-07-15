import sys

with open('app/src/main/java/com/example/data/AppRepository.kt', 'r') as f:
    content = f.read()

if 'import kotlinx.coroutines.tasks.await' not in content:
    content = content.replace('import kotlinx.coroutines.flow.flow', 'import kotlinx.coroutines.flow.flow\nimport kotlinx.coroutines.tasks.await')

update_space = """
    fun updateBusinessSpace(space: BusinessSpace, onComplete: () -> Unit) {
        if (firestore == null) return
        firestore.collection("business_spaces").document(space.id).set(space).addOnSuccessListener { onComplete() }
    }
"""

if 'fun updateBusinessSpace' not in content:
    content = content.replace('fun addBusinessSpace(space: BusinessSpace, onComplete: (String) -> Unit) {\n        if (firestore == null) return\n        val docRef = firestore.collection("business_spaces").document()\n        space.id = docRef.id\n        docRef.set(space).addOnSuccessListener { onComplete(docRef.id) }\n    }', 'fun addBusinessSpace(space: BusinessSpace, onComplete: (String) -> Unit) {\n        if (firestore == null) return\n        val docRef = firestore.collection("business_spaces").document()\n        space.id = docRef.id\n        docRef.set(space).addOnSuccessListener { onComplete(docRef.id) }\n    }\n' + update_space)

update_prod = """
    fun updateSpaceProduct(product: SpaceProduct) {
        if (firestore == null) return
        firestore.collection("space_products").document(product.id).set(product)
    }

    fun deleteSpaceProduct(productId: String) {
        if (firestore == null) return
        firestore.collection("space_products").document(productId).delete()
    }
"""
if 'fun updateSpaceProduct' not in content:
    content = content.replace('fun addSpaceProduct(product: SpaceProduct) {\n        if (firestore == null) return\n        val docRef = firestore.collection("space_products").document()\n        product.id = docRef.id\n        docRef.set(product)\n    }', 'fun addSpaceProduct(product: SpaceProduct) {\n        if (firestore == null) return\n        val docRef = firestore.collection("space_products").document()\n        product.id = docRef.id\n        docRef.set(product)\n    }\n' + update_prod)

with open('app/src/main/java/com/example/data/AppRepository.kt', 'w') as f:
    f.write(content)

