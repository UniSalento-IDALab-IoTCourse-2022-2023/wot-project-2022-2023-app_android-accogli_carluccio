package com.example.ssgapp.common.domain.repository

import arrow.core.Either
import com.example.ssgapp.common.domain.model.Worker
import com.example.ssgapp.operator.domain.model.NetworkError

interface WorkerRepository {
    suspend fun getWorkerFrom(userId: String): Either<NetworkError, Worker>
}