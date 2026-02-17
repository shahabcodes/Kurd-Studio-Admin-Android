package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.SiteApi
import com.crimsonedge.studioadmin.data.remote.dto.HeroRequest
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.ProfileRequest
import com.crimsonedge.studioadmin.data.remote.dto.SectionRequest
import com.crimsonedge.studioadmin.data.remote.dto.SiteSettingRequest
import com.crimsonedge.studioadmin.domain.model.HeroContent
import com.crimsonedge.studioadmin.domain.model.Profile
import com.crimsonedge.studioadmin.domain.model.Section
import com.crimsonedge.studioadmin.domain.model.SiteSetting
import com.crimsonedge.studioadmin.domain.repository.SiteRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteRepositoryImpl @Inject constructor(
    private val api: SiteApi
) : SiteRepository {

    override fun getProfile(): Flow<Resource<Profile>> = flow {
        emit(Resource.Loading)
        try {
            val dto = api.getProfile()
            val profile = Profile(
                id = dto.id,
                name = dto.name,
                tagline = dto.tagline,
                bio = dto.bio,
                avatarImageId = dto.avatarImageId,
                email = dto.email,
                instagramUrl = dto.instagramUrl,
                twitterUrl = dto.twitterUrl,
                artworksCount = dto.artworksCount,
                poemsCount = dto.poemsCount,
                yearsExperience = dto.yearsExperience,
                updatedAt = dto.updatedAt
            )
            emit(Resource.Success(profile))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun updateProfile(request: ProfileRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.updateProfile(request)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun getHero(): Flow<Resource<HeroContent>> = flow {
        emit(Resource.Loading)
        try {
            val dto = api.getHero()
            val hero = HeroContent(
                id = dto.id,
                quote = dto.quote,
                quoteAttribution = dto.quoteAttribution,
                headline = dto.headline,
                subheading = dto.subheading,
                featuredImageId = dto.featuredImageId,
                badgeText = dto.badgeText,
                primaryCtaText = dto.primaryCtaText,
                primaryCtaLink = dto.primaryCtaLink,
                secondaryCtaText = dto.secondaryCtaText,
                secondaryCtaLink = dto.secondaryCtaLink,
                isActive = dto.isActive,
                updatedAt = dto.updatedAt
            )
            emit(Resource.Success(hero))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun updateHero(request: HeroRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.updateHero(request)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun getSettings(): Flow<Resource<List<SiteSetting>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getSettings()
            val settings = dtos.map { dto ->
                SiteSetting(
                    id = dto.id,
                    settingKey = dto.settingKey,
                    settingValue = dto.settingValue,
                    settingType = dto.settingType,
                    updatedAt = dto.updatedAt
                )
            }
            emit(Resource.Success(settings))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun updateSetting(key: String, request: SiteSettingRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.updateSetting(key, request)
            emit(Resource.Success(response))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun getSections(): Flow<Resource<List<Section>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getSections()
            val sections = dtos.map { dto ->
                Section(
                    id = dto.id,
                    sectionKey = dto.sectionKey,
                    tag = dto.tag,
                    title = dto.title,
                    subtitle = dto.subtitle,
                    displayOrder = dto.displayOrder,
                    isActive = dto.isActive,
                    updatedAt = dto.updatedAt
                )
            }
            emit(Resource.Success(sections))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun updateSection(id: Int, request: SectionRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.updateSection(id, request)
            emit(Resource.Success(response))
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
