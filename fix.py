import sys
with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'r') as f:
    content = f.read()

content = content.replace("selectedLocation!!.longitude, logoUri, null\n                            ) {", "selectedLocation!!.longitude, \n                                logoUri, \n                                null\n                            ) {")

with open('app/src/main/java/com/example/ui/screens/MySpaceScreens.kt', 'w') as f:
    f.write(content)
