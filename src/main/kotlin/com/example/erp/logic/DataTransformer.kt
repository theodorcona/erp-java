package com.example.erp.logic

import com.example.erp.common.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

interface DataTransformer {
    fun transform(input: String, properties: Map<String, Input>): String
}

@JsonDeserialize(using = InputDeserializer::class)
interface Input {
    val type: InputType
    fun apply(data: JsonNode): Any
    fun outputType(): SchemaProperties.PropertyType
}

class StaticInput(val value: Any, val propertyType: SchemaProperties.PropertyType): Input {
    override val type = InputType.STATIC
    override fun apply(data: JsonNode): Any {
        return value
    }
    override fun outputType(): SchemaProperties.PropertyType {
        return propertyType
    }
}

class OtherDataInput(
    val path: String,
    val propertyType: SchemaProperties.PropertyType
): Input {
    override val type = InputType.OTHER
    override fun apply(data: JsonNode): Any {
        return getValueAtPath(data, path.split("_")).toValue(propertyType)
    }

    override fun outputType(): SchemaProperties.PropertyType {
        return propertyType
    }
}

class ApplyInput(val apply: Apply): Input {
    override val type = InputType.APPLY
    override fun apply(data: JsonNode): Any {
        return apply.calculate(data)
    }

    override fun outputType(): SchemaProperties.PropertyType {
        return apply.operator.outputType()
    }
}

enum class InputType {
    STATIC, OTHER, APPLY
}

enum class ApplyType {
    MONO, DUAL, TRI
}

interface Apply{
    val type: ApplyType
    fun calculate(data: JsonNode) : Any
    val operator: Operators.Operator
    fun inputs(): List<Input>
}

class MonoApply(
    override val operator: Operators.MonoOperator,
    val input1: Input,
) : Apply {
    override val type = ApplyType.MONO
    override fun calculate(data: JsonNode): Any {
        return operator.apply(input1.apply(data))
    }

    override fun inputs(): List<Input> {
        return listOf(input1)
    }
}

class DualApply(
    override val operator: Operators.DualOperator,
    val input1: Input,
    val input2: Input
) : Apply {
    override val type = ApplyType.DUAL
    override fun calculate(data: JsonNode): Any {
        return operator.apply(input1.apply(data), input2.apply(data))
    }
    override fun inputs(): List<Input> {
        return listOf(input1, input2)
    }
}

class TriApply(
    override val operator: Operators.TriOperator,
    val input1: Input,
    val input2: Input,
    val input3: Input
) : Apply {
    override val type = ApplyType.TRI
    override fun calculate(data: JsonNode) : Any {
        return operator.apply(input1.apply(data), input2.apply(data), input3.apply(data))
    }
    override fun inputs(): List<Input> {
        return listOf(input1, input2, input3)
    }
}
