package com.example.erp.rest

import com.example.erp.common.SchemaProperties
import com.example.erp.entity.Entity
import com.example.erp.entity.EntityStore
import com.example.erp.entitymeta.EntityMetadata
import com.example.erp.entitymeta.EntityMetadataService
import org.joda.time.DateTime
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class EntityController(
    private val entityStore: EntityStore,
    private val entityMetadataService: EntityMetadataService
) {

    @PostMapping("/entities")
    fun createEntityMetadata(
        @RequestBody entityMetadataCreationDTO: EntityMetadataCreationDTO
    ) {
        entityMetadataService.insertEntityMetadata(
            EntityMetadata(entityMetadataCreationDTO.entityName, entityMetadataCreationDTO.schema)
        )
    }

    @GetMapping("/entities/{entityName}")
    fun getEntitiesForCollection(
        @PathVariable entityName: String,
        @RequestParam("cursor") cursor: String?,
        @RequestParam("pageSize") pageSize: Int
    ): PageDTO<Entity> {
        if (!entityMetadataService.entityCollectionExists(entityName)) {
            throw NotFoundException()
        }
        return entityStore.findAllEntitiesInCollectionPaged(entityName, RangeQuery(cursor, pageSize))
    }

    @PostMapping("/entities/{entityName}")
    fun createEntity(
        @PathVariable entityName: String,
        @RequestBody entity: EntityCreateDTO
    ): EntityDTO {
        if (!entityMetadataService.entityCollectionExists(entityName)) {
            throw NotFoundException()
        }
        return entityStore.insertEntity(entity.data, entityName).let {
            EntityDTO(
                id = it.id,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                data = it.data
            )
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
        entityStore.updateEntityAndReturnPrevious(entityId, entity.data, entityName)
        return entityStore.findEntityById(entityId)!!.let {
            EntityDTO(
                id = it.id,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
                data = it.data
            )
        }
    }

    @GetMapping("/entities/{entityName}/{entityId}")
    fun getEntitiesForCollection(
        @PathVariable entityName: String,
        @PathVariable entityId: UUID
    ): EntityDTO {
        return entityStore.findEntityById(entityId)
            ?.takeIf { it.collection == entityName }
            ?.let {
                EntityDTO(
                    id = it.id,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                    data = it.data
                )
            } ?: throw NotFoundException()
    }
}

data class EntityCreateDTO(
    val data: String
)

data class EntityDTO(
    val id: UUID,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val data: String
)

data class EntityMetadataCreationDTO(
    val entityName: String,
    val schema: SchemaProperties.Schema
)
