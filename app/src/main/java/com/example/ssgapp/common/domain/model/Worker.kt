package com.example.ssgapp.common.domain.model

import com.google.gson.annotations.SerializedName

data class Worker(
    @SerializedName("id") val workerId: String,
    @SerializedName("name") val name: String,
    @SerializedName("surname") val surname: String,
    @SerializedName("ssn") val ssn: String,
    @SerializedName("email") val email: String,
    @SerializedName("dateOfBirth") val birth: String,
    @SerializedName("generalLicence") val generalLicence: String? = null,
    @SerializedName("specificLicences") val specificLicences: List<String>? = null
)


