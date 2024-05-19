package com.example.erp.eventlink

import com.example.erp.entity.CollectionDescriptor
import com.example.erp.entity.IndexedStringPropertyName
import com.example.erp.eventmeta.EVENT_METADATA_COLLECTION
import com.example.erp.eventmeta.EventMetadata
import com.example.erp.logic.Input

object EVENT_LINK_COLLECTION : CollectionDescriptor<EventLink>(
    type = EventLink::class.java,
    collectionName = "eventLink",
    indexedStringProperties = listOf(
        IndexedStringPropertyName("inputEvent"),
    )
) {
    object PROPERTIES {
        val inputEvent = EVENT_LINK_COLLECTION.indexedStringProperties[0]
    }
}

data class EventLink(
    val inputEvent: String,
    val outputEvent: String,
    val properties: Map<String, Input>
)