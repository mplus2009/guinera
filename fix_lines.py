import sys
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if 'selectedLocation!!.longitude, logoUri, null' in line:
        new_lines.append('                                selectedLocation!!.longitude, logoUri, null\n')
    elif 'logoUri                            ) {' in line:
        new_lines.append('                            ) {\n')
    else:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.writelines(new_lines)
