package de.tum.storemanager

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class StoreManager : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    companion object {
        @Volatile
        lateinit var service: StoreService
    }
}
