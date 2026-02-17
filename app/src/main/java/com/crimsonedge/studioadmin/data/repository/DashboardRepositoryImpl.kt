package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.DashboardApi
import com.crimsonedge.studioadmin.domain.model.DashboardStats
import com.crimsonedge.studioadmin.domain.repository.DashboardRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val api: DashboardApi
) : DashboardRepository {

    override fun getStats(): Flow<Resource<DashboardStats>> = flow {
        emit(Resource.Loading)
        try {
            val dto = api.getStats()
            val stats = DashboardStats(
                artworkCount = dto.artworkCount,
                writingCount = dto.writingCount,
                imageCount = dto.imageCount,
                unreadContactCount = dto.unreadContactCount
            )
            emit(Resource.Success(stats))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }
}
