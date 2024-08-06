package com.example.ssgapp.util

object Constant {
    // ifconfig | grep "inet " | grep -v 127.0.0.1
    //const val BASE_URL = "http://127.0.0.1"
    const val SERVER_MQTT_IP_ADDRESS = "tcp://192.168.1.65:1883"

    const val SERVER_IP_ADDRESS = "http://192.168.1.65"
    const val BASE_URL_LOGIN_MS = "${SERVER_IP_ADDRESS}:8080/api/authentication/"
    const val BASE_URL_SITE_MANAGEMENT_MS = "${SERVER_IP_ADDRESS}:8081/api/siteconfiguration/"
    const val BASE_URL_WORKER_MS = "${SERVER_IP_ADDRESS}:8081/api/workers/"
    const val BASE_URL_ALARMS_MS = "${SERVER_IP_ADDRESS}:8082"
}