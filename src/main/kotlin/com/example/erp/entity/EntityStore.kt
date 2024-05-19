package com.example.erp.entity

import com.example.erp.rest.RangeQuery
import com.example.erp.rest.PageDTO
import java.util.*

interface EntityStore {
    fun findEntityById(id: UUID, includeNotIndexed: Boolean = false): Entity?
    fun findAllEntitiesInCollectionPaged(collection: String, rangeQuery: RangeQuery): PageDTO<Entity>
    fun findEntitiesByPropertyPaged(entityProperty: IndexedProperty<Any>, cursor: RangeQuery): PageDTO<Entity>
    fun insertEntity(data: String, collection: String): Entity
    fun updateEntityAndReturnPrevious(id: UUID, data: String, collection: String): Entity
    fun deleteEntityById(id: UUID): Entity?
}
