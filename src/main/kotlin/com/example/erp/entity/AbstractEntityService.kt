package com.example.erp.entity

import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.entitymeta.SchemaService
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import java.util.*

/**
 * Convenience class to access entities
 *
 * @param Existing The type representing an existing entity.
 * @param New The type representing a new entity to be inserted.
 * @property collection The name of the collection in which entities are stored.
 * @property entityStore The service responsible for storing and retrieving entities.
 * @property entityMetadataService The service responsible for retrieving entity metadata.
 * @property schemaService The service responsible for managing entity schemas.
 * @property indexService The service responsible for managing entity indexes.
 */
abstract class AbstractEntityService<Existing, New>(
    val collection: String,
    val entityStore: EntityStore,
    val entityMetadataService: EntityMetadataService,
    val schemaService: SchemaService,
    val indexService: IndexService
) {
    abstract fun entityToDomain(entity: Entity): Existing
    abstract fun domainToData(dto: New): String

    protected fun getEntitiesAllPaged(rangeQuery: RangeQuery): PageDTO<Existing> {
        return entityStore.findAllEntitiesInCollectionPaged(collection, rangeQuery).map(this::entityToDomain)
    }

    protected fun findById(id: UUID): Existing? {
        return entityStore.findEntityById(id)
            ?.takeIf { it.collection == collection }
            ?.let(this::entityToDomain)
    }

    protected fun findByPropertyPaged(
        collectionProperty: IndexedCollectionProperty<Any>,
        rangeQuery: RangeQuery
    ): PageDTO<Existing> {
        val property = collectionProperty.toProperty(collection)
        val schema = entityMetadataService.getEntityMetadataForCollection(collection).obj.schema
        check(schemaService.schemaContainsIndexedProperty(schema, property)) {
            "Collection '${collection}' does not contain indexed property '${collectionProperty.propertyName.path}'"
        }
        return entityStore.findEntitiesByPropertyPaged(property, rangeQuery).map(this::entityToDomain)
    }

    protected fun insertEntity(entity: New): Existing {
        val entityData = domainToData(entity)
        val schema = entityMetadataService.getEntityMetadataForCollection(collection).obj.schema
        /**
         * TODO: Application dev could've declared schema lazily to make the check always pass.
         * Therefore, also check that data serializes correctly to registered entity type with Jackson
         * Also, you can generate a random example of the schema when new entity is registered, and see
         * that is deserializes correctly to entity
         */
        check(schemaService.dataFitsSchema(schema, entityData)) { "Data does not fit schema" }
        val response = entityStore.insertEntity(entityData, collection)
        indexService.insertEntityIndex(response.id)
        return response.let(this::entityToDomain)
    }

    protected fun updateEntity(id: UUID, entityDto: New): Existing {
        val data = domainToData(entityDto)
        val entityMetadata = entityMetadataService.getEntityMetadataForCollection(collection)
        check(schemaService.dataFitsSchema(entityMetadata.obj.schema, data)) { "Data does not fit schema" }
        val previousEntity = entityStore.updateEntityAndReturnPrevious(id, data, collection)
        indexService.updateEntityIndex(previousEntity, id)
        return previousEntity.let(this::entityToDomain)
    }

    protected fun deleteEntity(id: UUID): Existing? {
        indexService.removeEntityIndex(id)
        return entityStore.deleteEntityById(id)?.let(this::entityToDomain)
    }
}
