import sys

with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'r') as f:
    content = f.read()

bad_bubble_sig = """fun MessageBubble(message: BusinessMessage, isMe: Boolean, viewModel: AppViewModel, spaceId: String) {"""
good_bubble_sig = """fun MessageBubble(message: BusinessMessage, isMe: Boolean, spaceProducts: List<SpaceProduct>) {"""

content = content.replace(bad_bubble_sig, good_bubble_sig)

bad_bubble_body = """                if (message.attachedProductId.isNotBlank()) {
                    val products by remember { viewModel.getSpaceProducts(spaceId) }.collectAsState(initial = emptyList())
                    val product = products.find { it.id == message.attachedProductId }"""

good_bubble_body = """                if (message.attachedProductId.isNotBlank()) {
                    val product = spaceProducts.find { it.id == message.attachedProductId }"""
                    
content = content.replace(bad_bubble_body, good_bubble_body)

bad_bubble_call = """            items(messages.reversed()) { msg ->
                MessageBubble(message = msg, isMe = msg.senderId == currentUserId, viewModel = viewModel, spaceId = chat?.spaceId ?: "")
            }"""
            
good_bubble_call = """            val spaceProducts by remember(chat?.spaceId) { 
                if (chat?.spaceId != null) viewModel.getSpaceProducts(chat.spaceId) 
                else kotlinx.coroutines.flow.flowOf(emptyList()) 
            }.collectAsState(initial = emptyList())
            items(messages.reversed()) { msg ->
                MessageBubble(message = msg, isMe = msg.senderId == currentUserId, spaceProducts = spaceProducts)
            }"""
            
content = content.replace(bad_bubble_call, good_bubble_call)

with open('app/src/main/java/com/example/ui/screens/BusinessChatScreens.kt', 'w') as f:
    f.write(content)

