package com.example.erp.rest

import com.example.erp.common.SchemaDTO
import com.example.erp.common.fromSchemaDTO
import com.example.erp.entity.EntityServiceFactory
import com.example.erp.entity.EntityStore
import com.example.erp.entitymeta.EntityMetadata
import com.example.erp.entitymeta.EntityMetadataService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class EntityController(
    private val entityStore: EntityStore,
    private val entityMetadataService: EntityMetadataService,
    private val entityServiceFactory: EntityServiceFactory,
    private val objectMapper: ObjectMapper
) {

    @PostMapping("/entities")
    fun createEntityMetadata(
        @RequestBody entityMetadataCreationDTO: EntityMetadataCreationDTO
    ) {
        entityMetadataService.insertEntityMetadata(
            EntityMetadata(entityMetadataCreationDTO.entityName, fromSchemaDTO(entityMetadataCreationDTO.schema))
        )
    }

    @GetMapping("/entities/{entityName}")
    fun getEntitiesForCollection(
        @PathVariable entityName: String,
        @RequestParam("cursor") cursor: String?,
        @RequestParam("pageSize") pageSize: Int
    ): PageDTO<EntityDTO> {
        if (!entityMetadataService.entityCollectionExists(entityName)) {
            throw NotFoundException()
        }
        return entityServiceFactory.getServiceForGenericEntity(entityName).getAll(RangeQuery(cursor, pageSize))
            .map { EntityDTO(it.id, it.obj) }
    }

    @PostMapping("/entities/{entityName}")
    fun createEntity(
        @PathVariable entityName: String,
        @RequestBody entity: EntityCreateDTO
    ): EntityDTO {
        if (!entityMetadataService.entityCollectionExists(entityName)) {
            throw NotFoundException()
        }
        return entityServiceFactory.getServiceForGenericEntity(entityName).insert(entity.data).let {
            EntityDTO(it.id, it.obj)
        }
    }

    @PutMapping("/entities/{entityName}/{entityId}")
    fun updateEntity(
        @PathVariable entityName: String,
        @PathVariable entityId: UUID,
        @RequestBody entity: EntityCreateDTO
    ): EntityDTO {
        if (!entityMetadataService.entityCollectionExists(entityName)) {
            throw NotFoundException()
        }
        entityServiceFactory.getServiceForGenericEntity(entityName).update(entityId, entity.data)
        return entityServiceFactory.getServiceForGenericEntity(entityName).getById(entityId).let {
            EntityDTO(it.id, it.obj)
        }
    }

    @GetMapping("/entities/{entityName}/{entityId}")
    fun getEntityById(
        @PathVariable entityName: String,
        @PathVariable entityId: UUID
    ): EntityDTO {
        return entityServiceFactory.getServiceForGenericEntity(entityName).getById(entityId).let {
            EntityDTO(it.id, it.obj)
        }
    }
}

data class EntityCreateDTO(
    val data: Map<String, Any>
)


data class EntityDTO(
    val id: UUID,
    val data: Map<String, Any>
)

data class EntityMetadataCreationDTO(
    val entityName: String,
    val schema: SchemaDTO
)
