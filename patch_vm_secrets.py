with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("Falta el API Key de ImgBB en Secrets.", "Falta el API Key de ImgBB en el código.")

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)
