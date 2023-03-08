package com.example.solacebrokerdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SolaceBrokerDemoApplication {
	companion object Globals {
		//EXAMPLE 1: A single queue with 1 producer & 2 consumers
		const val isNonExclusiveExample = true

		//EXAMPLE 2: A queue for each consumer + locking following leader election pattern
		const val isLeaderElectionExample = false

		//Generic modifiers:
		const val publishRate: Long = 5000
		const val initialDelay: Long = 5000
	}
}

fun main(args: Array<String>) {
	runApplication<SolaceBrokerDemoApplication>(*args)
}
