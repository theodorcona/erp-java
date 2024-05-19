package com.example.erp.rest

import com.example.erp.common.AnyWithId
import com.example.erp.entity.EntityServiceFactory
import com.example.erp.entitymeta.EntityMetadata
import com.example.erp.event.ApplicationUser
import com.example.erp.event.Event
import com.example.erp.event.EventService
import com.example.erp.eventlink.EVENT_LINK_COLLECTION
import com.example.erp.eventlink.EventLink
import com.example.erp.eventmeta.EVENT_METADATA_COLLECTION
import com.example.erp.eventmeta.EventMetadata
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class EventController(
    private val eventService: EventService,
    entityServiceFactory: EntityServiceFactory
) {
    private val eventMetadataService = entityServiceFactory.getServiceForEntity(EVENT_METADATA_COLLECTION)
    private val eventLinkService = entityServiceFactory.getServiceForEntity(EVENT_LINK_COLLECTION)

    @PostMapping("/events/{eventName}")
    fun createEntityMetadata(
        @PathVariable eventName: String,
        @RequestBody eventRequest: EventRequest
    ) {
        runBlocking {
            eventService.sendEvent(
                Event(
                    eventName,
                    createdBy = ApplicationUser(UUID.randomUUID(), ApplicationUser.Type.USER),
                    data = eventRequest.data
                )
            )
        }
    }

    @PostMapping("/events")
    fun createEntityMetadata(
        @RequestBody eventMetadata: EventMetadata
    ): AnyWithId<EventMetadata> {
        return eventMetadataService.insert(eventMetadata)
    }

    @PostMapping("/events/links")
    fun createEventLink(@RequestBody eventLink: EventLink): AnyWithId<EventLink> {
        return eventLinkService.insert(eventLink)
    }

    @GetMapping("/events/links")
    fun getEventLinks(
        @RequestParam("cursor") cursor: String?,
        @RequestParam("pageSize") pageSize: Int
    ): PageDTO<AnyWithId<EventLink>> {
        return eventLinkService.getAll(RangeQuery(cursor, pageSize))
    }
}

data class EventRequest(
    val data: String
)
