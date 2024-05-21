package com.example.erp.organization

import com.example.erp.entity.NoDTOCollectionDescriptor
import java.util.*

object ORGANIZATION_COLLECTION : NoDTOCollectionDescriptor<Organization>(
    Organization::class.java,
    "organization"
)


data class Organization(
    val id: UUID,
    val userIds: List<UUID>
)