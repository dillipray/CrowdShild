package com.crowdshield.stampede.repository

import android.net.Uri
import com.crowdshield.stampede.data.IncidentEntity
import com.crowdshield.stampede.domain.VideoAnalysisResult
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    fun getAllIncidents(): Flow<List<IncidentEntity>>
    suspend fun reportIncident(incident: IncidentEntity): Result<Unit>
    suspend fun syncPendingIncidents(): Result<Unit>
    suspend fun uploadVideo(uri: Uri): Result<VideoAnalysisResult>
}
