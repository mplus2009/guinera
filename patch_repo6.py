with open('app/src/main/java/com/example/data/AppRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'isPending = doc.metadata.hasPendingWrites()',
    'isPending = it.metadata.hasPendingWrites()'
)

with open('app/src/main/java/com/example/data/AppRepository.kt', 'w') as f:
    f.write(content)
