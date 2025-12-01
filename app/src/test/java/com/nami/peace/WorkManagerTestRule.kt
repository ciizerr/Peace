package com.nami.peace

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit test rule that initializes WorkManager for testing.
 * This prevents the IllegalStateException that occurs when WorkManager
 * is accessed before initialization in tests.
 * 
 * Usage:
 * ```
 * @get:Rule
 * val workManagerRule = WorkManagerTestRule()
 * ```
 */
class WorkManagerTestRule : TestWatcher() {
    
    override fun starting(description: Description) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        
        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }
}
