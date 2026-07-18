with open('app/src/main/java/com/example/data/AppRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'isPending = snapshot.metadata.hasPendingWrites }',
    'isPending = doc.metadata.hasPendingWrites() }'
)
content = content.replace(
    'it.toObject(BusinessMessage::class.java)?.apply { id = it.id; isPending = snapshot.metadata.hasPendingWrites }',
    'it.toObject(BusinessMessage::class.java)?.apply { id = it.id; isPending = it.metadata.hasPendingWrites() }'
)

with open('app/src/main/java/com/example/data/AppRepository.kt', 'w') as f:
    f.write(content)
