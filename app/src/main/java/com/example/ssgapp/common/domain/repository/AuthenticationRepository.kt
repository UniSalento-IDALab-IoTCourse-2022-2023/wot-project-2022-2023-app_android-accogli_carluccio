package com.example.ssgapp.common.domain.repository

import arrow.core.Either
import com.example.ssgapp.common.domain.model.JwtToken
import com.example.ssgapp.common.domain.model.User
import com.example.ssgapp.operator.domain.model.NetworkError
import retrofit2.Response

interface AuthenticationRepository {
    suspend fun login(user: User): Either<NetworkError, JwtToken>

}