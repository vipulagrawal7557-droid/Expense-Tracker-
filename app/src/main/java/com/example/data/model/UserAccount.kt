package com.example.data.model

import android.content.Context
import android.content.SharedPreferences

data class UserAccount(
    val email: String,
    val name: String,
    val role: String,
    val isAdmin: Boolean,
    val phone: String = "+91 9829598357"
)

object AdminCredentials {
    const val DEVELOPER_EMAIL = "vipulagrawal7557@gmail.com"
    const val DEVELOPER_PASSWORD = "Vishu@7557"
    const val DEVELOPER_NAME = "Vipul Agrawal"
    const val DEVELOPER_PHONE = "+91 9829598357"
    const val DEVELOPER_ROLE = "Lead Developer & Administrator"

    val DEVELOPER_ACCOUNT = UserAccount(
        email = DEVELOPER_EMAIL,
        name = DEVELOPER_NAME,
        role = DEVELOPER_ROLE,
        isAdmin = true,
        phone = DEVELOPER_PHONE
    )

    val GUEST_ACCOUNT = UserAccount(
        email = "guest@fintrack.local",
        name = "Guest User",
        role = "Standard Access",
        isAdmin = false,
        phone = ""
    )
}

class AuthPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fintrack_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_IS_ADMIN = "key_is_admin"
        private const val KEY_USER_PHONE = "key_user_phone"
    }

    fun saveSession(user: UserAccount) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_ROLE, user.role)
            .putBoolean(KEY_IS_ADMIN, user.isAdmin)
            .putString(KEY_USER_PHONE, user.phone)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getSavedSession(): UserAccount? {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val email = prefs.getString(KEY_USER_EMAIL, AdminCredentials.DEVELOPER_EMAIL) ?: AdminCredentials.DEVELOPER_EMAIL
        val name = prefs.getString(KEY_USER_NAME, AdminCredentials.DEVELOPER_NAME) ?: AdminCredentials.DEVELOPER_NAME
        val role = prefs.getString(KEY_USER_ROLE, AdminCredentials.DEVELOPER_ROLE) ?: AdminCredentials.DEVELOPER_ROLE
        val isAdmin = prefs.getBoolean(KEY_IS_ADMIN, true)
        val phone = prefs.getString(KEY_USER_PHONE, AdminCredentials.DEVELOPER_PHONE) ?: AdminCredentials.DEVELOPER_PHONE

        return UserAccount(
            email = email,
            name = name,
            role = role,
            isAdmin = isAdmin,
            phone = phone
        )
    }
}
