package com.example.erp.entity.implementation

import com.example.erp.entity.*
import com.example.erp.entitymeta.EntityMetadataService
import com.example.erp.entitymeta.SchemaService
import org.springframework.stereotype.Service
import java.util.*

@Service
class IndexServiceImpl(
    private val entityStore: EntityStore,
    private val entityMetadataService: EntityMetadataService,
    private val schemaService: SchemaService,
    private val indexRegistry: IndexRegistry
) : IndexService {
    override fun insertEntityIndex(id: UUID) {
        // Index is not created yet
        val entity = entityStore.findEntityById(id, includeNotIndexed = true) ?: return
        val metadata = entityMetadataService.getEntityMetadataForCollection(entity.collection).obj
        val indexedProperties = schemaService.getIndexedProperties(metadata.schema, entity.data, entity.collection).plus(
            DateProperty(entity.createdAt, entity.collection, IndexedDatePropertyName("createdAt"))
        )
        indexedProperties.forEach {
            indexRegistry.insert(it, id)
        }
    }

    override fun updateEntityIndex(previousEntity: Entity, id: UUID) {
        val entity = entityStore.findEntityById(id) ?: return
        val metadata = entityMetadataService.getEntityMetadataForCollection(entity.collection).obj
        schemaService.getIndexedProperties(metadata.schema, previousEntity.data, entity.collection).forEach {
            indexRegistry.remove(it, id)
        }
        schemaService.getIndexedProperties(metadata.schema, entity.data, entity.collection).forEach {
            indexRegistry.insert(it, id)
        }
    }

    override fun removeEntityIndex(id: UUID) {
        val entity = entityStore.findEntityById(id) ?: return
        val metadata = entityMetadataService.getEntityMetadataForCollection(entity.collection).obj
        schemaService.getIndexedProperties(metadata.schema, entity.data, entity.collection).forEach {
            indexRegistry.remove(it, id)
        }
    }
}