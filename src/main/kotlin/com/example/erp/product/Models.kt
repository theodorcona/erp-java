package com.example.erp.product

import com.example.erp.entity.IndexedStringPropertyName
import com.example.erp.entity.NoDTOCollectionDescriptor


object PRODUCT_COLLECTION : NoDTOCollectionDescriptor<Product>(
    Product::class.java,
    "product",
    indexedStringProperties = listOf(
        IndexedStringPropertyName("sku")
    )
) {
    object PROPERTIES {
        val sku = indexedStringProperties[0]
    }
}

data class Product(
    val sku: String,
)

data class ProductIdentifier(
    val type: String,
    val value: String
)
