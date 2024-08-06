package com.example.ssgapp.operator.data.repository

import arrow.core.Either
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.operator.domain.model.NetworkError
import com.example.ssgapp.operator.domain.repository.MachineryRepository
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

class FakeMachineryRepositoryImpl @Inject constructor(): MachineryRepository {

    override suspend fun getAvailableMachineriesFor(workerId: String): Either<NetworkError, Response<List<Machinery>>> {
        delay(1000L)

        val macAddressTest = "7B:A2:BA:56:0E:B3"//"E4:5F:01:5F:5B:3F"

        return Either.catch {
            val machineryList = listOf(
                Machinery(
                    id = "A",
                    name = "Carro Plaza 2T",
                    type = "Camion Trasportatore",
                    serialNumber = "AA77AA",
                    macAddress = macAddressTest,
                    isRemote = true
                ),
                Machinery(
                    id = "B",
                    name = "Trattore Lamborghini",
                    type = "Trattore",
                    serialNumber = "BB77BB",
                    macAddress = macAddressTest,
                    isRemote = false
                ),
                Machinery(
                    id = "C",
                    name = "Trattorino Ludopatico",
                    type = "Trattore",
                    serialNumber = "CC77CC",
                    macAddress = macAddressTest,
                    isRemote = false
                ),
                Machinery(
                    id = "D",
                    name = "Bulldozer Sovietico 5T",
                    type = "Bulldozer",
                    serialNumber = "AA77AA",
                    macAddress = macAddressTest,
                    isRemote = false
                ),
            )

            val response: Response<List<Machinery>>  = Response.success(machineryList)
            response
        }.mapLeft { it.toNetworkError() }
    }

}