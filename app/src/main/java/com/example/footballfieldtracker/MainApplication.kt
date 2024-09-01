package com.example.footballfieldtracker

import android.app.Application
import com.example.footballfieldtracker.data.DefaultAppContainer


class MainApplication : Application() {
    // Mogao si za ovo interfejs da napravis
    lateinit var container: DefaultAppContainer

    override fun onCreate() {
        super.onCreate()

        container = DefaultAppContainer(this)
    }
}