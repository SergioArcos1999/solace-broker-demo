package com.example.solacebrokerdemo

import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.jms.JMSException
import javax.jms.Message
import javax.jms.TextMessage


@Component
class NonExclusiveExample(
    private val jmsTemplate: JmsTemplate,
) {
    private final val QUEUE_NAME = "non-exclusive-queue"
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
        if(SolaceBrokerDemoApplication.isNonExclusiveExample) {
            val msg = "Message $counter"
            printLog("PUBLISHER 1", "PUBLISHED: $msg")
            jmsTemplate.convertAndSend(QUEUE_NAME, msg)
            ++counter
        }
    }

    @JmsListener(destination = "non-exclusive-queue")
    fun consumer1(message: Message) {
        val receiveTime = Date()

        if (message is TextMessage) {
            val tm: TextMessage = message
            try {
                printLog("CONSUMER 1", "RECEIVED: ${tm.text}")
            } catch (e: JMSException) {
                e.printStackTrace()
            }
        } else {
            println(message.toString())
        }
    }

    @JmsListener(destination = "non-exclusive-queue")
    fun consumer2(message: Message) {
        val receiveTime = Date()

        if (message is TextMessage) {
            val tm: TextMessage = message
            try {
                printLog("CONSUMER 2", "RECEIVED: ${tm.text}")
            } catch (e: JMSException) {
                e.printStackTrace()
            }
        } else {
            println(message.toString())
        }
    }

    private fun printLog(responsible: String, message: String) =
        println("$responsible ==> $message")

}