package com.example.finalServer

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/heartRate")
class HeartRateController(
        private val influxDBService: InfluxDBService
) {

    @PostMapping
    suspend fun createHeartRate(@RequestBody request: HeartRateRequest): ResponseEntity<Any?> {
        val data = "heartRate,userId=${request.userid} rate=${request.rate}"
        influxDBService.writeToInfluxDB(data)
        return ResponseEntity(HttpStatus.CREATED)
    }
}
