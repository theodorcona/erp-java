package com.example.erp.product

import com.example.erp.entity.CollectionDescriptor
import com.fasterxml.jackson.databind.JsonNode


object PRODUCT_COLLECTION : CollectionDescriptor<Product>(
    Product::class.java,
    "product"
)

data class Product(
    val name: String,
    val description: String,
    val additionalData: JsonNode,
    val identifiers: List<ProductIdentifier>
)

data class ProductIdentifier(
    val type: String,
    val value: String
)