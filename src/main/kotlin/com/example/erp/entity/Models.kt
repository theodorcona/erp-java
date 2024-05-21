package com.example.erp.entity

import org.joda.time.DateTime
import java.util.*

// https://erp.com/users/abcd8976-abcd738373738338-383473-sadf23
data class Entity(
    val id: UUID,
    // abcd8976-abcd738373738338-383473-sadf23
    val collection: String, // users,invoices,trees
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val data: String
)

sealed interface IndexedProperty<out T> {
    val entityCollection: String
    val propertyName: IndexedPropertyName<T>
    val value: T
}

class StringProperty(
    override val value: String,
    override val entityCollection: String,
    override val propertyName: IndexedPropertyName<String>
) : IndexedProperty<String>

class LongProperty(
    override val value: Long,
    override val entityCollection: String,
    override val propertyName: IndexedPropertyName<Long>
) : IndexedProperty<Long>

class DateProperty(
    override val value: DateTime,
    override val entityCollection: String,
    override val propertyName: IndexedPropertyName<DateTime>
) : IndexedProperty<DateTime>

/**
 * Use this when your entity type doesn't need a different serialization format
 */
abstract class NoDTOCollectionDescriptor<T>(
    type: Class<T>,
    collectionName: String,
    indexedStringProperties: List<IndexedStringPropertyName> = emptyList(),
    indexedDateProperties: List<IndexedDatePropertyName> = emptyList(),
    indexedLongProperties: List<IndexedLongPropertyName> = emptyList(),
) : CollectionDescriptor<T,T>(
    type,
    type,
    collectionName,
    indexedStringProperties,
    indexedDateProperties,
    indexedLongProperties,
    { d -> d },
    { d -> d }
)

abstract class CollectionDescriptor<Domain, DTO>(
    val type: Class<Domain>,
    val dtoType: Class<DTO>,
    val collectionName: String,
    val indexedStringProperties: List<IndexedStringPropertyName> = emptyList(),
    val indexedDateProperties: List<IndexedDatePropertyName> = emptyList(),
    val indexedLongProperties: List<IndexedLongPropertyName> = emptyList(),
    val fromDTO : (dto: DTO) -> Domain,
    val toDTO: (domain: Domain) -> DTO,
)
interface IndexedPropertyName<out T> {
    val path: String
}
data class IndexedStringPropertyName(override val path: String) : IndexedPropertyName<String>
data class IndexedDatePropertyName(override val path: String) : IndexedPropertyName<DateTime>
data class IndexedLongPropertyName(override val path: String) : IndexedPropertyName<Long>

sealed interface IndexedCollectionProperty<out T> {
    val propertyName: IndexedPropertyName<T>
    val value: T
    fun toProperty(collection: String): IndexedProperty<T>
}

class StringCollectionProperty(
    override val value: String,
    override val propertyName: IndexedStringPropertyName,
) : IndexedCollectionProperty<String> {
    override fun toProperty(collection: String) = StringProperty(value, collection, propertyName)
}

class LongCollectionProperty(
    override val value: Long,
    override val propertyName: IndexedLongPropertyName,
) : IndexedCollectionProperty<Long> {
    override fun toProperty(collection: String) = LongProperty(value, collection, propertyName)
}

class DateCollectionProperty(
    override val value: DateTime,
    override val propertyName: IndexedDatePropertyName,
) : IndexedCollectionProperty<DateTime> {
    override fun toProperty(collection: String) = DateProperty(value, collection, propertyName)
}
