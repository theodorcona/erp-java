package com.example.erp.entity

import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.entitymeta.SchemaService

class GenericEntityService(
    collectionName: String,
    entityStore: EntityStore,
    entityMetadataService: EntityMetadataService,
    schemaService: SchemaService,
    indexService: IndexService
) : AbstractEntityService<String, String>(
    collectionName,
    entityStore,
    entityMetadataService,
    schemaService,
    indexService
) {
    override fun entityToDomain(entity: Entity): String {
        return entity.data
    }

    override fun domainToData(dto: String): String {
        return dto
    }
}