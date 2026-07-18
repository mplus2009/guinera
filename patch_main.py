with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "val repository = AppRepository(database.appDao())", 
    "val repository = AppRepository(database.appDao(), applicationContext)"
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
