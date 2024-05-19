package com.example.erp.common

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.joda.time.DateTime

object SchemaProperties {
    @JsonDeserialize(using = SchemaPropertiesDeserializer::class)
    data class Schema(
        val properties: Map<kotlin.String, PropertyTypeIndexable>
    )
    data class PropertyTypeIndexable(
        val property: Property,
        val indexed: kotlin.Boolean = false,
    )
    sealed class Property(val type: PropertyType) {
        fun toIndexable(indexed: kotlin.Boolean = false) = PropertyTypeIndexable(this, indexed)
    }
    data class Object(val properties: Map<kotlin.String, PropertyTypeIndexable>) : Property(PropertyType.OBJECT)
    class DynamicObject : Property(PropertyType.DYNAMIC_OBJECT)
    data class Array(val property: PropertyTypeIndexable) : Property(PropertyType.ARRAY)
    class Long : Property(PropertyType.LONG)
    class String : Property(PropertyType.STRING)
    class Date : Property(PropertyType.DATE)
    class Boolean : Property(PropertyType.BOOLEAN)
    enum class PropertyType {
        OBJECT, DYNAMIC_OBJECT, ARRAY, LONG, STRING, DATE, BOOLEAN
    }
}
fun Any.toBoolean() : Boolean {
    return kotlin.runCatching { this as Boolean }
        .getOrElse { throw IllegalStateException("Could not convert $this to Boolean") }
}
fun Any.toDate() : DateTime{
    return kotlin.runCatching { DateTime.parse(this as String) }
        .getOrElse { throw IllegalStateException("Could not convert $this to Date") }
}
fun Any.toStringType() : String {
    return kotlin.runCatching { this as String }
        .getOrElse { throw IllegalStateException("Could not convert $this to String") }
}
fun Any.toLong() : Long {
    return kotlin.runCatching { this as Long }
        .getOrElse { throw IllegalStateException("Could not convert $this to Long") }
}
object Operators {
    sealed class Operator(val type: OperatorType) {
        abstract fun outputType(): SchemaProperties.PropertyType
    }
    sealed class MonoOperator(type: OperatorType) : Operator(type) {
        abstract fun inputType1(): SchemaProperties.PropertyType
        abstract fun apply(input: Any): Any
    }
    sealed class DualOperator(type: OperatorType) : Operator(type) {
        abstract fun inputType1(): SchemaProperties.PropertyType
        abstract fun inputType2(): SchemaProperties.PropertyType
        abstract fun apply(input1: Any, input2: Any): Any
    }
    sealed class TriOperator(type: OperatorType) : Operator(type) {
        abstract fun inputType1(): SchemaProperties.PropertyType
        abstract fun inputType2(): SchemaProperties.PropertyType
        abstract fun inputType3(): SchemaProperties.PropertyType
        abstract fun apply(input1: Any, input2: Any, input3: Any): Any
    }
    class IfOperator(val outputType: SchemaProperties.PropertyType) : TriOperator(OperatorType.IF) {
        override fun inputType1() = SchemaProperties.PropertyType.BOOLEAN
        override fun inputType2() = outputType
        override fun inputType3() = outputType
        override fun apply(input1: Any, input2: Any, input3: Any): Any {
            return if (input1.toBoolean()) input2  else input3
        }
        override fun outputType() = outputType
    }
    class GreaterThanOperator : DualOperator(OperatorType.GREATER_THAN) {
        override fun inputType1() = SchemaProperties.PropertyType.LONG
        override fun inputType2() = SchemaProperties.PropertyType.LONG
        override fun apply(input1: Any, input2: Any): Any {
            return (input1.toLong() > input2.toLong())
        }

        override fun outputType() = SchemaProperties.PropertyType.BOOLEAN
    }
    class UpperCaseOperator : MonoOperator(OperatorType.UPPERCASE) {
        override fun inputType1() = SchemaProperties.PropertyType.STRING
        override fun apply(input1: Any): Any {
            return (input1.toStringType().uppercase())
        }

        override fun outputType() = SchemaProperties.PropertyType.STRING
    }

    enum class OperatorType {
        IF, GREATER_THAN, UPPERCASE
    }
}
