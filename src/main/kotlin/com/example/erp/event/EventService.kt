package com.example.erp.event

import com.example.erp.entity.EntityServiceFactory
import java.util.*

interface EventService {
    suspend fun sendEvent(event: Event)
}

data class EventHandlerContext(
    val entityServiceFactory: EntityServiceFactory,
    val eventService: EventService
)

interface EventHandlerRegistry {
    fun <T : Any> registerEventHandler(eventDescriptor: EventDescriptor<T>, block: suspend (EventHandlerContext, Event) -> Unit) : Boolean
    fun getEventHandlers(eventName: String): List<suspend (EventHandlerContext, Event) -> Unit>
}

data class Event(
    val eventName: String,
    val createdBy: ApplicationUser,
    val data: String
)

data class ApplicationUser(val id: UUID, val type: Type) {
    enum class Type {
        USER,
        APP
    }
}

data class EventDescriptor<T>(
    val type: Class<T>,
    val eventName: String
) {
    override fun hashCode(): Int {
        return eventName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is EventDescriptor<*>) {
            return false
        }
        return eventName == other.eventName
    }
}