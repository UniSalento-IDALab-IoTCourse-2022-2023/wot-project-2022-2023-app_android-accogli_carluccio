package com.example.ssgapp.groundworker.data.repository

import arrow.core.Either
import com.example.ssgapp.common.domain.model.Beacon
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.groundworker.domain.repository.ActiveMachineryRepository
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.operator.domain.model.NetworkError
import com.example.ssgapp.operator.domain.repository.MachineryRepository
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject

class FakeActiveMachineryRepositoryImpl @Inject constructor(): ActiveMachineryRepository {

    override suspend fun getTodayActiveMachineries(): Either<NetworkError, Response<List<Machinery>>> {
        delay(1000L)

        return Either.catch {
            val machineryList = listOf(
                Machinery(
                    id = "AAAAAAA",
                    beacons = listOf(
                        Beacon(
                            id = "BEACON-1-center",
                            position = "center",
                            macAddress = "D8:F1:CA:B2:7A:04",
                            safetyDistance = 0.5
                        )/*,
                        Beacon(
                            id = "BEACON-1-front",
                            position = "front",
                            macAddress = "E8:10:FE:1D:58:42",
                            safetyDistance = 1.0
                        )*/
                    )
                ),
                Machinery(
                    id = "BBBBBB",
                    beacons = listOf(
                        Beacon(
                            id = "BEACON-1-front",
                            position = "front",
                            macAddress = "E8:10:FE:1D:58:42",
                            safetyDistance = 1.0
                        )
                    )
                )
            )

            val response: Response<List<Machinery>> = Response.success(machineryList)
            response
        }.mapLeft { it.toNetworkError() }
    }

}