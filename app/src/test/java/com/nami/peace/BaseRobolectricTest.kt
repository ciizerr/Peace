package com.nami.peace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Base test class for Robolectric tests that properly initializes WorkManager.
 * All Robolectric tests should extend this class to avoid WorkManager initialization errors.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
abstract class BaseRobolectricTest {
    
    @Before
    open fun initializeWorkManager() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        
        // Initialize WorkManager for testing
        // This is safe to call multiple times - it will only initialize once
        try {
            WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        } catch (e: IllegalStateException) {
            // WorkManager already initialized, ignore
        }
    }
}
