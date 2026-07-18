with open('app/src/main/java/com/example/data/AppRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('apiKey == ""none""', 'apiKey == "\\"none\\""')

with open('app/src/main/java/com/example/data/AppRepository.kt', 'w') as f:
    f.write(content)
