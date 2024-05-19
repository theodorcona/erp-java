package com.example.erp.eventlink

import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery

interface EventLinkService {
    fun insertEventLink(eventLink: EventLink)
    fun getEventLinksForInputEvent(inputEvent: String, rangeQuery: RangeQuery): PageDTO<EventLink>
}