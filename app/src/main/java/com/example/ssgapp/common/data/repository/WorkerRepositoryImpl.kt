package com.example.ssgapp.common.data.repository

import android.util.Log
import arrow.core.Either
import com.example.ssgapp.common.data.remote.WorkerApi
import com.example.ssgapp.common.domain.model.Worker
import com.example.ssgapp.common.domain.repository.WorkerRepository
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.operator.domain.model.NetworkError
import javax.inject.Inject

class WorkerRepositoryImpl  @Inject constructor(
    private val workerApi: WorkerApi
): WorkerRepository {
    override suspend fun getWorkerFrom(userId: String): Either<NetworkError, Worker> {
        return Either.catch {
            workerApi.getWorkerFrom(userId)
        }.mapLeft {
            it.toNetworkError()
        }
    }
}