package com.example.erp.event.implementation

import com.example.erp.common.firstUuid
import com.example.erp.entity.EntityServiceFactory
import com.example.erp.entity.StringCollectionProperty
import com.example.erp.event.*
import com.example.erp.eventlink.EventLinkService
import com.example.erp.eventmeta.EVENT_METADATA_COLLECTION
import com.example.erp.logic.DataTransformer
import com.example.erp.rest.RangeQuery
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventHandlerRegistryImpl(
    entityServiceFactory: EntityServiceFactory,
    val eventLinkService: EventLinkService,
    val dataTransformer: DataTransformer,
    val eventService: EventService
) : EventHandlerRegistry {

    private val logger = LoggerFactory.getLogger(EventHandlerRegistryImpl::class.java)
    private val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)
    private val eventHandlers = mutableMapOf<String, List<suspend (EventHandlerContext, Event) -> Unit>>()

    override fun <T : Any> registerEventHandler(
        eventDescriptor: EventDescriptor<T>,
        block: suspend (EventHandlerContext, Event) -> Unit
    ): Boolean {
        val eventMetadata = eventMetadataService.getByProperty(
            StringCollectionProperty(eventDescriptor.eventName, EVENT_METADATA_COLLECTION.PROPERTIES.eventName),
            RangeQuery(null, 2)
        )
        if (eventMetadata.items.isEmpty()) {
            logger.error("Failed registering event handler. No event with name '${eventDescriptor.eventName}'")
            return false
        }
        val previous = eventHandlers.getOrDefault(eventDescriptor.eventName, emptyList())
        eventHandlers[eventDescriptor.eventName] = previous.plus(block)
        return true
    }

    override fun getEventHandlers(eventName: String): List<suspend (EventHandlerContext, Event) -> Unit> {
        val items = eventLinkService.getEventLinksForInputEvent(
            eventName,
            RangeQuery(firstUuid, 1000)
        ).items
        val eventLinkHandlers: List<suspend (EventHandlerContext, Event) -> Unit> =
            items.map { eventLink ->
                { ctx, event ->
                    val out = dataTransformer.transform(event.data, eventLink.properties)
                    val outEvent =
                        Event(eventLink.outputEvent, ApplicationUser(UUID.randomUUID(), ApplicationUser.Type.APP), out)
                    ctx.eventService.sendEvent(outEvent)
                }
            }
        return (eventHandlers[eventName] ?: listOf()).plus(eventLinkHandlers)
    }
}