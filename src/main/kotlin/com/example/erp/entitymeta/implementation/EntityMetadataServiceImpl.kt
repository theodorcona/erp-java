package com.example.erp.entitymeta.implementation

import com.example.erp.common.AnyWithId
import com.example.erp.entity.*
import com.example.erp.entity.implementation.IndexRegistry
import com.example.erp.entitymeta.EntityMetadata
import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class EntityMetadataServiceImpl(
    private val objectMapper: ObjectMapper,
    private val entityStore: EntityStore,
    private val indexRegistry: IndexRegistry
) : EntityMetadataService {
    private object ENTITY_METADATA_COLLECTION : CollectionDescriptor<EntityMetadata>(
        type = EntityMetadata::class.java,
        collectionName = "entityMetadata",
        indexedStringProperties = listOf(
            IndexedStringPropertyName("collection")
        )
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
        ).map(this::entityToDTO)
    }

    override fun getEntityMetadataForCollection(collection: String): AnyWithId<EntityMetadata> {
        return findCollectionByName(collection).items.singleOrNull()
            ?: throw IllegalStateException("Entity metadata doesn't exist for collection '$collection'")
    }

    override fun insertEntityMetadata(entityMetadata: EntityMetadata): AnyWithId<EntityMetadata> {
        if (entityCollectionExists(entityMetadata.collection)) {
            throw IllegalStateException("Entity metadata for collection '${entityMetadata.collection}' already exists")
        }
        val entityData = dtoToData(entityMetadata)
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
        return response.let(this::entityToDTO)
    }

    private fun entityToDTO(entity: Entity): AnyWithId<EntityMetadata> {
        val entityMetadata = objectMapper.readValue(entity.data, EntityMetadata::class.java)
        return AnyWithId(entity.id, entityMetadata)
    }

    private fun dtoToData(dto: EntityMetadata): String {
        return objectMapper.writeValueAsString(dto)
    }

    private fun findCollectionByName(collection: String): PageDTO<AnyWithId<EntityMetadata>> {
        val collectionProperty = StringCollectionProperty(collection, ENTITY_METADATA_COLLECTION.PROPERTIES.collection)
        val property = collectionProperty.toProperty(ENTITY_METADATA_COLLECTION.collectionName)
        return entityStore.findEntitiesByPropertyPaged(property, RangeQuery(null, 10)).map(this::entityToDTO)
    }
}