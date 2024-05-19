package com.example.erp.common

import com.example.erp.logic.Input
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer


class InputDeserializer : StdNodeBasedDeserializer<Input>(Input::class.java) {
    override fun convert(p0: JsonNode, p1: DeserializationContext): Input {
        return deserializeInput(p0)
    }
}
class SchemaPropertiesDeserializer :
    StdNodeBasedDeserializer<SchemaProperties.Schema>(SchemaProperties.Schema::class.java) {
    override fun convert(jsonNode: JsonNode, ctx: DeserializationContext): SchemaProperties.Schema {
        return deserializeSchema(jsonNode)
    }
}
