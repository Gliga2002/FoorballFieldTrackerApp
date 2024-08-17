package com.example.footballfieldtracker.services

import android.os.Build
import androidx.annotation.RequiresApi

interface NearbyFieldsDetectionController {
    @RequiresApi(Build.VERSION_CODES.O)
    fun startNearbyFieldsDetectionService()

    @RequiresApi(Build.VERSION_CODES.O)
    fun stopNearbyFieldsDetectionService()
}