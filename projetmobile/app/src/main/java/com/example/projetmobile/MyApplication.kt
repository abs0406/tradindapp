// app/src/main/java/com/example/projetmobile/MyApplication.kt
package com.example.projetmobile

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialise Firebase avant tout
        FirebaseApp.initializeApp(this)
    }
}
