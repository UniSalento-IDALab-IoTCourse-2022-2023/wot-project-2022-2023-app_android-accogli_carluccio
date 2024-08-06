package com.example.ssgapp.common

import android.content.Context
import android.content.SharedPreferences

class LocalStorage (context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("localStorage", Context.MODE_PRIVATE)

    // Funzione per memorizzare un valore
    fun saveValue(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    // Funzione per leggere un valore
    fun getValue(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    // Funzione per aggiornare un valore
    fun updateValue(key: String, value: String) {
        saveValue(key, value)  // Riutilizziamo saveValue perché la logica è la stessa
    }

    // Funzione per rimuovere un valore
    fun removeValue(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }
}