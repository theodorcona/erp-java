package com.example.erp.logic.implementation

import com.example.erp.logic.DataTransformer
import com.example.erp.logic.Input
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class DataTransformerImpl(
    private val objectMapper: ObjectMapper,
) : DataTransformer {
    override fun transform(
        input: String,
        properties: Map<String, Input>
    ): String {
        return objectMapper.writeValueAsString(applyDynamicInput(input, properties))
    }

    private fun applyDynamicInput(input: String, dynamicProperties: Map<String, Input>): Map<String, Any> {
        val data = objectMapper.readTree(input)
        return dynamicProperties.mapValues{ it.value.apply(data) }
    }
}