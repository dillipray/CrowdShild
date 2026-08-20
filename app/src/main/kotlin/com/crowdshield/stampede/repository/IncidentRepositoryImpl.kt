package com.crowdshield.stampede.repository

import android.net.Uri
import com.crowdshield.stampede.data.IncidentDao
import com.crowdshield.stampede.data.IncidentEntity
import com.crowdshield.stampede.domain.VideoAnalysisResult
import com.crowdshield.stampede.network.VideoApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepositoryImpl @Inject constructor(
    private val incidentDao: IncidentDao,
    private val videoApiService: VideoApiService
) : IncidentRepository {

    override fun getAllIncidents(): Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()

    override suspend fun reportIncident(incident: IncidentEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            incidentDao.insertIncident(incident)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPendingIncidents(): Result<Unit> = withContext(Dispatchers.IO) {
        // Implementation for syncing pending incidents
        Result.success(Unit)
    }

    override suspend fun uploadVideo(uri: Uri): Result<VideoAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val result = videoApiService.uploadVideo(uri)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
