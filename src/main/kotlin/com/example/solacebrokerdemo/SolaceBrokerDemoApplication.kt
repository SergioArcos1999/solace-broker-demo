package com.example.solacebrokerdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SolaceBrokerDemoApplication

fun main(args: Array<String>) {
	runApplication<SolaceBrokerDemoApplication>(*args)
}
