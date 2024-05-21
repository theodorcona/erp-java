package com.example.erp.entitymeta

import com.example.erp.common.SchemaDTO
import com.example.erp.common.SchemaProperties


data class EntityMetadata(
    val collection: String,
    val schema: SchemaProperties.Schema
)

data class EntityMetadataDTO(
    val collection: String,
    val schema: SchemaDTO
)


