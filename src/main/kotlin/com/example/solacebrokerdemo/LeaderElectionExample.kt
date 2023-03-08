package com.example.solacebrokerdemo

import org.redisson.Redisson
import org.redisson.api.RLock
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.jms.JMSException
import javax.jms.Message
import javax.jms.TextMessage


@Component
@EnableScheduling
class LeaderElectionExample(
    private val jmsTemplate: JmsTemplate,
) {
    private final val QUEUE_1_NAME = "exclusive-queue-1"
    private final val QUEUE_2_NAME = "exclusive-queue-2"

    private val redisClient = Redisson.create()
    private val lock: RLock = redisClient.getLock("myLock")
    private var counter = 1

    @PostConstruct
    private fun customizeJmsTemplate() {
        // Update the jmsTemplate's connection factory to cache the connection
        val ccf = CachingConnectionFactory()
        ccf.targetConnectionFactory = jmsTemplate.connectionFactory
        jmsTemplate.connectionFactory = ccf
        // By default Spring Integration uses Queues, but if you set this to true you
        // will send to a PubSub+ topic destination
        jmsTemplate.isPubSubDomain = false
    }

    @Throws(Exception::class)
    @Scheduled(
        fixedRate = SolaceBrokerDemoApplication.publishRate,
        initialDelay = SolaceBrokerDemoApplication.initialDelay
    )
    fun sendMessage() {
        if(SolaceBrokerDemoApplication.isLeaderElectionExample) {
            val msg = "Message $counter"
            printLog("PUBLISHER 1", "PUBLISHED: $msg")
            //Simulate that queues are subscribed to the same topic, so both will receive the same message
            jmsTemplate.convertAndSend(QUEUE_1_NAME, msg)
            jmsTemplate.convertAndSend(QUEUE_2_NAME, msg)
            ++counter
        }
    }

    @JmsListener(destination = "exclusive-queue-1")
    fun consumer1(message: Message) {
        if(lock.isLocked) printLog("CONSUMER 2", "Cannot acquire lock because it is taken")
        lock.lock()
        printLog("CONSUMER 1", "Lock acquired")
        val receiveTime = Date()

        if (message is TextMessage) {
            val tm: TextMessage = message
            try {
                printLog("CONSUMER 1", "RECEIVED: ${tm.text}")
            } catch (e: JMSException) {
                e.printStackTrace()
            } finally {
                lock.unlock()
                printLog("CONSUMER 1", "Lock unlocked")
            }
        } else {
            println(message.toString())
        }
    }

    @JmsListener(destination = "exclusive-queue-2")
    fun consumer2(message: Message) {
        val receiveTime = Date()
        if(lock.isLocked) printLog("CONSUMER 2", "Cannot acquire lock because it is taken")
        lock.lock()
        printLog("CONSUMER 2", "Lock acquired")

        if (message is TextMessage) {
            val tm: TextMessage = message
            try {
                printLog("CONSUMER 2", "RECEIVED: ${tm.text}")
            } catch (e: JMSException) {
                e.printStackTrace()
            } finally {
                lock.unlock()
                printLog("CONSUMER 2", "Lock unlocked")
            }
        } else {
            println(message.toString())
        }
    }

    private fun printLog(responsible: String, message: String) =
        println("$responsible ==> $message")

}