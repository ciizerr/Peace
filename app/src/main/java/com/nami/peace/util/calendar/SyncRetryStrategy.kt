package com.nami.peace.util.calendar

import kotlin.math.min
import kotlin.math.pow

/**
 * Strategy for retrying failed calendar sync operations with exponential backoff.
 */
object SyncRetryStrategy {
    
    /** Maximum number of retry attempts before giving up */
    const val MAX_RETRY_ATTEMPTS = 5
    
    /** Base delay in milliseconds for exponential backoff */
    private const val BASE_DELAY_MS = 1000L // 1 second
    
    /** Maximum delay in milliseconds (cap for exponential backoff) */
    private const val MAX_DELAY_MS = 300000L // 5 minutes
    
    /**
     * Calculate the delay before the next retry attempt using exponential backoff.
     * 
     * Formula: min(BASE_DELAY * 2^retryCount, MAX_DELAY)
     * 
     * @param retryCount The number of previous retry attempts
     * @return Delay in milliseconds before next retry
     */
    fun calculateRetryDelay(retryCount: Int): Long {
        if (retryCount < 0) return 0L
        if (retryCount >= MAX_RETRY_ATTEMPTS) return MAX_DELAY_MS
        
        val exponentialDelay = BASE_DELAY_MS * (2.0.pow(retryCount.toDouble())).toLong()
        return min(exponentialDelay, MAX_DELAY_MS)
    }
    
    /**
     * Check if a sync operation should be retried based on retry count.
     * 
     * @param retryCount The number of previous retry attempts
     * @return true if should retry, false if max attempts reached
     */
    fun shouldRetry(retryCount: Int): Boolean {
        return retryCount < MAX_RETRY_ATTEMPTS
    }
    
    /**
     * Check if enough time has passed since the last retry to attempt again.
     * 
     * @param lastRetryAt Timestamp of last retry attempt in milliseconds
     * @param retryCount The number of previous retry attempts
     * @return true if enough time has passed, false otherwise
     */
    fun canRetryNow(lastRetryAt: Long?, retryCount: Int): Boolean {
        if (lastRetryAt == null) return true
        
        val requiredDelay = calculateRetryDelay(retryCount)
        val timeSinceLastRetry = System.currentTimeMillis() - lastRetryAt
        
        return timeSinceLastRetry >= requiredDelay
    }
    
    /**
     * Determine if an error is retryable.
     * 
     * @param error The exception that occurred
     * @return true if the error is retryable, false if it's permanent
     */
    fun isRetryableError(error: Throwable): Boolean {
        return when {
            // Network errors are retryable
            error is java.net.UnknownHostException -> true
            error is java.net.SocketTimeoutException -> true
            error is java.io.IOException -> true
            
            // Google API errors
            error.message?.contains("network", ignoreCase = true) == true -> true
            error.message?.contains("timeout", ignoreCase = true) == true -> true
            error.message?.contains("unavailable", ignoreCase = true) == true -> true
            error.message?.contains("503", ignoreCase = true) == true -> true
            error.message?.contains("429", ignoreCase = true) == true -> true // Rate limit
            
            // Permission errors are NOT retryable (need user action)
            error.message?.contains("permission", ignoreCase = true) == true -> false
            error.message?.contains("unauthorized", ignoreCase = true) == true -> false
            error.message?.contains("401", ignoreCase = true) == true -> false
            error.message?.contains("403", ignoreCase = true) == true -> false
            
            // Authentication errors are NOT retryable
            error.message?.contains("authentication", ignoreCase = true) == true -> false
            error.message?.contains("not authenticated", ignoreCase = true) == true -> false
            
            // Default: retry for unknown errors
            else -> true
        }
    }
}
