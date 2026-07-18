with open('app/src/main/java/com/example/data/AppRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'doc.toObject(ChatMessage::class.java)?.apply { id = doc.id; isPending = it.metadata.hasPendingWrites() }',
    'doc.toObject(ChatMessage::class.java)?.apply { id = doc.id; isPending = doc.metadata.hasPendingWrites() }'
)

with open('app/src/main/java/com/example/data/AppRepository.kt', 'w') as f:
    f.write(content)
