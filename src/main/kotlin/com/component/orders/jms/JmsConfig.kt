package com.component.orders.jms

import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.core.JmsTemplate
import javax.jms.ConnectionFactory

@Configuration
class JmsConfig {

    @Value("\${jms.broker-url}")
    lateinit var jmsBrokerUrl: String

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val connectionFactory = ActiveMQConnectionFactory()
        connectionFactory.brokerURL = jmsBrokerUrl
        return connectionFactory
    }

    @Bean
    fun jmsTemplate(connectionFactory: ConnectionFactory): JmsTemplate {
        return JmsTemplate(connectionFactory)
    }
}