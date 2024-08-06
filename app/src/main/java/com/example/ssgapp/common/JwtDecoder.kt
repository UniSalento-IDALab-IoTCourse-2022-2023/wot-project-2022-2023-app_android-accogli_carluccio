package com.example.ssgapp.common

import com.example.ssgapp.common.domain.model.JwtPayload
import com.example.ssgapp.common.domain.model.UserRole
import com.google.gson.Gson
import java.util.Base64


class JwtDecoder(private val gson: Gson = Gson()) {

    fun decode(jwt: String): JwtPayload {
        val parts = jwt.split(".")
        if (parts.size != 3) throw IllegalArgumentException("Invalid JWT token")

        val payload = String(Base64.getUrlDecoder().decode(parts[1]))
        return gson.fromJson(payload, JwtPayload::class.java)
    }

    fun getRole(jwt: String): UserRole? {
        val payload = decode(jwt)
        return UserRole.fromString(payload.role)
    }

    fun getUserId(jwt: String): String {
        val payload = decode(jwt)
        return payload.userId
    }
}