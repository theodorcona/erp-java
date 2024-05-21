package com.example.erp.entity.implementation

import com.example.erp.common.AnyWithId
import com.example.erp.entity.*
import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.entitymeta.SchemaService
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
    override fun <T : Any, DTO : Any> getServiceForEntity(collectionDescriptor: CollectionDescriptor<T, DTO>): EntityService<T> {
        return object : EntityService<T>, AbstractEntityService<AnyWithId<T>, T>(
            collectionDescriptor.collectionName,
            entityStore,
            entityMetadataService,
            schemaService,
            indexService,
        ) {
            override fun entityToDomain(entity: Entity): AnyWithId<T> {
                val entityMetadata = objectMapper.readValue(entity.data, collectionDescriptor.dtoType)
                return AnyWithId(entity.id, collectionDescriptor.fromDTO(entityMetadata))
            }

            override fun domainToData(domain: T): String {
                collectionDescriptor.toDTO(domain)
                return objectMapper.writeValueAsString(collectionDescriptor.toDTO(domain))
            }

            override fun getById(id: UUID): AnyWithId<T> {
                return findById(id)!!
            }

            override fun getAll(rangeQuery: RangeQuery): PageDTO<AnyWithId<T>> {
                return getEntitiesAllPaged(rangeQuery)
            }

            override fun getByProperty(
                property: IndexedCollectionProperty<Any>,
                rangeQuery: RangeQuery
            ): PageDTO<AnyWithId<T>> {
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

    override fun getServiceForGenericEntity(collectionName: String): EntityService<Map<String, Any>> {
        return object : EntityService<Map<String, Any>>,
            AbstractEntityService<AnyWithId<Map<String, Any>>, Map<String, Any>>(
                collectionName,
                entityStore,
                entityMetadataService,
                schemaService,
                indexService,
            ) {
            override fun entityToDomain(entity: Entity): AnyWithId<Map<String, Any>> {
                return AnyWithId(entity.id, objectMapper.readValue(
                    entity.data,
                    object : TypeReference<Map<String, Any>>() {}
                ))
            }

            override fun domainToData(domain: Map<String, Any>): String {
                return objectMapper.writeValueAsString(domain)
            }

            override fun getById(id: UUID): AnyWithId<Map<String, Any>> {
                return findById(id)!!
            }

            override fun getAll(rangeQuery: RangeQuery): PageDTO<AnyWithId<Map<String, Any>>> {
                return getEntitiesAllPaged(rangeQuery)
            }

            override fun getByProperty(
                property: IndexedCollectionProperty<Any>,
                rangeQuery: RangeQuery
            ): PageDTO<AnyWithId<Map<String, Any>>> {
                return findByPropertyPaged(property, rangeQuery)
            }

            override fun insert(entity: Map<String, Any>): AnyWithId<Map<String, Any>> {
                return insertEntity(entity)
            }

            override fun update(id: UUID, entity: Map<String, Any>): AnyWithId<Map<String, Any>> {
                return updateEntity(id, entity)
            }

            override fun delete(id: UUID): AnyWithId<Map<String, Any>> {
                return deleteEntity(id)!!
            }
        }
    }
}