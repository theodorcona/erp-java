package com.example.erp.organization

import com.example.erp.entity.CollectionDescriptor
import com.example.erp.eventmeta.EventMetadata
import java.util.*

object ORGANIZATION_COLLECTION : CollectionDescriptor<Organization>(
    Organization::class.java,
    "organization"
)


data class Organization(
    val id: UUID,
    val userIds: List<UUID>
)