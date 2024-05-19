package com.example.erp.entity.implementation

import org.joda.time.DateTime
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.repository.MongoRepository

interface EntityRepository : MongoRepository<GenericEntity, String>
interface StringIndexRepository : MongoRepository<StringIndexEntry, String> {
    fun findAllByEntityCollectionAndPropertyNameAndValueAndEntityIdGreaterThanEqual(
        entityCollection: String,
        propertyName: String,
        value: String,
        entityId: String,
        pageable: Pageable
    ): Page<StringIndexEntry>
    fun deleteByEntityCollectionAndPropertyNameAndValueAndEntityId(
        entityCollection: String,
        propertyName: String,
        value: String,
        entityId: String
    )
}
interface DateIndexRepository : MongoRepository<DateIndexEntry, String> {
    fun findAllByEntityCollectionAndPropertyNameAndValueAndEntityIdGreaterThan(
        entityCollection: String,
        propertyName: String,
        value: DateTime,
        entityId: String,
        pageable: Pageable
    ): Page<DateIndexEntry>
    fun findAllByEntityCollectionAndPropertyNameAndValueAndEntityId(
        entityCollection: String,
        propertyName: String,
        value: DateTime,
        entityId: String
    ): List<DateIndexEntry>
    fun findAllByEntityCollectionAndPropertyNameAndValueGreaterThanEqual(
        collection: String,
        propertyName: String,
        value: DateTime,
        pageable: Pageable
    ): Page<DateIndexEntry>
    fun deleteByEntityCollectionAndPropertyNameAndValueAndEntityId(
        entityCollection: String,
        propertyName: String,
        value: DateTime,
        entityId: String
    )
}
interface LongIndexRepository : MongoRepository<LongIndexEntry, String> {
    fun findAllByEntityCollectionAndPropertyNameAndValueAndEntityIdGreaterThan(
        entityCollection: String,
        propertyName: String,
        value: Long,
        entityId: String,
        pageable: Pageable
    ): Page<LongIndexEntry>
    fun deleteByEntityCollectionAndPropertyNameAndValueAndEntityId(
        entityCollection: String,
        propertyName: String,
        value: Long,
        entityId: String
    )
}

interface IndexEntry {
    val entityId: String
}

data class StringIndexEntry(
    val value: String,
    val entityCollection: String,
    val propertyName: String,
    override val entityId: String,
) : IndexEntry

data class DateIndexEntry(
    val value: DateTime,
    val entityCollection: String,
    val propertyName: String,
    override val entityId: String
) : IndexEntry

data class LongIndexEntry(
    val value: Long,
    val entityCollection: String,
    val propertyName: String,
    override val entityId: String
) : IndexEntry


data class GenericEntity(
    @Id val id: String,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val collection: String,
    val data: String
)

data class EntityDeletion(
    @Id val entityId: String
)
