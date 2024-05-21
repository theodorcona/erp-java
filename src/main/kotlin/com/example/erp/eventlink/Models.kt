package com.example.erp.eventlink

import com.example.erp.common.fromInputDTO
import com.example.erp.common.toDate
import com.example.erp.common.toInputDTO
import com.example.erp.entity.CollectionDescriptor
import com.example.erp.entity.IndexedStringPropertyName
import com.example.erp.eventmeta.EVENT_METADATA_COLLECTION
import com.example.erp.eventmeta.EventMetadata
import com.example.erp.logic.Input
import com.example.erp.logic.InputDTO

object EVENT_LINK_COLLECTION : CollectionDescriptor<EventLink, EventLinkDTO>(
    type = EventLink::class.java,
    dtoType = EventLinkDTO::class.java,
    collectionName = "eventLink",
    indexedStringProperties = listOf(
        IndexedStringPropertyName("inputEvent"),
    ),
    fromDTO = { dto -> EventLink(dto.inputEvent, dto.outputEvent, dto.properties.mapValues { fromInputDTO(it.value) }) },
    toDTO = { domain ->
        EventLinkDTO(
            domain.inputEvent, domain.outputEvent, domain.properties.mapValues { it.value.toInputDTO() }
        )
    }

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

data class EventLinkDTO(
    val inputEvent: String,
    val outputEvent: String,
    val properties: Map<String, InputDTO>
)
