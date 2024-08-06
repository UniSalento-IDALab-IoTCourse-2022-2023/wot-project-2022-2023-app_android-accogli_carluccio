package com.example.ssgapp.di

import android.content.Context
import com.example.ssgapp.MyApplication
import com.example.ssgapp.common.LocalStorage
import com.example.ssgapp.common.data.remote.AuthenticationApi
import com.example.ssgapp.common.data.remote.SiteManagementApi
import com.example.ssgapp.common.data.remote.WorkerApi
import com.example.ssgapp.common.network.HeaderInterceptor
import com.example.ssgapp.util.Constant.BASE_URL_LOGIN_MS
import com.example.ssgapp.util.Constant.BASE_URL_SITE_MANAGEMENT_MS
import com.example.ssgapp.util.Constant.BASE_URL_WORKER_MS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideSiteManagementApi(@ApplicationContext context: Context): SiteManagementApi {
        val headerInterceptor = HeaderInterceptor(context)

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL_SITE_MANAGEMENT_MS)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SiteManagementApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthenticationApi(): AuthenticationApi {
        //val client = OkHttpClient.Builder().build() // non necessario. funziona anche quando non lo fornisco

        return Retrofit.Builder()
            .baseUrl(BASE_URL_LOGIN_MS)
            .addConverterFactory(GsonConverterFactory.create())
            //.client(client)
            .build()
            .create(AuthenticationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkerApi(@ApplicationContext context: Context): WorkerApi {
        //val client = OkHttpClient.Builder().build() // non necessario. funziona anche quando non lo fornisco
        val headerInterceptor = HeaderInterceptor(context)

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL_WORKER_MS)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            //.client(client)
            .build()
            .create(WorkerApi::class.java)
    }

    // Per fornire l'istanza MyApplication ai viewModel che vogliano usarla
    @Provides
    @Singleton
    fun provideMyApplication(@ApplicationContext appContext: Context): MyApplication {
        return appContext as MyApplication
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }
}