package com.example.erp.entitymeta.implementation

import com.example.erp.common.SchemaProperties
import com.example.erp.common.getValueAtPath
import com.example.erp.entity.*
import com.example.erp.entitymeta.SchemaService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SchemaServiceImpl(private val objectMapper: ObjectMapper) : SchemaService {
    private val logger = LoggerFactory.getLogger(SchemaServiceImpl::class.java)

    override fun schemaContainsIndexedProperty(
        schema: SchemaProperties.Schema,
        indexedProperty: IndexedProperty<Any>
    ): Boolean {
        return objectContainsIndexedProperty(schema, indexedProperty.propertyName.path)
    }

    override fun dataFitsSchema(schema: SchemaProperties.Schema, data: String): Boolean {
        val jsonNode = objectMapper.readTree(data)
        return jsonNodeFitsSchema(jsonNode, schema)
    }

    override fun getIndexedProperties(
        schema: SchemaProperties.Schema,
        data: String,
        collection: String
    ): List<IndexedProperty<Any>> {
        val pathToType = schema.properties.flatMap {
            getAllIndexedProperties(it)
        }
        return pathToType.map { (path, type) ->
            val jsonNode = getValueAtPath(objectMapper.readTree(data), path.split("_"))
            when (type) {
                SchemaProperties.PropertyType.STRING -> StringProperty(
                    jsonNode.textValue(),
                    collection,
                    IndexedStringPropertyName(path)
                )

                SchemaProperties.PropertyType.LONG -> LongProperty(
                    jsonNode.longValue(),
                    collection,
                    IndexedLongPropertyName(path)
                )

                SchemaProperties.PropertyType.DATE -> DateProperty(
                    jsonNode.let { DateTime.parse(it.textValue()) },
                    collection,
                    IndexedDatePropertyName(path)
                )

                else -> throw IllegalStateException("")
            }
        }
    }

    override fun schemaContainsPaths(schema: SchemaProperties.Schema, paths: Map<String, SchemaProperties.PropertyType>): Boolean {
        return paths.all {
            val keyPath = it.key.split("_")
            schemaContainsPath(schema, keyPath, it.value)
        }
    }

    private fun schemaContainsPath(
        schema: SchemaProperties.Schema,
        keyPath: List<String>,
        propertyType: SchemaProperties.PropertyType
    ): Boolean {
        if (keyPath.isEmpty()) return false
        val indexedObject = schema.properties[keyPath[0]] ?: return false
        return objectContainsProperty(indexedObject, keyPath, 1) {
            it.property.type == propertyType && it.required
        }
    }

    override fun pathsFitSchema(schema: SchemaProperties.Schema, paths: Map<String, SchemaProperties.PropertyType>): Boolean {
        return schema.properties.all {
            !it.value.required || isContained(it.key, it.value, paths)
        }
    }

    private fun isContained(
        key: String,
        property: SchemaProperties.PropertyTypeIndexable,
        paths: Map<String, SchemaProperties.PropertyType>,
        prefix: String = ""
    ): Boolean {
        val fullPath = "${prefix}_$key"
        if (property.property.type == SchemaProperties.PropertyType.OBJECT) {
            return (property.property as SchemaProperties.Object).properties.all {
                isContained(it.key, it.value, paths, fullPath)
            }
        }
        return paths.contains(fullPath) && paths[fullPath] == property.property.type
    }

    private fun getAllIndexedProperties(
        entry: Map.Entry<String, SchemaProperties.PropertyTypeIndexable>,
        prefix: String = ""
    ): List<Pair<String, SchemaProperties.PropertyType>> {
        return if (entry.value.indexed) {
            listOf("$prefix${entry.key}" to entry.value.property.type)
        } else if (entry.value.property is SchemaProperties.Object) {
            (entry.value.property as SchemaProperties.Object).properties.flatMap {
                getAllIndexedProperties(it, "$prefix${entry.key}_")
            }
        } else {
            listOf()
        }

    }

    private fun objectContainsIndexedProperty(schema: SchemaProperties.Schema, path: String): Boolean {
        val keyPath = path.split("_")
        if (keyPath.isEmpty()) return false
        val indexedObject = schema.properties[keyPath[0]] ?: return false
        return objectContainsProperty(indexedObject, keyPath, 1) { it.indexed }
    }

    private fun objectContainsProperty(
        schema: SchemaProperties.PropertyTypeIndexable,
        keyPath: List<String>,
        position: Int,
        condition: (SchemaProperties.PropertyTypeIndexable) -> Boolean
    ): Boolean {
        if (position >= keyPath.size) {
            return condition(schema)
        }
        if(schema.property !is SchemaProperties.Object) {
            logger.warn("Property at path '${keyPath.subList(0, position + 1).joinToString("_")}' not of type OBJECT")
            return false
        }
        val property = schema.property.properties[keyPath[position]]
        if (property == null) {
            logger.warn("Property at path '${keyPath.subList(0, position + 1).joinToString("_")}' is null")
            return false
        }
        return objectContainsProperty(property, keyPath, position + 1, condition)
    }

    private fun jsonNodeFitsSchema(jsonNode: JsonNode, schema: SchemaProperties.Schema): Boolean {
        return jsonNode.isObject && schema.properties.all {
            if (jsonNode.has(it.key)) jsonNodeFitsSchema(jsonNode.get(it.key), it.value) else !it.value.required
        }
    }

    private fun jsonNodeFitsSchema(jsonNode: JsonNode, schema: SchemaProperties.PropertyTypeIndexable): Boolean {
        return when (schema.property.type) {
            SchemaProperties.PropertyType.STRING -> jsonNode.isTextual
            SchemaProperties.PropertyType.LONG -> jsonNode.isNumber
            SchemaProperties.PropertyType.DATE -> jsonNode.isTextual
            SchemaProperties.PropertyType.ARRAY -> jsonNode.isArray
            SchemaProperties.PropertyType.DYNAMIC_OBJECT -> jsonNode.isObject
            SchemaProperties.PropertyType.BOOLEAN -> jsonNode.isBoolean
            SchemaProperties.PropertyType.OBJECT ->
                jsonNode.isObject && (schema.property as SchemaProperties.Object).properties.all {
                    jsonNodeFitsSchema(jsonNode.get(it.key), it.value)
                }
        }
    }

}
