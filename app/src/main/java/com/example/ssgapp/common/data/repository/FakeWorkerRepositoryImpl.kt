package com.example.ssgapp.common.data.repository

import arrow.core.Either
import com.example.ssgapp.common.data.remote.WorkerApi
import com.example.ssgapp.common.domain.model.Worker
import com.example.ssgapp.common.domain.repository.WorkerRepository
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.operator.domain.model.NetworkError
import javax.inject.Inject

class FakeWorkerRepositoryImpl @Inject constructor(
    private val workerApi: WorkerApi
): WorkerRepository {
    override suspend fun getWorkerFrom(userId: String): Either<NetworkError, Worker> {
        return Either.catch {
            if (userId == "668127c2e4c3e33cc58ed43d") { // Equipment operator
                Worker(
                    workerId = "668127c20bcad837b5084ffa",
                    name = "carlo",
                    surname = "marlo",
                    ssn = "CCGCMN98D01G751P",
                    email = "carlo@gmail.com@gw1.gw1",
                    birth = "1998-12-03",
                    generalLicence = "C",
                    specificLicences = listOf("gru mobile", "camion trasportatore")
                )
            } else if (userId == "668127abe4c3e33cc58ed43c") { // Ground worker
                Worker(
                    workerId = "668127aa0bcad837b5084ff9",
                    name = "Giacomo",
                    surname = "Wonka",
                    ssn = "GCGCMN98D01G751P",
                    email = "gw1@gw1.gw1",
                    birth = "1998-12-03"
                )
            } else {
                throw Exception("An error occurred!")
            }
        }.mapLeft {
            it.toNetworkError()
        }
    }
}