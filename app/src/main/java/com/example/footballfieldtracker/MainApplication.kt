package com.example.footballfieldtracker

import android.app.Application
import com.example.footballfieldtracker.data.DefaultAppContainer

class MainApplication : Application() {
    private lateinit var container: DefaultAppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}