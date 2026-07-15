import sys

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_route = """        composable(
            route = "business_detail/{spaceId}",
            arguments = listOf(androidx.navigation.navArgument("spaceId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            com.example.ui.screens.BusinessDetailScreen(
                spaceId = spaceId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate("add_space_product/$spaceId") }
            )
        }"""

new_route = """        composable(
            route = "business_detail/{spaceId}",
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
        }

        composable(
            route = "edit_business/{spaceId}",
            arguments = listOf(androidx.navigation.navArgument("spaceId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val spaceId = backStackEntry.arguments?.getString("spaceId") ?: return@composable
            com.example.ui.screens.EditBusinessScreen(
                spaceId = spaceId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit_space_product/{productId}",
            arguments = listOf(androidx.navigation.navArgument("productId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            com.example.ui.screens.EditSpaceProductScreen(
                productId = productId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }"""

content = content.replace(old_route, new_route)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

