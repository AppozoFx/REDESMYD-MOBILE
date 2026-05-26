package com.redes.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.redes.app.di.AppContainer
import com.redes.app.di.DefaultAppContainer

class REDESApplication : Application() {
    val appContainer: AppContainer by lazy {
        DefaultAppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appContainer.presenceManager)
    }
}
