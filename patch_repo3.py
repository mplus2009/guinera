with open('app/src/main/java/com/example/data/AppRepository.kt', 'r') as f:
    content = f.read()

# For chats (Community)
content = content.replace(
    'doc.toObject(ChatMessage::class.java)?.apply { id = doc.id }',
    'doc.toObject(ChatMessage::class.java)?.apply { id = doc.id; isPending = snapshot.metadata.hasPendingWrites }'
)

# For messages (BusinessChat)
content = content.replace(
    'doc.toObject(BusinessMessage::class.java)?.apply { id = doc.id }',
    'doc.toObject(BusinessMessage::class.java)?.apply { id = doc.id; isPending = snapshot.metadata.hasPendingWrites }'
)

with open('app/src/main/java/com/example/data/AppRepository.kt', 'w') as f:
    f.write(content)
