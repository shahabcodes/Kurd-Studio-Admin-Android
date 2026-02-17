package com.crimsonedge.studioadmin.data.remote.api

import com.crimsonedge.studioadmin.data.remote.dto.DashboardStatsDto
import retrofit2.http.GET

interface DashboardApi {

    @GET("dashboard/stats")
    suspend fun getStats(): DashboardStatsDto
}
