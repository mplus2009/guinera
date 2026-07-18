with open('app/src/main/java/com/example/ui/screens/P2PChatScreen.kt', 'r') as f:
    content = f.read()

old = """        // Timestamp text
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateString = sdf.format(Date(msg.timestamp))
        Text(
            text = dateString,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}"""

new = """        // Timestamp text
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateString = sdf.format(Date(msg.timestamp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (isOutgoing) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (msg.isPending) Icons.Default.Schedule else Icons.Default.Check,
                    contentDescription = if (msg.isPending) "Enviando" else "Enviado",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}"""

content = content.replace('import androidx.compose.material.icons.filled.ChatBubbleOutline', 'import androidx.compose.material.icons.filled.ChatBubbleOutline\nimport androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Schedule')

content = content.replace(old, new)
with open('app/src/main/java/com/example/ui/screens/P2PChatScreen.kt', 'w') as f:
    f.write(content)
