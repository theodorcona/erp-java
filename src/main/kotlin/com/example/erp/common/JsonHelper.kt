package com.example.erp.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import org.joda.time.DateTime

fun getValueAtPath(json: JsonNode, keyPath: List<String>, position: Int = 0): JsonNode {
    return if (position >= keyPath.size) {
        json
    } else {
        getValueAtPath(json.get(keyPath[position]), keyPath, position + 1)
    }
}

fun JsonNode.toValue(propertyType: SchemaProperties.PropertyType) = when(propertyType) {
    SchemaProperties.PropertyType.STRING -> this.textValue()
    SchemaProperties.PropertyType.BOOLEAN-> this.booleanValue()
    SchemaProperties.PropertyType.LONG -> this.numberValue().toLong()
    SchemaProperties.PropertyType.DATE -> DateTime.parse(this.toString())
    SchemaProperties.PropertyType.OBJECT -> throw IllegalStateException("Cannot convert to value")
    // TODO
    SchemaProperties.PropertyType.ARRAY -> throw IllegalStateException("Cannot convert to value")
    SchemaProperties.PropertyType.DYNAMIC_OBJECT -> throw IllegalStateException("Cannot convert to value")
}