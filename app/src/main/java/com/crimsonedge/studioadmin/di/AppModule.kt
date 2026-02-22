package com.crimsonedge.studioadmin.di

import android.content.Context
import com.crimsonedge.studioadmin.BuildConfig
import com.crimsonedge.studioadmin.data.local.ThemeDataStore
import com.crimsonedge.studioadmin.data.local.TokenDataStore
import com.crimsonedge.studioadmin.data.remote.api.ArtworkApi
import com.crimsonedge.studioadmin.data.remote.api.AuthApi
import com.crimsonedge.studioadmin.data.remote.api.ContactApi
import com.crimsonedge.studioadmin.data.remote.api.DashboardApi
import com.crimsonedge.studioadmin.data.remote.api.ImageApi
import com.crimsonedge.studioadmin.data.remote.api.NavigationApi
import com.crimsonedge.studioadmin.data.remote.api.SiteApi
import com.crimsonedge.studioadmin.data.remote.api.WritingApi
import com.crimsonedge.studioadmin.data.remote.interceptor.AuthInterceptor
import com.crimsonedge.studioadmin.data.remote.interceptor.SecurityCheckInterceptor
import com.crimsonedge.studioadmin.data.remote.interceptor.TokenAuthenticator
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenDataStore(
        @ApplicationContext context: Context
    ): TokenDataStore {
        return TokenDataStore(context)
    }

    @Provides
    @Singleton
    fun provideThemeDataStore(
        @ApplicationContext context: Context
    ): ThemeDataStore {
        return ThemeDataStore(context)
    }

    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return if (BuildConfig.DEBUG) {
            CertificatePinner.DEFAULT
        } else {
            CertificatePinner.Builder()
                .add(
                    "admin.kurdstudio.com",
                    "sha256/9MBQoq7S0tcAlSzqr/3Roiz1J48UDXINYyzqr+w7FzY=", // Leaf
                    "sha256/iFvwVyJSxnQdyaUvUERIf+8qk7gRze3612JMwoO3zdU="  // Intermediate (Cloudflare)
                )
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        moshi: Moshi,
        securityCheckInterceptor: SecurityCheckInterceptor,
        certificatePinner: CertificatePinner
    ): AuthApi {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(securityCheckInterceptor)
            .addInterceptor(loggingInterceptor)
            .certificatePinner(certificatePinner)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenDataStore: TokenDataStore): AuthInterceptor {
        return AuthInterceptor(tokenDataStore)
    }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenDataStore: TokenDataStore,
        authApi: AuthApi
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenDataStore, authApi)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        securityCheckInterceptor: SecurityCheckInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(securityCheckInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .certificatePinner(certificatePinner)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideDashboardApi(retrofit: Retrofit): DashboardApi {
        return retrofit.create(DashboardApi::class.java)
    }

    @Provides
    @Singleton
    fun provideArtworkApi(retrofit: Retrofit): ArtworkApi {
        return retrofit.create(ArtworkApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWritingApi(retrofit: Retrofit): WritingApi {
        return retrofit.create(WritingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideImageApi(retrofit: Retrofit): ImageApi {
        return retrofit.create(ImageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSiteApi(retrofit: Retrofit): SiteApi {
        return retrofit.create(SiteApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNavigationApi(retrofit: Retrofit): NavigationApi {
        return retrofit.create(NavigationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideContactApi(retrofit: Retrofit): ContactApi {
        return retrofit.create(ContactApi::class.java)
    }
}
