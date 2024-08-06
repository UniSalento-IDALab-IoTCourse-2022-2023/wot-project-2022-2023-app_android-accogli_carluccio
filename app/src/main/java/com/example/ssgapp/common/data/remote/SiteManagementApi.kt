package com.example.ssgapp.common.data.remote

import com.example.ssgapp.common.domain.model.Machinery
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SiteManagementApi {
    @GET("equipmentOperators/{workerId}") // TODO per il momento ho preferire non passare parametri per capire se funziona tutto: cambiare in siteconfiguration/equipmentOperators/{driverID}
    suspend fun getAvailableMachineriesFor(@Path("workerId") workerId: String): Response<List<Machinery>>

    @GET("beacons")
    suspend fun getTodayActiveMachineries(): Response<List<Machinery>>
}