package com.example.erp.entity

import com.example.erp.common.AnyWithId
import com.example.erp.rest.PageDTO
import com.example.erp.rest.RangeQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface EntityService<T : Any> {
    fun getById(id: UUID): AnyWithId<T>
    fun getAll(rangeQuery: RangeQuery): PageDTO<AnyWithId<T>>
    fun getByProperty(
        property: IndexedCollectionProperty<Any>,
        rangeQuery: RangeQuery
    ): PageDTO<AnyWithId<T>>
    fun insert(entity: T): AnyWithId<T>
    fun update(id: UUID, entity: T): AnyWithId<T>
    fun delete(id: UUID): AnyWithId<T>
}