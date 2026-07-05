#!/bin/bash

# 1. Eliminar la línea problemática de signingConfig
sed -i '/signingConfig = signingConfigs.getByName("debugConfig")/d' app/build.gradle.kts

# 2. Comentar o eliminar toda la configuración de signingConfigs que use my-upload-key.jks
sed -i '/my-upload-key.jks/d' app/build.gradle.kts
sed -i '/storeFile/d' app/build.gradle.kts
sed -i '/storePassword/d' app/build.gradle.kts
sed -i '/keyAlias/d' app/build.gradle.kts
sed -i '/keyPassword/d' app/build.gradle.kts

# 3. Asegurar que buildTypes release NO tenga signingConfig
sed -i '/signingConfig = signingConfigs.getByName("release")/d' app/build.gradle.kts

echo "✅ Configuración de firma eliminada de build.gradle.kts"
