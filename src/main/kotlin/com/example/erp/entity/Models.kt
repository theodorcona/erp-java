package com.example.erp.entity

import org.joda.time.DateTime
import java.util.*

data class Entity(
    val id: UUID,
    val collection: String,
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

abstract class CollectionDescriptor<T>(
    val type: Class<T>,
    val collectionName: String,
    val indexedStringProperties: List<IndexedStringPropertyName> = emptyList(),
    val indexedDateProperties: List<IndexedDatePropertyName> = emptyList(),
    val indexedLongProperties: List<IndexedLongPropertyName> = emptyList(),
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
