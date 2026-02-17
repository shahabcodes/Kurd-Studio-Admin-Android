package com.crimsonedge.studioadmin.di

import com.crimsonedge.studioadmin.data.repository.ArtworkRepositoryImpl
import com.crimsonedge.studioadmin.data.repository.AuthRepositoryImpl
import com.crimsonedge.studioadmin.data.repository.ContactRepositoryImpl
import com.crimsonedge.studioadmin.data.repository.DashboardRepositoryImpl
import com.crimsonedge.studioadmin.data.repository.ImageRepositoryImpl
import com.crimsonedge.studioadmin.data.repository.NavigationRepositoryImpl
import com.crimsonedge.studioadmin.data.repository.SiteRepositoryImpl
import com.crimsonedge.studioadmin.data.repository.WritingRepositoryImpl
import com.crimsonedge.studioadmin.domain.repository.ArtworkRepository
import com.crimsonedge.studioadmin.domain.repository.AuthRepository
import com.crimsonedge.studioadmin.domain.repository.ContactRepository
import com.crimsonedge.studioadmin.domain.repository.DashboardRepository
import com.crimsonedge.studioadmin.domain.repository.ImageRepository
import com.crimsonedge.studioadmin.domain.repository.NavigationRepository
import com.crimsonedge.studioadmin.domain.repository.SiteRepository
import com.crimsonedge.studioadmin.domain.repository.WritingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindArtworkRepository(impl: ArtworkRepositoryImpl): ArtworkRepository

    @Binds
    @Singleton
    abstract fun bindWritingRepository(impl: WritingRepositoryImpl): WritingRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindSiteRepository(impl: SiteRepositoryImpl): SiteRepository

    @Binds
    @Singleton
    abstract fun bindNavigationRepository(impl: NavigationRepositoryImpl): NavigationRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository
}
