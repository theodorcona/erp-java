package com.example.erp.entitymeta

import com.example.erp.common.AnyWithId
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery

interface EntityMetadataService{
    fun entityCollectionExists(collection: String): Boolean
    fun getAllEntityMetadata(rangeQuery: RangeQuery): PageDTO<AnyWithId<EntityMetadata>>
    fun getEntityMetadataForCollection(collection: String): AnyWithId<EntityMetadata>
    fun insertEntityMetadata(entityMetadata: EntityMetadata): AnyWithId<EntityMetadata>
}
