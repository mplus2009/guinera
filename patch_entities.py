with open('app/src/main/java/com/example/data/Entities.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'var logoUri: String = "",\n    var timestamp: Long = System.currentTimeMillis()\n) {',
    'var logoUri: String = "",\n    var timestamp: Long = System.currentTimeMillis(),\n    var isPending: Boolean = false\n) {'
)

content = content.replace(
    'var attachedProductId: String = "",\n    var timestamp: Long = System.currentTimeMillis()\n)',
    'var attachedProductId: String = "",\n    var timestamp: Long = System.currentTimeMillis(),\n    var isPending: Boolean = false\n)'
)

content = content.replace(
    'val duration: Int = 0, // for call duration or audio message duration\n    val timestamp: Long = System.currentTimeMillis()\n)',
    'val duration: Int = 0, // for call duration or audio message duration\n    val timestamp: Long = System.currentTimeMillis(),\n    val isPending: Boolean = false\n)'
)

with open('app/src/main/java/com/example/data/Entities.kt', 'w') as f:
    f.write(content)
