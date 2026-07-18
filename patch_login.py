import sys

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'r') as f:
    content = f.read()

bad_login = """    fun loginWithEmail(displayName: String, email: String, avatarUrl: String, context: android.content.Context) {
        val newUserId = "email_${UUID.randomUUID().toString().take(6)}"
        val session = UserSession(
            userId = newUserId,
            displayName = displayName,
            email = email,
            avatarUrl = avatarUrl,
            isAnonymous = false,
            uID = newUserId
        )
        _userSession.value = session
        
        val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("session_user_id", newUserId)
            putString("session_name", displayName)
            putString("session_email", email)
            putString("session_avatar", avatarUrl)
            putBoolean("session_is_anonymous", false)
            putString("session_uid", newUserId)
        }.apply()
    }"""

good_login = """    fun loginWithEmail(displayName: String, email: String, avatarUrl: String, uID: String, context: android.content.Context) {
        val session = UserSession(
            userId = uID,
            displayName = displayName,
            email = email,
            avatarUrl = avatarUrl,
            isAnonymous = false,
            uID = uID
        )
        _userSession.value = session
        
        val sharedPrefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("session_user_id", uID)
            putString("session_name", displayName)
            putString("session_email", email)
            putString("session_avatar", avatarUrl)
            putBoolean("session_is_anonymous", false)
            putString("session_uid", uID)
        }.apply()
    }"""

content = content.replace(bad_login, good_login)

with open('app/src/main/java/com/example/ui/AppViewModel.kt', 'w') as f:
    f.write(content)

