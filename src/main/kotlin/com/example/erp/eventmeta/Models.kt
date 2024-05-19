package com.example.erp.eventmeta

import com.example.erp.common.SchemaProperties
import com.example.erp.entity.*

object EVENT_METADATA_COLLECTION : CollectionDescriptor<EventMetadata>(
    type = EventMetadata::class.java,
    collectionName = "eventMetadata",
    indexedStringProperties = listOf(IndexedStringPropertyName("eventName"))
) {
    object PROPERTIES {
        val eventName = indexedStringProperties.get(0)
    }
}

data class EventMetadata(
    val eventName: String,
    val schema: SchemaProperties.Schema
)