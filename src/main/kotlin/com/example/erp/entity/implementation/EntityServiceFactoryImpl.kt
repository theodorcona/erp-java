package com.example.erp.entity.implementation

import com.example.erp.common.AnyWithId
import com.example.erp.entity.*
import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.entitymeta.SchemaService
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class EntityServiceFactoryImpl(
    private val objectMapper: ObjectMapper,
    private val entityStore: EntityStore,
    private val entityMetadataService: EntityMetadataService,
    private val schemaService: SchemaService,
    private val indexService: IndexService
) : EntityServiceFactory {
    override fun <T : Any> getServiceForEntity(collectionDescriptor: CollectionDescriptor<T>): EntityService<T> {
        return object : EntityService<T>, AbstractEntityService<AnyWithId<T>, T>(
            collectionDescriptor.collectionName,
            entityStore,
            entityMetadataService,
            schemaService,
            indexService,
        ) {
            override fun entityToDTO(entity: Entity): AnyWithId<T> {
                val entityMetadata = objectMapper.readValue(entity.data, collectionDescriptor.type)
                return AnyWithId(entity.id, entityMetadata)
            }

            override fun dtoToData(dto: T): String {
                return objectMapper.writeValueAsString(dto)
            }

            override fun getById(id: UUID): AnyWithId<T> {
                return findById(id)!!
            }

            override fun getAll(rangeQuery: RangeQuery): PageDTO<AnyWithId<T>> {
                return getEntitiesAllPaged(rangeQuery)
            }

            override fun getByProperty(property: IndexedCollectionProperty<Any>, rangeQuery: RangeQuery): PageDTO<AnyWithId<T>> {
                return findByPropertyPaged(property, rangeQuery)
            }

            override fun insert(entity: T): AnyWithId<T> {
                return insertEntity(entity)
            }

            override fun update(id: UUID, entity: T): AnyWithId<T> {
                return updateEntity(id, entity)
            }

            override fun delete(id: UUID): AnyWithId<T> {
                return deleteEntity(id)!!
            }
        }
    }
}