package com.example.ssgapp.common.network

import android.content.Context
import com.example.ssgapp.common.LocalStorage
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val jwtToken = LocalStorage(context).getValue("JWT")
        val jwtTokenField = "Bearer $jwtToken"

        val request = chain.request().newBuilder()
            .addHeader("Authorization", jwtTokenField)
            .build()
        return chain.proceed(request)
    }

}