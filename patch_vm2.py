import sys

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

get_space_prod = """
    suspend fun getSpaceProduct(productId: String): SpaceProduct? {
        return repository.getSpaceProduct(productId)
    }
"""

if 'suspend fun getSpaceProduct' not in content:
    content = content.replace("fun getSpaceProducts(spaceId: String) = repository.getSpaceProducts(spaceId)", "fun getSpaceProducts(spaceId: String) = repository.getSpaceProducts(spaceId)\n" + get_space_prod)

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    screen_content = f.read()

screen_content = screen_content.replace("viewModel.repository.getSpaceProduct(productId)", "viewModel.getSpaceProduct(productId)")

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(screen_content)

