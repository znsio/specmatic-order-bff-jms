package com.component.orders.jms

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.springframework.stereotype.Component
import java.io.Serializable
import javax.jms.Session

@Component
class JmsSender @Autowired constructor(private val jmsTemplate: JmsTemplate) {
    fun sendObjectMessage(channel:String,  message:Any) {
        jmsTemplate.send(channel, MessageCreator { session: Session ->
            val objectMessage = session.createObjectMessage(message as Serializable)
            objectMessage
        })
    }
}