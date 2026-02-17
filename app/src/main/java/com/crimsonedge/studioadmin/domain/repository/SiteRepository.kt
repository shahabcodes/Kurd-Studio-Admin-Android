package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.data.remote.dto.HeroRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.ProfileRequest
import com.crimsonedge.studioadmin.data.remote.dto.SectionRequest
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingRequest
import com.crimsonedge.studioadmin.domain.model.HeroContent
import com.crimsonedge.studioadmin.domain.model.Profile
import com.crimsonedge.studioadmin.domain.model.Section
import com.crimsonedge.studioadmin.domain.model.SiteSetting
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface SiteRepository {
    fun getProfile(): Flow<Resource<Profile>>
    fun updateProfile(request: ProfileRequest): Flow<Resource<MessageResponse>>
    fun getHero(): Flow<Resource<HeroContent>>
    fun updateHero(request: HeroRequest): Flow<Resource<MessageResponse>>
    fun getSettings(): Flow<Resource<List<SiteSetting>>>
    fun updateSetting(key: String, request: SiteSettingRequest): Flow<Resource<MessageResponse>>
    fun getSections(): Flow<Resource<List<Section>>>
    fun updateSection(id: Int, request: SectionRequest): Flow<Resource<MessageResponse>>
}
