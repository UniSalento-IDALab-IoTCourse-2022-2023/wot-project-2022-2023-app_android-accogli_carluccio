package com.example.ssgapp.common.data.repository

import arrow.core.Either
import com.example.ssgapp.common.data.remote.AuthenticationApi
import com.example.ssgapp.common.domain.model.Beacon
import com.example.ssgapp.common.domain.model.JwtToken
import com.example.ssgapp.common.domain.model.Machinery
import com.example.ssgapp.common.domain.model.User
import com.example.ssgapp.common.domain.repository.AuthenticationRepository
import com.example.ssgapp.operator.data.mapper.toNetworkError
import com.example.ssgapp.operator.domain.model.NetworkError
import retrofit2.Response
import javax.inject.Inject

class FakeAuthenticationRepositoryImpl @Inject constructor(
    private val authenticationApi: AuthenticationApi
): AuthenticationRepository {
    override suspend fun login(user: User): Either<NetworkError, JwtToken> {
        return Either.catch {
            if (user.username == "PX441JP" && user.password == "nIaXNHGfucO4") {
                // return jwt operatore da terra
                JwtToken("eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOlsiaHR0cDovL2xvZ2luU2VydmljZTo4MDgwIl0sInJvbGUiOiJHUk9VTkRfV09SS0VSIiwidXNlcklEIjoiNjY4MTI3YWJlNGMzZTMzY2M1OGVkNDNjIiwic3ViIjoiUFg0NDFKUCIsImlhdCI6MTcxOTkxNjM2MCwiaXNzIjoiaHR0cDovL2xvZ2luU2VydmljZTo4MDgwIiwiZXhwIjoxNzE5OTUyMzYwfQ.XbRNJJ6aq_WzfS79s-hN5S6OKRzEYAHYb2Cw-qf-IpU")
            } else if (user.username == "SR230DC" && user.password == "Ba0WQ1hXvS6t" && user.macAddress == "AA:AA:AA:AA:AA:AA") {
                // return jwt operatore alla guida
                JwtToken("eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOlsiaHR0cDovL2xvZ2luU2VydmljZTo4MDgwIl0sInJvbGUiOiJFUVVJUE1FTlRfT1BFUkFUT1IiLCJ1c2VySUQiOiI2NjgxMjdjMmU0YzNlMzNjYzU4ZWQ0M2QiLCJzdWIiOiJTUjIzMERDIiwiaWF0IjoxNzE5OTE2NDA4LCJpc3MiOiJodHRwOi8vbG9naW5TZXJ2aWNlOjgwODAiLCJleHAiOjE3MTk5NTI0MDh9.ZkHwbAq2n7Rzc8PmkN9-21Je57_bim_v52hiSRE91IM")
            } else {
                // lancia networkerror nel caso in cui non è ne uno ne l'altro
                throw Exception("An error occurred!") // TODO: mettere unauthorized
            }
        }.mapLeft { it.toNetworkError() }
    }
}