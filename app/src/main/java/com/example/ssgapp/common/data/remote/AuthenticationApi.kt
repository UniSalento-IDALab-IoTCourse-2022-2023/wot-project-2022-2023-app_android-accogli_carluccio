package com.example.ssgapp.common.data.remote

import com.example.ssgapp.common.domain.model.JwtToken
import com.example.ssgapp.common.domain.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthenticationApi {
    @Headers("Content-Type: application/json")
    @POST("authenticate")
    suspend fun login(@Body userData: User): JwtToken
}