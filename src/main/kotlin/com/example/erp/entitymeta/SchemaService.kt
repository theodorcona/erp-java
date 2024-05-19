package com.example.erp.entitymeta

import com.example.erp.common.SchemaProperties
import com.example.erp.entity.IndexedProperty

interface SchemaService {
    fun schemaContainsIndexedProperty(schema: SchemaProperties.Schema, indexedProperty: IndexedProperty<Any>): Boolean
    fun dataFitsSchema(schema: SchemaProperties.Schema, data: String): Boolean
    fun getIndexedProperties(schema: SchemaProperties.Schema, data: String, collection: String): List<IndexedProperty<Any>>
    fun pathsFitSchema(schema: SchemaProperties.Schema, paths: Map<String, SchemaProperties.PropertyType>): Boolean
    fun schemaContainsPaths(schema: SchemaProperties.Schema, paths: Map<String, SchemaProperties.PropertyType>): Boolean
}