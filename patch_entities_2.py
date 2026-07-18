with open('app/src/main/java/com/example/data/Entities.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'var bannerUri: String = "",\n    var timestamp: Long = System.currentTimeMillis()\n)',
    'var bannerUri: String = "",\n    var timestamp: Long = System.currentTimeMillis(),\n    var isPending: Boolean = false\n)'
)

with open('app/src/main/java/com/example/data/Entities.kt', 'w') as f:
    f.write(content)
