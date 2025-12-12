package com.nami.peace.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.nami.peace.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

data class UpdateInfo(
    val version: String,
    val releaseNotes: String,
    val downloadUrl: String
)

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    object Downloading : DownloadStatus()
    data class Progress(val percent: Int) : DownloadStatus()
    data class ReadyToInstall(val file: File) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    data class Available(val updateInfo: UpdateInfo) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data class ReadyToInstall(val file: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}



@Singleton
class AppUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()
    private val repoOwner = "ciizerr"
    private val repoName = "Peace"

    fun checkForUpdate(): Flow<UpdateState> = flow {
        try {
            val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val request = Request.Builder().url(url).build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Specific handling for 404 (No releases yet)
                    if (response.code == 404) {
                         emit(UpdateState.Error(context.getString(com.nami.peace.R.string.error_no_releases)))
                    } else {
                         emit(UpdateState.Error(context.getString(com.nami.peace.R.string.error_github_api, response.code)))
                    }
                    return@use
                }

                val json = JSONObject(response.body?.string() ?: "")
                val tagName = json.optString("tag_name", "").removePrefix("v")
                val body = json.optString("body", "")
                val assets = json.optJSONArray("assets")
                
                // Find APK asset
                var downloadUrl = ""
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.getString("name").endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                }

                if (tagName.isNotEmpty() && downloadUrl.isNotEmpty()) {
                    if (isNewerVersion(tagName, BuildConfig.VERSION_NAME)) {
                        emit(UpdateState.Available(UpdateInfo(tagName, body, downloadUrl)))
                    } else {
                        emit(UpdateState.UpToDate)
                    }
                } else {
                     // Tag exists but no APK found (Source code release only?)
                    emit(UpdateState.UpToDate)
                }
            }
        } catch (e: java.io.IOException) {
            // Network error (No internet, timeout)
            emit(UpdateState.Error(context.getString(com.nami.peace.R.string.error_network)))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(UpdateState.Error(e.localizedMessage ?: "Unknown Error"))
        }
    }.flowOn(Dispatchers.IO)

    fun downloadApk(url: String): Flow<DownloadStatus> = flow {
        emit(DownloadStatus.Downloading)
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                emit(DownloadStatus.Error("Download failed: ${response.code}"))
                return@flow
            }

            val body = response.body ?: run {
                emit(DownloadStatus.Error("Empty body"))
                return@flow
            }

            val file = File(context.externalCacheDir, "update.apk")
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        // Emit progress periodically if total size known
                        if (totalBytes > 0) {
                            val percent = ((downloadedBytes * 100) / totalBytes).toInt()
                            emit(DownloadStatus.Progress(percent))
                        }
                    }
                }
            }
            
            emit(DownloadStatus.ReadyToInstall(file))

        } catch (e: Exception) {
            e.printStackTrace()
            emit(DownloadStatus.Error(e.localizedMessage ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    fun installApk(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppUpdater", "Install failed", e)
        }
    }

    private fun isNewerVersion(remote: String, local: String): Boolean {
        try {
            val remoteParts = remote.split(".").map { it.toInt() }
            val localParts = local.split(".").map { it.toInt() }
            
            val length = maxOf(remoteParts.size, localParts.size)
            
            for (i in 0 until length) {
                val remotePart = remoteParts.getOrElse(i) { 0 }
                val localPart = localParts.getOrElse(i) { 0 }
                
                if (remotePart > localPart) return true
                if (remotePart < localPart) return false
            }
            
            return false // Equal
        } catch (e: Exception) {
            // Fallback for non-semver versions (e.g. "beta-1")
            return remote != local
        }
    }
}
