package com.example.solacebrokerdemo

import com.solacesystems.jms.SolJmsUtility
import org.springframework.context.annotation.Bean
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.PostConstruct
import javax.jms.ConnectionFactory
import javax.jms.JMSException
import javax.jms.Message
import javax.jms.TextMessage


@Component
@EnableScheduling
class BrokerManager(
    private val jmsTemplate: JmsTemplate,
) {
    private val queueName = "my-queue"

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
    @Scheduled(fixedRate = 5000)
    fun sendMessage() {
        val msg = "Hello World " + System.currentTimeMillis()
        println("PUBLISHER 1 ==> Message Published with message content of: $msg")
        jmsTemplate.convertAndSend(queueName, msg)
    }

    @JmsListener(destination = "my-queue")
    private fun consumer1(message: Message) {
        val receiveTime = Date()

        if (message is TextMessage) {
            val tm: TextMessage = message
            try {
                System.out.println(
                    "CONSUMER 1 ==> Message Received with message content of: ${tm.text}"
                )
            } catch (e: JMSException) {
                e.printStackTrace()
            }
        } else {
            System.out.println(message.toString())
        }
    }

    @JmsListener(destination = "my-queue")
    private fun consumer2(message: Message) {
        val receiveTime = Date()

        if (message is TextMessage) {
            val tm: TextMessage = message
            try {
                System.out.println(
                    "CONSUMER 2 ==> Message Received with message content of: ${tm.text}"
                )
            } catch (e: JMSException) {
                e.printStackTrace()
            }
        } else {
            System.out.println(message.toString())
        }
    }
}