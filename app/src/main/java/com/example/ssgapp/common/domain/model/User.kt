package com.example.ssgapp.common.domain.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("macAddress") val macAddress: String,
)

data class JwtToken(
    @SerializedName("jwt") val jwt: String
)


// Classi usate per estrarre il ruolo dal jwt
data class JwtPayload(
    @SerializedName("role") val role: String,
    @SerializedName("userID") val userId: String
)

enum class UserRole(val roleString: String) {
    GROUND_WORKER("GROUND_WORKER"),
    EQUIPMENT_OPERATOR("EQUIPMENT_OPERATOR");

    companion object {
        fun fromString(roleString: String): UserRole? {
            return values().find { it.roleString == roleString }
        }
    }
}