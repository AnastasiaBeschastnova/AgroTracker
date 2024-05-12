package com.example.agrotracker.localdata

import android.content.Context
import android.content.Context.MODE_PRIVATE

import android.content.SharedPreferences




class AgroTrackerPreferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("AgroTrackerPreferences", MODE_PRIVATE)
    val tokenKey: String="token"

    fun getToken(): String?{
        //получение существующего токена
        return preferences.getString(tokenKey,null)
    }

    fun saveToken(token: String){
        //сохранение токена
        preferences.edit()
            .putString(tokenKey, token)
            .apply()
    }

    fun deleteToken(){
        //удаление сохраненного токена
        preferences.edit()
            .putString(tokenKey, null)
            .apply()
    }
}