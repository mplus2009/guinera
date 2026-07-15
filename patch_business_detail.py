import sys

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

bad_sig = """fun BusinessDetailScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditBusiness: () -> Unit,
    onEditProduct: (String) -> Unit
) {"""

good_sig = """fun BusinessDetailScreen(
    spaceId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onAddProduct: () -> Unit,
    onEditBusiness: () -> Unit,
    onEditProduct: (String) -> Unit,
    onContactOwner: (BusinessSpace) -> Unit = {}
) {"""

content = content.replace(bad_sig, good_sig)

bad_owner_action = """                    if (space.ownerId == currentUserId) {
                        OutlinedButton(onClick = onEditBusiness) {
                            Text("Editar Negocio")
                        }
                    }
                }
            }"""

good_owner_action = """                    if (space.ownerId == currentUserId) {
                        OutlinedButton(onClick = onEditBusiness) {
                            Text("Editar Negocio")
                        }
                    } else {
                        Button(onClick = { onContactOwner(space) }) {
                            Icon(androidx.compose.material.icons.Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Contactar al Dueño")
                        }
                    }
                }
            }"""

content = content.replace(bad_owner_action, good_owner_action)

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)

