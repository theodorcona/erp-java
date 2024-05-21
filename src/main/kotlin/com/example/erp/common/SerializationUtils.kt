package com.example.erp.common

import com.example.erp.entitymeta.EntityMetadata
import com.example.erp.entitymeta.EntityMetadataDTO
import com.example.erp.logic.*

fun SchemaProperties.Schema.toSchemaDTO(): SchemaDTO {
    return SchemaDTO(
        properties = properties.entries.map {
            toPropertyDTO(it.key, it.value)
        }
    )
}

fun toPropertyDTO(key: String?, value: SchemaProperties.PropertyTypeIndexable): PropertyTypeIndexableDTO {
    return PropertyTypeIndexableDTO(
        key = key,
        type = value.property.type,
        indexed = value.indexed,
        required = value.indexed,
        objectProperties = if (value.property is SchemaProperties.Object) {
            value.property.properties.entries.map { toPropertyDTO(it.key, it.value) }
        } else null,
        arrayProperty = if (value.property is SchemaProperties.Array) {
            toPropertyDTO(null, value.property.property)
        } else null
    )
}

fun fromSchemaDTO(schemaDTO: SchemaDTO): SchemaProperties.Schema {
    return schemaDTO.properties.map {
        it.key!! to fromPropertyDTO(it)
    }.let { properties ->
        SchemaProperties.Schema(properties.toMap())
    }
}

private fun fromPropertyDTO(
    property: PropertyTypeIndexableDTO,
    requireKey: Boolean = true
): SchemaProperties.PropertyTypeIndexable {
    val type = property.type
    if (requireKey) checkNotNull(property.key)
    return when (type) {
        SchemaProperties.PropertyType.DATE -> {
            SchemaProperties.Date().toIndexable(
                property.indexed
            )
        }

        SchemaProperties.PropertyType.LONG -> {
            SchemaProperties.Long().toIndexable(
                property.indexed
            )
        }

        SchemaProperties.PropertyType.STRING -> {
            SchemaProperties.String().toIndexable(
                property.indexed
            )
        }

        SchemaProperties.PropertyType.ARRAY -> {
            val arrayProperty = property.arrayProperty
            checkNotNull(arrayProperty) { "Expected array type to have 'arrayProperty'" }
            SchemaProperties.Array(fromPropertyDTO(arrayProperty, false)).toIndexable(property.indexed)
        }

        SchemaProperties.PropertyType.BOOLEAN -> SchemaProperties.Boolean().toIndexable(
            property.indexed
        )

        SchemaProperties.PropertyType.DYNAMIC_OBJECT -> SchemaProperties.DynamicObject().toIndexable()
        SchemaProperties.PropertyType.OBJECT -> {
            val objectProperties = property.objectProperties
            checkNotNull(objectProperties) { "Expected object type to have 'objectProperties'" }
            SchemaProperties.Object(
                objectProperties.map { it.key!! to fromPropertyDTO(it) }.toMap()
            ).toIndexable(property.indexed)
        }
    }
}

fun Operators.Operator.toOperatorDTO(): Operators.OperatorDTO {
    return Operators.OperatorDTO(
        type = type,
        ifOperatorOutputType = if (this is Operators.IfOperator) this.outputType else null
    )
}

fun fromOperatorDTO(operatorDTO: Operators.OperatorDTO): Operators.Operator {
    return when (operatorDTO.type) {
        Operators.OperatorType.IF -> {
            val outputType = operatorDTO.ifOperatorOutputType
                ?: throw IllegalArgumentException("ifOperatorOutputType required for type=IF")
            Operators.IfOperator(outputType)
        }

        Operators.OperatorType.GREATER_THAN -> Operators.GreaterThanOperator()
        Operators.OperatorType.UPPERCASE -> Operators.UpperCaseOperator()
    }
}

fun Input.toInputDTO(): InputDTO {
    return InputDTO(
        type = type,
        staticValue = if (this is StaticInput) this.value else null,
        otherPath = if (this is OtherDataInput) this.path else null,
        applyType = if (this is ApplyInput) this.apply.type else null,
        applyOperator = if (this is ApplyInput) this.apply.operator.toOperatorDTO() else null,
        applyInputs = if (this is ApplyInput) this.apply.inputs().map { it.toInputDTO() } else null,
        staticResultType = if (this is StaticInput) outputType else null,
        otherResultType = if (this is OtherDataInput) outputType else null,
    )
}

fun fromInputDTO(inputDTO: InputDTO): Input {
    return when (inputDTO.type) {
        InputType.STATIC -> {
            val staticValue =
                inputDTO.staticValue ?: throw IllegalArgumentException("staticValue required for type=STATIC")
            val outputType =
                inputDTO.staticResultType ?: throw IllegalArgumentException("staticResultType required for type=STATIC")
            StaticInput(staticValue, outputType)
        }

        InputType.OTHER -> {
            val otherPath = inputDTO.otherPath ?: throw IllegalArgumentException("otherPath required for type=OTHER")
            val outputType =
                inputDTO.otherResultType ?: throw IllegalArgumentException("otherResultType required for type=OTHER")
            OtherDataInput(otherPath, outputType)
        }

        InputType.APPLY -> {
            val applyType = inputDTO.applyType ?: throw IllegalArgumentException("applyType required for type=APPLY")
            val operator =
                inputDTO.applyOperator ?: throw IllegalArgumentException("applyOperator required for type=APPLY")
            val applyInputs = inputDTO.applyInputs!!.map { fromInputDTO(it) }
            val apply = when (applyType) {
                ApplyType.MONO -> {
                    check(applyInputs.size == 1) { "Expected exactly one input for mono operator" }
                    MonoApply(
                        fromOperatorDTO(operator) as Operators.MonoOperator,
                        applyInputs.single()
                    )
                }

                ApplyType.DUAL -> {
                    check(applyInputs.size == 2) { "Expected exactly two inputs for dual operator" }
                    DualApply(
                        fromOperatorDTO(operator) as Operators.DualOperator,
                        applyInputs[0],
                        applyInputs[1]
                    )
                }

                ApplyType.TRI -> {
                    check(applyInputs.size == 3) { "Expected exactly three inputs for dual operator" }
                    TriApply(
                        fromOperatorDTO(operator) as Operators.TriOperator,
                        applyInputs[0],
                        applyInputs[1],
                        applyInputs[2]
                    )
                }
            }
            ApplyInput(apply)
        }
    }
}

fun EntityMetadata.toDTO(): EntityMetadataDTO {
    return EntityMetadataDTO(
        collection = collection,
        schema = schema.toSchemaDTO()
    )
}

fun fromEntityMetadataDTO(entityMetadataDTO: EntityMetadataDTO): EntityMetadata {
    return EntityMetadata(
        collection = entityMetadataDTO.collection,
        schema = fromSchemaDTO(entityMetadataDTO.schema)
    )
}
