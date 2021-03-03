package com.example.projectapp

import android.content.Context

class SharedPreferencesHelper(context: Context) {

    companion object{
        const val permission_preferences = "PERMISSION_PREFS"
        const val show_permission_alert = "show_permission_alert"
    }

    private val preferences = context.getSharedPreferences(permission_preferences, Context.MODE_PRIVATE)

    fun savePermission(permission: String, granted: Boolean){
        preferences.edit().putBoolean(permission, granted).apply()
    }

    fun getPermissionState(permission: String) = preferences.getBoolean(permission, false)

    fun putShowAlertOnStartUp(show: Boolean) {
        preferences.edit().putBoolean(show_permission_alert, show).apply()
    }

    fun getShowAlertOnStartUp(): Boolean{
        return preferences.getBoolean(show_permission_alert, false)
    }
}