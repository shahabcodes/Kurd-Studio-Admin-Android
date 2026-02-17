package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.domain.model.DashboardStats
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getStats(): Flow<Resource<DashboardStats>>
}
