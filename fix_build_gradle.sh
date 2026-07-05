#!/bin/bash
# Eliminar la línea que contiene signingConfig = signingConfigs.getByName("debugConfig")
sed -i '/signingConfig = signingConfigs.getByName("debugConfig")/d' app/build.gradle.kts
echo "✅ Línea problemática eliminada de app/build.gradle.kts"
