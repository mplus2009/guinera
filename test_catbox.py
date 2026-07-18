import requests

url = "https://catbox.moe/user/api.php"
files = {'fileToUpload': ('test.txt', b'hello world')}
data = {'reqtype': 'fileupload'}
response = requests.post(url, files=files, data=data)
print(response.status_code)
print(response.text)
