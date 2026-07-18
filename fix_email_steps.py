import re

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'r') as f:
    content = f.read()

# I will find the block starting with 2 -> { and ending with 3 -> { and replace it just with 2 -> {
start_str = "                            2 -> {\n                                Text(\n                                    text = \"Verificación de Correo\","
end_str = "                            3 -> {\n                                Text(\n                                    text = \"Completa tu Perfil\","

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    new_step_2 = "                            2 -> {\n                                Text(\n                                    text = \"Completa tu Perfil\","
    content = content[:start_idx] + new_step_2 + content[end_idx + len(end_str):]
else:
    print("Could not find blocks")

with open('app/src/main/java/com/example/ui/screens/LoginScreen.kt', 'w') as f:
    f.write(content)
