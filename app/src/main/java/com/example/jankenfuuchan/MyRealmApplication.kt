package com.example.jankenfuuchan

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MyRealmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true).deleteRealmIfMigrationNeeded().build()
        Realm.setDefaultConfiguration(config)
        

    }
}