package com.example.erp.entity.implementation

import com.example.erp.entity.Entity
import com.example.erp.entity.IndexedProperty
import com.example.erp.entity.EntityStore
import com.example.erp.entity.StringProperty
import com.example.erp.rest.RangeQuery
import com.example.erp.rest.PageDTO
import org.joda.time.DateTime
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class EntityStoreImpl(
    private val entityRepository: EntityRepository,
    private val indexRegistry: IndexRegistry,
    private val dateIndexRepository: DateIndexRepository,
) : EntityStore {
    override fun findEntityById(id: UUID, includeNotIndexed: Boolean): Entity? {
        val entity = entityRepository.findById(id.toString()).getOrNull()?.toEntity()?.takeIf {
            // The index is the source of truth when deciding whether an entity exists or not, so we must check it
            includeNotIndexed || dateIndexRepository.findAllByEntityCollectionAndPropertyNameAndValueAndEntityId(
                entityCollection = it.collection,
                propertyName = "createdAt",
                value = it.createdAt,
                entityId = it.id.toString()
            ).isNotEmpty()
        }
        return entity
    }

    override fun findAllEntitiesInCollectionPaged(collection: String, rangeQuery: RangeQuery): PageDTO<Entity> {
        return indexRegistry.findAllPaged(collection, rangeQuery).getEntities()
    }

    private fun GenericEntity.toEntity() = Entity(UUID.fromString(id), collection, createdAt, updatedAt, data)
    private fun Entity.toGenericEntity() = GenericEntity(id.toString(), createdAt, updatedAt, collection, data)

    override fun findEntitiesByPropertyPaged(
        entityProperty: IndexedProperty<Any>,
        cursor: RangeQuery
    ): PageDTO<Entity> {
        return indexRegistry.search(entityProperty, cursor).getEntities()
    }

    override fun insertEntity(data: String, collection: String): Entity {
        val entity = Entity(
            id = UUID.randomUUID(),
            collection = collection,
            createdAt = DateTime.now(),
            updatedAt = DateTime.now(),
            data = data
        )
        val toGenericEntity = entity.toGenericEntity()
        val savedEntity = entityRepository.save(toGenericEntity)
        return savedEntity.toEntity()
    }

    override fun updateEntityAndReturnPrevious(id: UUID, data: String, collection: String): Entity {
        val existingEntity = findEntityById(id)
        checkNotNull(existingEntity) { "Not entity wit id=$id" }
        check(existingEntity.collection == collection) { "Can not update entity in different collection" }
        existingEntity?.let {
            val updatedEntity = it.copy(
                updatedAt = DateTime.now(),
                data = data
            )
            entityRepository.save(updatedEntity.toGenericEntity())
            return existingEntity
        }
        throw IllegalArgumentException("Entity not found")
    }

    override fun deleteEntityById(id: UUID): Entity? {
        val entity: Entity? = findEntityById(id)
        return entity?.let {
            entityRepository.deleteById(it.id.toString())
            it
        }
    }

    /**
     * This is still problematic, what happens when entity is deleted between index and entity retrieval?
     * Should probably still serve the "deleted" entities, so implement a soft deletion
     */
    private fun PageDTO<UUID>.getEntities(): PageDTO<Entity> {
        val entities = entityRepository
            .findAllById(items.map { id -> id.toString() })
            .map { it.toEntity() }
        return PageDTO(
            items = entities,
            nextCursor = nextCursor
        )
    }
}
