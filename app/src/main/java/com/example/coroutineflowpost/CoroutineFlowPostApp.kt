package com.example.coroutineflowpost

import android.app.Application

class CoroutineFlowPostApp : Application() {
    lateinit var container: AppContainer
    private set

            override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}