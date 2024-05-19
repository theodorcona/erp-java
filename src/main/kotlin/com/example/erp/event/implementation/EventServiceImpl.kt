package com.example.erp.event.implementation

import com.example.erp.entity.EntityServiceFactory
import com.example.erp.entity.StringCollectionProperty
import com.example.erp.entitymeta.SchemaService
import com.example.erp.event.Event
import com.example.erp.event.EventDescriptor
import com.example.erp.event.EventService
import com.example.erp.eventmeta.EVENT_METADATA_COLLECTION
import com.example.erp.kafka.ErpKafkaProducer
import com.example.erp.rest.RangeQuery
import org.springframework.stereotype.Service

@Service
class EventServiceImpl(
    private val schemaService: SchemaService,
    private val kafkaProducer: ErpKafkaProducer,
    entityServiceFactory: EntityServiceFactory
) : EventService {

    private val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)

    suspend override fun sendEvent(event: Event) {
        val eventMetadata = eventMetadataService.getByProperty(
            StringCollectionProperty(event.eventName, EVENT_METADATA_COLLECTION.PROPERTIES.eventName),
            RangeQuery(null, 2)
        ).items.singleOrNull()

        checkNotNull(eventMetadata) { "No event metadata for event name '${event.eventName}'"}
        check(schemaService.dataFitsSchema(eventMetadata.obj.schema, event.data)) {
            "Event doesn't fit registered schema"
        }
        kafkaProducer.send("events", event)
    }
}