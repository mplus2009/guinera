with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'r') as f:
    content = f.read()

old = """                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}"""

new = """                if (message.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = message.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                ) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isPending) Icons.Default.Schedule else Icons.Default.Check,
                            contentDescription = if (message.isPending) "Enviando" else "Enviado",
                            modifier = Modifier.size(12.dp),
                            tint = if (message.isPending) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else androidx.compose.ui.graphics.Color(0xFF2196F3)
                        )
                    }
                }
            }
        }
    }
}"""

content = content.replace('import androidx.compose.material.icons.filled.Send', 'import androidx.compose.material.icons.filled.Send\nimport androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Schedule')

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'w') as f:
    f.write(content)
