package com.example.ssgapp.common.data.remote

import com.example.ssgapp.common.domain.model.Worker
import retrofit2.http.GET
import retrofit2.http.Path

interface WorkerApi {
    @GET("user/{userId}")
    suspend fun getWorkerFrom(@Path("userId") userId: String): Worker
}