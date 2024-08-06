package com.example.ssgapp.common.data.repository

import android.util.Log
import arrow.core.Either
import com.example.ssgapp.common.data.remote.AuthenticationApi
import com.example.ssgapp.common.domain.model.JwtToken
import com.example.ssgapp.common.domain.model.User
import com.example.ssgapp.common.domain.repository.AuthenticationRepository
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.operator.domain.model.NetworkError
import retrofit2.Response
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val authenticationApi: AuthenticationApi
): AuthenticationRepository {
    override suspend fun login(user: User): Either<NetworkError, JwtToken> {
        return Either.catch {
            authenticationApi.login(user)
        }.mapLeft {
            it.toNetworkError()
        }
    }

}