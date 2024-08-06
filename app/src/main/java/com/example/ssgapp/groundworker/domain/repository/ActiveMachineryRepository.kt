package com.example.ssgapp.groundworker.domain.repository

import arrow.core.Either
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.operator.domain.model.NetworkError
import retrofit2.Response

interface ActiveMachineryRepository {
    suspend fun getTodayActiveMachineries(): Either<NetworkError, Response<List<Machinery>>> // Response<...> l'ho messa solo perche se restituisce 201 non content, mi andava in errore, quando invece io voglio gestirlo
}