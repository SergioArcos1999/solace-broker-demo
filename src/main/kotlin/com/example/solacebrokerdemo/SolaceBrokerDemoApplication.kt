package com.example.solacebrokerdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SolaceBrokerDemoApplication {
	companion object Globals {
		//A single queue with 1 producer & 2 consumers
		var isNonExclusiveExample = true

		//A queue for each consumer + locking
		var isLeaderElectionExample = false
	}
}

fun main(args: Array<String>) {
	runApplication<SolaceBrokerDemoApplication>(*args)
}
