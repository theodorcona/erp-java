package com.example.erp.kafka.implementation

import com.example.erp.event.Event
import com.example.erp.kafka.ErpKafkaProducer
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.future.await
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Service
class ErpKafkaProducerImpl(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : ErpKafkaProducer {
    private val logger = LoggerFactory.getLogger(ErpKafkaConsumerImpl::class.java)

    override suspend fun send(topic: String, data: String) {
        logger.info("Sending to topic '$topic': '$data'")
        kafkaTemplate.send(ProducerRecord(topic, data)).await()
    }

    override suspend fun send(topic: String, event: Event) {
        val data = objectMapper.writeValueAsString(event)
        send(topic, data)
    }

}