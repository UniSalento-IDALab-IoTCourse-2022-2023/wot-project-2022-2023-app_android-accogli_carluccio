package com.example.ssgapp.common.domain.model

import com.google.gson.annotations.SerializedName

data class Machinery (
    @SerializedName("machineryID") val id: String,
    @SerializedName("machineryName")  val name: String? = null,
    @SerializedName("machineryType")  val type: String? = null,
    @SerializedName("machinerySerialNumber")  val serialNumber: String? = null,
    @SerializedName("boardMacBLE") val macAddress: String? = null,
    @SerializedName("beaconList") val beacons: List<Beacon>? = null,
    @SerializedName("isRemote") val isRemote: Boolean? = null
)

data class Beacon(
    val id: String,
    val position: String,
    val macAddress: String,
    val safetyDistance: Double,
    val machineryId: String? = null
)

data class GroupedBeacon(
    val machineryId: String,
    val beacons: MutableList<Beacon>
)