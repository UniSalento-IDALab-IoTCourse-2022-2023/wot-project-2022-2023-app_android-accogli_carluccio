package com.example.ssgapp.operator.data.repository

import arrow.core.Either
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.common.data.remote.SiteManagementApi
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.operator.domain.model.NetworkError
import com.example.ssgapp.operator.domain.repository.MachineryRepository
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

class MachineryRepositoryImpl @Inject constructor(
    private val siteManagementApi: SiteManagementApi
) : MachineryRepository {

    override suspend fun getAvailableMachineriesFor(workerId: String): Either<NetworkError, Response<List<Machinery>>> {
        delay(1000L)
        return Either.catch {
            siteManagementApi.getAvailableMachineriesFor(workerId)
        }.mapLeft {
            it.toNetworkError()
        }
    }

}