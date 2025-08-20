package com.vdi.pizzaapp.utils


import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedPreferencesHelper(context: Context) {

    // MasterKey untuk enkripsi
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // SharedPreferences terenkripsi
    private val sharedPref = EncryptedSharedPreferences.create(
        context,
        "user_prefs_encrypt",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserName(name: String) {
        sharedPref.edit().putString("USER_NAME", name).apply()
    }

    fun getUserName(): String {
        return sharedPref.getString("USER_NAME", "") ?: ""
    }
}