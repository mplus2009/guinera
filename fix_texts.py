import os

files_to_fix = [
    'app/src/main/java/com/example/ui/screens/BlockScreen.kt',
    'app/src/main/java/com/example/ui/screens/NewScreens.kt',
    'app/src/main/java/com/example/MainActivity.kt',
    'app/src/main/java/com/example/ui/screens/MapScreen.kt'
]

replacements = {
    "debes registrarte con una cuenta real (Google o correo electrónico)": "debes iniciar sesión con una cuenta de Google",
    "Registrarse / Iniciar Sesión": "Acceder con Google",
    "Cuenta de Vecino Activa": "Sesión con Google Activa"
}

for file_path in files_to_fix:
    with open(file_path, 'r') as f:
        content = f.read()
    
    for k, v in replacements.items():
        content = content.replace(k, v)
        
    with open(file_path, 'w') as f:
        f.write(content)
