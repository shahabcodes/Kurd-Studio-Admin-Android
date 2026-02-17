package com.crimsonedge.studioadmin.data.repository

import com.crimsonedge.studioadmin.data.remote.api.NavigationApi
import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemRequest
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkRequest
import com.crimsonedge.studioadmin.domain.model.NavigationItem
import com.crimsonedge.studioadmin.domain.model.SocialLink
import com.crimsonedge.studioadmin.domain.repository.NavigationRepository
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationRepositoryImpl @Inject constructor(
    private val api: NavigationApi
) : NavigationRepository {

    override fun getNavItems(): Flow<Resource<List<NavigationItem>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getNavigationItems()
            val items = dtos.map { dto ->
                NavigationItem(
                    id = dto.id,
                    label = dto.label,
                    link = dto.link,
                    iconSvg = dto.iconSvg,
                    displayOrder = dto.displayOrder,
                    isActive = dto.isActive
                )
            }
            emit(Resource.Success(items))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun createNavItem(request: NavigationItemRequest): Flow<Resource<CreatedResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.createNavigationItem(request)
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

    override fun updateNavItem(id: Int, request: NavigationItemRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.updateNavigationItem(id, request)
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

    override fun deleteNavItem(id: Int): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.deleteNavigationItem(id)
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

    override fun getSocialLinks(): Flow<Resource<List<SocialLink>>> = flow {
        emit(Resource.Loading)
        try {
            val dtos = api.getSocialLinks()
            val links = dtos.map { dto ->
                SocialLink(
                    id = dto.id,
                    platform = dto.platform,
                    url = dto.url,
                    iconSvg = dto.iconSvg,
                    displayOrder = dto.displayOrder,
                    isActive = dto.isActive
                )
            }
            emit(Resource.Success(links))
        } catch (e: HttpException) {
            emit(Resource.Error(
                message = e.message() ?: "HTTP error ${e.code()}",
                code = e.code()
            ))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Unknown network error"))
        }
    }

    override fun createSocialLink(request: SocialLinkRequest): Flow<Resource<CreatedResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.createSocialLink(request)
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

    override fun updateSocialLink(id: Int, request: SocialLinkRequest): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.updateSocialLink(id, request)
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

    override fun deleteSocialLink(id: Int): Flow<Resource<MessageResponse>> = flow {
        emit(Resource.Loading)
        try {
            val response = api.deleteSocialLink(id)
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
