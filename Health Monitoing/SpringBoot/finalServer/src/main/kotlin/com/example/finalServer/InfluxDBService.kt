package com.example.finalServer

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import org.springframework.stereotype.Service

@Service
class InfluxDBService(
        private val influxDBProperties: InfluxDBProperties
) {

    // Method to write data to InfluxDB
    suspend fun writeToInfluxDB(data: String) {
        val client = InfluxDBClientKotlinFactory.create(
                influxDBProperties.url,
                influxDBProperties.token.toCharArray(),
                influxDBProperties.org,
                influxDBProperties.bucket
        )
        client.use {
            val writeApi = client.getWriteKotlinApi()

            // val record = "heartRate,userId=user2 rate=60"

            // Write the data to InfluxDB with specified precision
            writeApi.writeRecord(data, WritePrecision.NS)

            client.close()
        }
    }





}