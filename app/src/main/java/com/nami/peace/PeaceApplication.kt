package com.nami.peace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import javax.inject.Inject

@HiltAndroidApp
class PeaceApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
