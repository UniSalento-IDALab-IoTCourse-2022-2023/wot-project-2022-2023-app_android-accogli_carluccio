package com.example.ssgapp.operator.data.mapper

import android.util.Log
import com.example.ssgapp.operator.domain.model.ApiError
import com.example.ssgapp.operator.domain.model.NetworkError
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toNetworkError(): NetworkError {
    val error = when (this) {
        is IOException -> ApiError.NetworkError
        is HttpException -> ApiError.UnknownResponse
        else -> ApiError.UnknownError
    }
    return NetworkError(
        error = error,
        t = this
    )
}