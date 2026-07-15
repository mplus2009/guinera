with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'selectedLocation!!.longitude, logoUri, null' in line:
        lines[i] = '                                selectedLocation!!.longitude,\n                                logoUri,\n                                null\n'
    elif 'logoUri                            ) {' in line:
        lines[i] = '                            ) {\n'

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.writelines(lines)

with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'r') as f:
    content = f.read()

content = content.replace("Icon(androidx.compose.material.icons.filled.Edit", "Icon(Icons.Filled.Edit")
with open('app/src/main/java/com/example/ui/screens/ProductItems.kt', 'w') as f:
    f.write(content)
