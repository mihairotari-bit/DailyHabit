package com.mihai.dailyhabit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

@HiltAndroidApp
class HelloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
    }
}
