package com.example.erp.entity.implementation

import com.example.erp.common.firstDate
import com.example.erp.common.firstUuid
import com.example.erp.common.toPagedResponse
import com.example.erp.entity.DateProperty
import com.example.erp.entity.IndexedProperty
import com.example.erp.entity.LongProperty
import com.example.erp.entity.StringProperty
import com.example.erp.rest.RangeQuery
import com.example.erp.rest.PageDTO
import org.joda.time.DateTime
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class IndexRegistry(
    private val stringIndex: StringIndexRepository,
    private val dateIndex: DateIndexRepository,
    private val longIndex: LongIndexRepository
) {
    fun <T> search(property: IndexedProperty<T>, rangeQuery: RangeQuery): PageDTO<UUID> {
        val uuidCursor = kotlin.runCatching {
            UUID.fromString(rangeQuery.cursor ?: firstUuid)
        }.getOrNull()?.toString()
        checkNotNull(uuidCursor) {
            throw IllegalStateException("Cursor must be of type UUID for index search")
        }
        return when (property) {
            is StringProperty -> stringIndex.findAllByEntityCollectionAndPropertyNameAndValueAndEntityIdGreaterThanEqual(
                property.entityCollection,
                property.propertyName.path,
                property.value,
                uuidCursor,
                PageRequest.of(0, rangeQuery.pageSize + 1)
            )

            is LongProperty -> longIndex.findAllByEntityCollectionAndPropertyNameAndValueAndEntityIdGreaterThan(
                property.entityCollection,
                property.propertyName.path,
                property.value,
                uuidCursor,
                PageRequest.of(0, rangeQuery.pageSize + 1)
            )

            is DateProperty -> dateIndex.findAllByEntityCollectionAndPropertyNameAndValueAndEntityIdGreaterThan(
                property.entityCollection,
                property.propertyName.path,
                property.value,
                uuidCursor,
                PageRequest.of(0, rangeQuery.pageSize + 1)
            )
        }.toPagedResponse(rangeQuery) { entity -> entity.entityId }.map { UUID.fromString(it.entityId) }
    }

    fun findAllPaged(collection: String, rangeQuery: RangeQuery): PageDTO<UUID> {
        val dateCursor = kotlin.runCatching { DateTime.parse(rangeQuery.cursor ?: firstDate) }.getOrNull()
        checkNotNull(dateCursor) { "Cursor must be of type date for this query" }
        /**
         * This is a bit hacky for now. We're getting all index entries that have collection=<collection> and
         * propertyName=createdAt. If we didn't have the propertyName condition, we would get each entry multiple times.
         * The assumption here is that each entity has exactly one index entry for propertyName=createdAt
         */
        return dateIndex.findAllByEntityCollectionAndPropertyNameAndValueGreaterThanEqual(
            collection,
            "createdAt",
            dateCursor,
            PageRequest.of(0, rangeQuery.pageSize + 1)
        ).toPagedResponse(rangeQuery) { indexEntry -> indexEntry.value.toString() }.map { UUID.fromString(it.entityId) }
    }

    fun <T> insert(property: IndexedProperty<T>, entityId: UUID) {
        when (property) {
            is StringProperty -> stringIndex.insert(
                StringIndexEntry(
                    property.value,
                    property.entityCollection,
                    property.propertyName.path,
                    entityId.toString()
                )
            )

            is LongProperty -> longIndex.insert(
                LongIndexEntry(
                    property.value,
                    property.entityCollection,
                    property.propertyName.path,
                    entityId.toString()
                )
            )

            is DateProperty -> dateIndex.insert(
                DateIndexEntry(
                    property.value,
                    property.entityCollection,
                    property.propertyName.path,
                    entityId.toString()
                )
            )
        }
    }

    fun <Any> remove(property: IndexedProperty<Any>, entityId: UUID) {
        when (property) {
            is StringProperty -> stringIndex.deleteByEntityCollectionAndPropertyNameAndValueAndEntityId(
                property.entityCollection, property.propertyName.path, property.value, entityId.toString()
            )

            is LongProperty -> longIndex.deleteByEntityCollectionAndPropertyNameAndValueAndEntityId(
                property.entityCollection, property.propertyName.path, property.value, entityId.toString()
            )

            is DateProperty -> dateIndex.deleteByEntityCollectionAndPropertyNameAndValueAndEntityId(
                property.entityCollection, property.propertyName.path, property.value, entityId.toString()
            )
        }
    }
}