package com.crowdshield.stampede.network

import android.content.Context
import android.net.Uri
import com.crowdshield.stampede.domain.VideoAnalysisResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoApiService @Inject constructor(
    private val client: OkHttpClient,
    @ApplicationContext private val context: Context
) {
    // In a real app, this would be a config or build parameter
    private val baseUrl = "http://10.0.2.2:8000" // Emulator loopback to local server

    suspend fun uploadVideo(fileUri: Uri): VideoAnalysisResult = withContext(Dispatchers.IO) {
        val file = getFileFromUri(fileUri) ?: throw Exception("Failed to access video file")
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("video/*".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("$baseUrl/api/v1/videos/upload")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Upload failed: ${response.message}")
            
            val json = JSONObject(response.body?.string() ?: "{}")
            VideoAnalysisResult(
                filename = json.optString("filename"),
                videoPath = json.optString("video_path"),
                totalHumansDetected = json.optInt("total_humans", 0),
                riskScore = json.optDouble("risk_score", 0.0).toFloat(),
                status = "SUCCESS"
            )
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File(context.cacheDir, "upload_temp_${System.currentTimeMillis()}.mp4")
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        return file
    }
}
