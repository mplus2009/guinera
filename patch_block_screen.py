with open('app/src/main/java/com/example/ui/screens/BlockScreen.kt', 'r') as f:
    content = f.read()

old = """                            Text(
                                text = formatter.format(Date(chat.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }"""

new = """                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = formatter.format(Date(chat.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (chat.isPending) Icons.Default.Schedule else Icons.Default.Check,
                                        contentDescription = if (chat.isPending) "Enviando" else "Enviado",
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }"""

content = content.replace('import androidx.compose.material.icons.filled.Warning', 'import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Schedule')

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/BlockScreen.kt', 'w') as f:
    f.write(content)
