package com.crimsonedge.studioadmin.domain.repository

import com.crimsonedge.studioadmin.data.remote.dto.CreatedResponse
import com.crimsonedge.studioadmin.data.remote.dto.MessageResponse
import com.crimsonedge.studioadmin.data.remote.dto.NavigationItemRequest
import com.crimsonedge.studioadmin.data.remote.dto.SocialLinkRequest
import com.crimsonedge.studioadmin.domain.model.NavigationItem
import com.crimsonedge.studioadmin.domain.model.SocialLink
import com.crimsonedge.studioadmin.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface NavigationRepository {
    fun getNavItems(): Flow<Resource<List<NavigationItem>>>
    fun createNavItem(request: NavigationItemRequest): Flow<Resource<CreatedResponse>>
    fun updateNavItem(id: Int, request: NavigationItemRequest): Flow<Resource<MessageResponse>>
    fun deleteNavItem(id: Int): Flow<Resource<MessageResponse>>
    fun getSocialLinks(): Flow<Resource<List<SocialLink>>>
    fun createSocialLink(request: SocialLinkRequest): Flow<Resource<CreatedResponse>>
    fun updateSocialLink(id: Int, request: SocialLinkRequest): Flow<Resource<MessageResponse>>
    fun deleteSocialLink(id: Int): Flow<Resource<MessageResponse>>
}
