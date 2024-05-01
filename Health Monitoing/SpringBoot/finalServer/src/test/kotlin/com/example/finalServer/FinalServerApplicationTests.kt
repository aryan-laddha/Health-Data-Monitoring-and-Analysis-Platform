package com.example.finalServer

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class FinalServerApplicationTests {

	@Test
	fun contextLoads() {
	}
	@Test
	fun kotlinTest(){
		val HeartRateRequest = HeartRateRequest("user5", 68)
		println(HeartRateRequest)

	}

}
