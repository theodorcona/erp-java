package com.example.erp.eventlink.implementation

import com.example.erp.common.SchemaProperties
import com.example.erp.entity.EntityServiceFactory
import com.example.erp.entity.StringCollectionProperty
import com.example.erp.entitymeta.SchemaService
import com.example.erp.eventlink.EVENT_LINK_COLLECTION
import com.example.erp.eventlink.EventLink
import com.example.erp.eventlink.EventLinkService
import com.example.erp.eventmeta.EVENT_METADATA_COLLECTION
import com.example.erp.logic.ApplyInput
import com.example.erp.logic.Input
import com.example.erp.logic.OtherDataInput
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import org.springframework.stereotype.Service

@Service
class EventLinkServiceImpl(
    private val schemaService: SchemaService,
    entityServiceFactory: EntityServiceFactory
) : EventLinkService {
    private val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)
    private val eventLinkService = entityServiceFactory.getServiceForEntity(EVENT_LINK_COLLECTION)
    override fun insertEventLink(eventLink: EventLink) {
        val inputEventMetadata = eventMetadataService.getByProperty(
            StringCollectionProperty(eventLink.inputEvent, EVENT_METADATA_COLLECTION.PROPERTIES.eventName),
            RangeQuery(null, 2)
        ).items.singleOrNull()
        val outputEventMetadata = eventMetadataService.getByProperty(
            StringCollectionProperty(eventLink.inputEvent, EVENT_METADATA_COLLECTION.PROPERTIES.eventName),
            RangeQuery(null, 2)
        ).items.singleOrNull()

        checkNotNull(inputEventMetadata)
        checkNotNull(outputEventMetadata)

        val pathsToOutputType = eventLink.properties.mapValues { it.value.outputType }
        val inputPathToType = mapInputPathToInputType(eventLink.properties.values).toMap()

        schemaService.pathsFitSchema(outputEventMetadata.obj.schema, pathsToOutputType)
        schemaService.schemaContainsPaths(inputEventMetadata.obj.schema, inputPathToType)

        eventLinkService.insert(eventLink)
    }

    override fun getEventLinksForInputEvent(inputEvent: String, rangeQuery: RangeQuery): PageDTO<EventLink> {
        return eventLinkService.getByProperty(
            StringCollectionProperty(inputEvent, EVENT_LINK_COLLECTION.PROPERTIES.inputEvent), rangeQuery
        ).map { it.obj }
    }

    private fun mapInputPathToInputType(inputs: Collection<Input>): List<Pair<String, SchemaProperties.PropertyType>> {
        return inputs.flatMap {
            when (it) {
                is OtherDataInput -> listOf(it.path to it.outputType)
                is ApplyInput -> mapInputPathToInputType(inputs)
                else -> listOf()
            }
        }
    }
}