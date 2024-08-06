package com.example.ssgapp.groundworker.data.repository

import arrow.core.Either
import com.example.ssgapp.common.data.remote.SiteManagementApi
import com.example.ssgapp.common.domain.model.Beacon
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.groundworker.domain.repository.ActiveMachineryRepository
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.operator.domain.model.NetworkError
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

class ActiveMachineryRepositoryImpl @Inject constructor(
    private val siteManagementApi: SiteManagementApi
): ActiveMachineryRepository {

    override suspend fun getTodayActiveMachineries(): Either<NetworkError, Response<List<Machinery>>> {
        delay(1000L)
        return Either.catch {
            siteManagementApi.getTodayActiveMachineries()
        }.mapLeft {
            it.toNetworkError()
        }
    }

}