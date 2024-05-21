package com.example.erp.entitymeta.implementation

import com.example.erp.common.AnyWithId
import com.example.erp.common.fromEntityMetadataDTO
import com.example.erp.common.toDTO
import com.example.erp.entity.*
import com.example.erp.entity.implementation.IndexRegistry
import com.example.erp.entitymeta.EntityMetadata
import com.example.erp.entitymeta.EntityMetadataDTO
import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class EntityMetadataServiceImpl(
    private val objectMapper: ObjectMapper,
    private val entityStore: EntityStore,
    private val indexRegistry: IndexRegistry
) : EntityMetadataService {
    private object ENTITY_METADATA_COLLECTION : CollectionDescriptor<EntityMetadata, EntityMetadataDTO>(
        type = EntityMetadata::class.java,
        dtoType = EntityMetadataDTO::class.java,
        collectionName = "entityMetadata",
        indexedStringProperties = listOf(
            IndexedStringPropertyName("collection")
        ),
        toDTO = { domain -> domain.toDTO() },
        fromDTO = { dto -> fromEntityMetadataDTO(dto) }
    ) {
        object PROPERTIES {
            val collection = indexedStringProperties.get(0)
        }
    }

    override fun entityCollectionExists(collection: String): Boolean {
        return findCollectionByName(collection).items.isNotEmpty()
    }

    override fun getAllEntityMetadata(rangeQuery: RangeQuery): PageDTO<AnyWithId<EntityMetadata>> {
        return entityStore.findAllEntitiesInCollectionPaged(
            ENTITY_METADATA_COLLECTION.collectionName,
            rangeQuery
        ).map(this::entityDataToDomain)
    }

    override fun getEntityMetadataForCollection(collection: String): AnyWithId<EntityMetadata> {
        return findCollectionByName(collection).items.singleOrNull()
            ?: throw IllegalStateException("Entity metadata doesn't exist for collection '$collection'")
    }

    override fun insertEntityMetadata(entityMetadata: EntityMetadata): AnyWithId<EntityMetadata> {
        if (entityCollectionExists(entityMetadata.collection)) {
            throw IllegalStateException("Entity metadata for collection '${entityMetadata.collection}' already exists")
        }
        val entityData = domainToData(entityMetadata)
        val response = entityStore.insertEntity(entityData, ENTITY_METADATA_COLLECTION.collectionName)
        if (findCollectionByName(entityMetadata.collection).items.size > 1) {
            entityStore.deleteEntityById(response.id)
            throw IllegalStateException("Detected concurrent write, cancelling")
        }
        indexRegistry.insert(
            StringProperty(
                entityMetadata.collection,
                ENTITY_METADATA_COLLECTION.collectionName,
                ENTITY_METADATA_COLLECTION.PROPERTIES.collection
            ), response.id
        )
        return response.let(this::entityDataToDomain)
    }

    private fun entityDataToDomain(entity: Entity): AnyWithId<EntityMetadata> {
        val entityMetadataDTO = objectMapper.readValue(entity.data, EntityMetadataDTO::class.java)
        return AnyWithId(entity.id, fromEntityMetadataDTO(entityMetadataDTO))
    }

    private fun domainToData(dto: EntityMetadata): String {
        return objectMapper.writeValueAsString(dto.toDTO())
    }

    private fun findCollectionByName(collection: String): PageDTO<AnyWithId<EntityMetadata>> {
        val collectionProperty = StringCollectionProperty(collection, ENTITY_METADATA_COLLECTION.PROPERTIES.collection)
        val property = collectionProperty.toProperty(ENTITY_METADATA_COLLECTION.collectionName)
        return entityStore.findEntitiesByPropertyPaged(property, RangeQuery(null, 10)).map(this::entityDataToDomain)
    }
}