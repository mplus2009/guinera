with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("Firebase Storage esté activado y tenga permisos correctos.", "Configuraste el API Key de ImgBB en los Secrets de AI Studio?")
content = content.replace("Firebase Storage puede estar fallando.", "Falta el API Key de ImgBB.")
content = content.replace("No se pudieron subir las imágenes a Firebase Storage.", "No se pudieron subir las imágenes. Falta el API Key de ImgBB en Secrets.")
content = content.replace("No se pudo subir la imagen a Firebase Storage.", "No se pudo subir la imagen. Falta el API Key de ImgBB en Secrets.")

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)
