import sys

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

bad_my_space = """        composable("my_space_map") {
            com.example.ui.screens.MySpaceMapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCreateSpace = { navController.navigate("create_business") },
                onManageBusinesses = { navController.navigate("manage_businesses") },
                onSpaceSelected = { spaceId -> navController.navigate("business_detail/$spaceId") }
            )
        }"""

good_my_space = """        composable("my_space_map") {
            com.example.ui.screens.MySpaceMapScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onCreateSpace = { navController.navigate("create_business") },
                onManageBusinesses = { navController.navigate("manage_businesses") },
                onSpaceSelected = { spaceId -> navController.navigate("business_detail/$spaceId") },
                onOpenBusinessChats = { navController.navigate("business_chats") }
            )
        }
        
        composable("business_chats") {
            com.example.ui.screens.BusinessChatsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenChat = { chatId -> navController.navigate("business_chat/$chatId") }
            )
        }
        
        composable(
            "business_chat/{chatId}",
            arguments = listOf(androidx.navigation.navArgument("chatId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            com.example.ui.screens.BusinessChatDetailScreen(
                chatId = chatId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }"""

content = content.replace(bad_my_space, good_my_space)

bad_business_detail = """        composable(
            "business_detail/{spaceId}",
            arguments = listOf(androidx.navigation.navArgument("spaceId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            com.example.ui.screens.BusinessDetailScreen(
                spaceId = spaceId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate("add_space_product/$spaceId") },
                onEditBusiness = { navController.navigate("edit_business/$spaceId") },
                onEditProduct = { productId -> navController.navigate("edit_space_product/$productId") }
            )
        }"""

good_business_detail = """        composable(
            "business_detail/{spaceId}",
            arguments = listOf(androidx.navigation.navArgument("spaceId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            com.example.ui.screens.BusinessDetailScreen(
                spaceId = spaceId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate("add_space_product/$spaceId") },
                onEditBusiness = { navController.navigate("edit_business/$spaceId") },
                onEditProduct = { productId -> navController.navigate("edit_space_product/$productId") },
                onContactOwner = { space -> 
                    val chatId = "${space.id}_${viewModel.currentUserId}"
                    navController.navigate("business_chat/$chatId") 
                }
            )
        }"""
        
content = content.replace(bad_business_detail, good_business_detail)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

