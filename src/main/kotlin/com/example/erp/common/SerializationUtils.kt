package com.example.erp.common

import com.example.erp.logic.*
import com.fasterxml.jackson.databind.JsonNode


private fun deserializeApply(jsonNode: JsonNode): Apply {
    val inputType = ApplyType.valueOf(jsonNode.get("type").asText())
    return when (inputType) {
        ApplyType.MONO -> MonoApply(
            deserializeMonoOperator(jsonNode.get("operator")),
            deserializeInput(jsonNode.get("input1"))
        )

        ApplyType.DUAL -> DualApply(
            deserializeDualOperator(jsonNode.get("operator")),
            deserializeInput(jsonNode.get("input1")),
            deserializeInput(jsonNode.get("input2"))
        )

        ApplyType.TRI -> TriApply(
            deserializeTriOperator(jsonNode.get("operator")),
            deserializeInput(jsonNode.get("input1")),
            deserializeInput(jsonNode.get("input2")),
            deserializeInput(jsonNode.get("input3")),
        )
    }

}

private fun deserializeMonoOperator(jsonNode: JsonNode): Operators.MonoOperator {
    val operatorType = Operators.OperatorType.valueOf(jsonNode.get("type").asText())
    return when (operatorType) {
        Operators.OperatorType.UPPERCASE -> Operators.UpperCaseOperator()
        else -> throw IllegalStateException("")
    }
}

private fun deserializeDualOperator(jsonNode: JsonNode): Operators.DualOperator {
    val operatorType = Operators.OperatorType.valueOf(jsonNode.get("type").asText())
    return when (operatorType) {
        Operators.OperatorType.GREATER_THAN -> Operators.GreaterThanOperator()
        else -> throw IllegalStateException("")
    }
}

private fun deserializeTriOperator(jsonNode: JsonNode): Operators.TriOperator {
    val operatorType = Operators.OperatorType.valueOf(jsonNode.get("type").asText())
    return when (operatorType) {
        Operators.OperatorType.IF -> Operators.IfOperator(
            SchemaProperties.PropertyType.valueOf(jsonNode.get("outputType").asText())
        )

        else -> throw IllegalStateException("")
    }
}

fun deserializeInput(jsonNode: JsonNode): Input {
    val inputType = InputType.valueOf(jsonNode.get("type").asText())
    return when (inputType) {
        InputType.APPLY -> ApplyInput(deserializeApply(jsonNode.get("apply")))
        InputType.STATIC -> {
            val type = SchemaProperties.PropertyType.valueOf(jsonNode.get("propertyType").textValue())
            StaticInput(jsonNode.get("value").toValue(type), type)
        }

        InputType.OTHER -> {
            OtherDataInput(
                jsonNode.get("path").textValue(),
                SchemaProperties.PropertyType.valueOf(jsonNode.get("propertyType").textValue())
            )
        }
    }
}


fun deserializeSchema(jsonNode: JsonNode): SchemaProperties.Schema {
    return jsonNode.get("properties").properties().map {
        it.key to convertToSchemaIndexedProperty(it.value)
    }.let { properties ->
        SchemaProperties.Schema(properties.toMap())
    }
}

private fun constructObjectSchema(jsonNode: JsonNode): SchemaProperties.Object {
    return jsonNode.get("properties").properties().map {
        it.key to convertToSchemaIndexedProperty(it.value)
    }.let { properties ->
        SchemaProperties.Object(properties.toMap())
    }
}

private fun convertToSchemaIndexedProperty(value: JsonNode): SchemaProperties.PropertyTypeIndexable {
    val type = SchemaProperties.PropertyType.valueOf(value.get("property").get("type").asText()!!)
    return when (type) {
        SchemaProperties.PropertyType.DATE -> SchemaProperties.Date().toIndexable(
            value.get("indexed").asBoolean()
        )

        SchemaProperties.PropertyType.LONG -> SchemaProperties.Long().toIndexable(
            value.get("indexed").asBoolean()
        )

        SchemaProperties.PropertyType.STRING -> SchemaProperties.String().toIndexable(
            value.get("indexed").asBoolean()
        )

        SchemaProperties.PropertyType.ARRAY -> SchemaProperties.Array(
            convertToSchemaIndexedProperty(value.get("property"))
        ).toIndexable()

        SchemaProperties.PropertyType.BOOLEAN -> SchemaProperties.Boolean().toIndexable(
            value.get("indexed").asBoolean()
        )

        SchemaProperties.PropertyType.DYNAMIC_OBJECT -> SchemaProperties.DynamicObject().toIndexable()
        SchemaProperties.PropertyType.OBJECT -> constructObjectSchema(value).toIndexable()
    }
}
