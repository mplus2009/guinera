import sys

with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'r') as f:
    content = f.read()

bad_block = """    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            val spaceProducts by remember(chat?.spaceId) { 
                if (chat?.spaceId != null) viewModel.getSpaceProducts(chat.spaceId) 
                else kotlinx.coroutines.flow.flowOf(emptyList()) 
            }.collectAsState(initial = emptyList())
            items(messages.reversed()) { msg ->
                MessageBubble(message = msg, isMe = msg.senderId == currentUserId, spaceProducts = spaceProducts)
            }
        }
    }"""

good_block = """    ) { padding ->
        val spaceProducts by remember(chat?.spaceId) { 
            if (chat?.spaceId != null) viewModel.getSpaceProducts(chat.spaceId) 
            else kotlinx.coroutines.flow.flowOf(emptyList()) 
        }.collectAsState(initial = emptyList())
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                MessageBubble(message = msg, isMe = msg.senderId == currentUserId, spaceProducts = spaceProducts)
            }
        }
    }"""

content = content.replace(bad_block, good_block)

with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'w') as f:
    f.write(content)

