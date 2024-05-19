package com.example.erp.kafka

import com.example.erp.event.Event

interface ErpKafkaProducer {
    suspend fun send(topic: String, data: String)
    suspend fun send(topic: String, event: Event)
}