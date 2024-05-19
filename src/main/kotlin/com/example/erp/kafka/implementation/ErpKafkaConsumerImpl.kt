package com.example.erp.kafka.implementation

import com.example.erp.entity.EntityServiceFactory
import com.example.erp.event.Event
import com.example.erp.event.EventHandlerContext
import com.example.erp.event.EventHandlerRegistry
import com.example.erp.event.EventService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import kotlin.math.log

@Service
class ErpKafkaConsumerImpl(
    private val eventHandlerRegistry: EventHandlerRegistry,
    private val objectMapper: ObjectMapper,
    private val entityServiceFactory: EntityServiceFactory,
    private val eventService: EventService
) {
    private val logger = LoggerFactory.getLogger(ErpKafkaConsumerImpl::class.java)

    @KafkaListener(topics = ["events"], groupId = "events")
    fun consumeMessage(message: String?) {
        runBlocking {
            logger.info("Received message $message")
            message?.let {
                val event = objectMapper.readValue(it, Event::class.java)
                eventHandlerRegistry.getEventHandlers(event.eventName).forEach { handler ->
                    logger.info("handling")
                    kotlin.runCatching {
                        handler(EventHandlerContext(entityServiceFactory, eventService), event)
                    }.onFailure { logger.error("Failed consuming event", it) }
                }
            }
        }
    }
}