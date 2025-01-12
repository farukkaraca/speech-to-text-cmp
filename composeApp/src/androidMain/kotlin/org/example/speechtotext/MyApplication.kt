package org.example.speechtotext

import android.app.Application
import di.initKoin
import di.platformModule
import di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        initKoin {
            androidContext(this@MyApplication)
            modules(
                module {
                    single { applicationContext }
                },
                platformModule,
                sharedModule
            )
        }
    }
}