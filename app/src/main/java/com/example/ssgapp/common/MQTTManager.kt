package com.example.ssgapp.common

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.ssgapp.groundworker.domain.model.MessageAlert
import com.example.ssgapp.util.Constant.SERVER_MQTT_IP_ADDRESS
import com.google.gson.GsonBuilder
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttException
import org.json.JSONObject
import java.time.LocalDateTime

class MQTTManager(
    private val context: Context
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMQTTAlarm(
        messageAlert: MessageAlert
    ) {

        val alarmTopic = "zone/default/machinery/${messageAlert.machineryId}/alarms"

        // Invio alarm
        val gson = GsonBuilder().create()
        val payload = gson.toJson(messageAlert)
        println(payload)

        val clientId = MqttClient.generateClientId()
        val mqttClient = MqttClient(SERVER_MQTT_IP_ADDRESS, clientId, null)

        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                Log.d("MQTT", "connectionLost: ")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "messageArrived: ")

                // Handle your message here

                val payload = message?.payload?.toString(Charsets.UTF_8)
//                handleMessage(topic, payload)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTT", "deliveryComplete: ")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d("MQTT", "connectComplete: ")
                serverURI?.let {
                    Log.d("MQTT", "connectComplete serverURI: $it")
                }
                // publish message for test:
                publish(mqttClient, alarmTopic, payload, 1)

                // Subscribe to your topic here
//                subscribe(topicId, QOS)
            }
        })


        try {
            Log.d("MQTT", "connect: ")
            // Set up the connection options
            val mqttConnectOptions = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
            }

            mqttClient.connect(mqttConnectOptions)
        } catch (ex: MqttException) {
            Log.d("MQTT", "Connection failure")
//            handleConnectionFailure(ex)
        }
    }



    fun publish(mqttClient: MqttClient, topic: String, msg: String, qos: Int = 0) {
        try {
            val mqttMessage = MqttMessage(msg.toByteArray())
            mqttClient.publish(topic, mqttMessage.payload, qos, false)
            Log.d("MQTT", "Message published to topic `$topic`: $msg")
        } catch (e: MqttException) {
            Log.d("MQTT", "Error publishing to $topic: " + e.message, e)
            // Handle publishing failure
        }
    }






/*
    private val clientId = "mqtt-publisher"//MqttClient.generateClientId()
    private val mqttClient = MqttClient("tcp://192.168.1.65:1883", clientId, null)

    fun connect(onConnected: () -> Unit) {
        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
        }

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                onConnected()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                exception?.printStackTrace()
            }
        })
    }

    fun publish(topic: String, payload: String) {
        val mqttMessage = MqttMessage().apply {
            this.payload = payload.toByteArray()
            qos = 1
            isRetained = false
        }

        try {
            mqttClient.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun setCallback(callback: MqttCallback) {
        mqttClient.setCallback(callback)
    }

    fun disconnect() {
        try {
            mqttClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }*/


    fun basic_MQTT_PUBLISH_TEST() {
        val topic = "zone/default/machinery/66704a537bd0c2056dcefa29/alarms"


        /**
         * ESEMPIO DI ALARM DI INGRESSO IN UNA ZONA DI PERICOLO
         *
         * __Nel caso di comunicazione di uscita dalla zona di pericolo impostare
         *   priority: "COMMUNICATION" e isEntryAlarm: false __
         */


        /**
         * NOTA BENE!!! Type e Priority vanno specificati in maiuscolo! Perchè sono tipi e non stringhe
         * Quindi type = DISTANCE | GENERAL
         *        priority = COMMUNICATION | WARNING | DANGER
         * Il controllo duplicati invece avviene tramite timestamp senza necessità di inserire un ID univoco
         */
        val distance_alert = JSONObject()
        distance_alert.put("timestamp", LocalDateTime.now())
        distance_alert.put("type", "DISTANCE")
        distance_alert.put("technologyID", "beacons")
        distance_alert.put("priority", "WARNING")
        distance_alert.put("workerID", "dana")
        distance_alert.put("machineryID", "escavatore")
        distance_alert.put("isEntryAlarm", true)


        /**
         *  ESEMPIO DI ALARM GENERALE
         */
        /*JSONObject general_alert = new JSONObject();
                general_alert.put("timestamp", LocalDateTime.now());
                general_alert.put("type", "GENERAL");
                general_alert.put("technologyID", "mqtt"); // qualsiasi stringa, non è vincolante
                general_alert.put("priority", "warning");
                general_alert.put("description", "Incendio in corso. Evacuare");*/


        // Invio alarm -distance || general-
        /**
         * ESEMPIO DI ALARM GENERALE
         */
        /*JSONObject general_alert = new JSONObject();
                general_alert.put("timestamp", LocalDateTime.now());
                general_alert.put("type", "GENERAL");
                general_alert.put("technologyID", "mqtt"); // qualsiasi stringa, non è vincolante
                general_alert.put("priority", "warning");
                general_alert.put("description", "Incendio in corso. Evacuare");*/
        //val payload = Json.encodeToString(distanceAlert)

        // Invio alarm -distance || general-
        //https://www.json2kt.com/
        val gson = GsonBuilder().create()
        val payload = gson.toJson(distance_alert)
        //
        println(payload)

        val clientId = MqttClient.generateClientId() //"mqtt-publisher"//
        val mqttClient = MqttClient("tcp://192.168.1.65:1883", clientId, null)

        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                Log.d("MQTT", "connectionLost: ")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "messageArrived: ")

                // Handle your message here

                val payload = message?.payload?.toString(Charsets.UTF_8)
//                handleMessage(topic, payload)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d("MQTT", "deliveryComplete: ")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d("MQTT", "connectComplete: ")
                serverURI?.let {
                    Log.d("MQTT", "connectComplete serverURI: $it")
                }
                // publish message for test:
                publish(mqttClient, topic, payload, 1)

                // Subscribe to your topic here
//                subscribe(topicId, QOS)
            }
        })


        try {
            Log.d("MQTT", "connect: ")
            // Set up the connection options
            val mqttConnectOptions = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
            }

            mqttClient.connect(mqttConnectOptions)
        } catch (ex: MqttException) {
            Log.d("MQTT", "Connection failure")
//            handleConnectionFailure(ex)
        }
    }
}



