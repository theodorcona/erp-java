package com.example.erp.logic

import com.example.erp.common.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

interface DataTransformer {
    fun transform(input: String, properties: Map<String, Input>): String
}

data class InputDTO(
    val type: InputType,
    val staticValue: Any?,
    val staticResultType: SchemaProperties.PropertyType?,
    val otherPath: String?,
    val otherResultType: SchemaProperties.PropertyType?,
    val applyType: ApplyType?,
    val applyOperator: Operators.OperatorDTO?,
    val applyInputs: List<InputDTO>?
)

interface Input {
    val type: InputType
    val outputType: SchemaProperties.PropertyType
    fun apply(data: JsonNode): Any
}

class StaticInput(
    val value: Any,
    override val outputType: SchemaProperties.PropertyType
): Input {
    override val type = InputType.STATIC
    override fun apply(data: JsonNode): Any {
        return value
    }
}

class OtherDataInput(
    val path: String,
    override val outputType: SchemaProperties.PropertyType
): Input {
    override val type = InputType.OTHER
    override fun apply(data: JsonNode): Any {
        return getValueAtPath(data, path.split("_")).toValue(outputType)
    }
}

class ApplyInput(val apply: Apply): Input {
    override val type = InputType.APPLY
    override fun apply(data: JsonNode): Any {
        return apply.calculate(data)
    }

    override val outputType = apply.operator.outputType()
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
