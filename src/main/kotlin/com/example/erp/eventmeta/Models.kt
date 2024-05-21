package com.example.erp.eventmeta

import com.example.erp.common.*
import com.example.erp.entity.*
import com.fasterxml.jackson.databind.ObjectMapper

object EVENT_METADATA_COLLECTION : CollectionDescriptor<EventMetadata, EventMetadataDTO>(
    type = EventMetadata::class.java,
    dtoType= EventMetadataDTO::class.java,
    collectionName = "eventMetadata",
    indexedStringProperties = listOf(IndexedStringPropertyName("eventName")),
    fromDTO = { dto -> EventMetadata(dto.eventName, fromSchemaDTO(dto.schema)) },
    toDTO = { domain -> EventMetadataDTO(domain.eventName, domain.schema.toSchemaDTO()) }
) {
    object PROPERTIES {
        val eventName = indexedStringProperties.get(0)
    }
}

data class EventMetadata(
    val eventName: String,
    val schema: SchemaProperties.Schema
)

data class EventMetadataDTO(
    val eventName: String,
    val schema: SchemaDTO
)
