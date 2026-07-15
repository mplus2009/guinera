import sys

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

old_fun_sig = """fun BusinessDetailScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddProduct: () -> Unit
)"""
new_fun_sig = """fun BusinessDetailScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditBusiness: () -> Unit,
    onEditProduct: (String) -> Unit
)"""
content = content.replace(old_fun_sig, new_fun_sig)

old_header = """            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (space.logoUri.isNotEmpty()) {
                        AsyncImage(
                            model = space.logoUri,
                            contentDescription = "Logo",
                            modifier = Modifier.size(120.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(120.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(64.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(space.brandName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }"""

new_header = """            item {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    if (space.bannerUri.isNotEmpty()) {
                        AsyncImage(
                            model = space.bannerUri,
                            contentDescription = "Banner",
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.secondaryContainer)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = 10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        if (space.logoUri.isNotEmpty()) {
                            AsyncImage(
                                model = space.logoUri,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(64.dp))
                            }
                        }
                    }
                }
                
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(space.brandName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (space.ownerId == currentUserId) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onEditBusiness) {
                            Text("Editar Negocio")
                        }
                    }
                }
            }"""

content = content.replace(old_header, new_header)

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)
