package com.example.erp.orders

import com.example.erp.entity.IndexedStringPropertyName
import com.example.erp.entity.NoDTOCollectionDescriptor
import com.example.erp.product.PRODUCT_COLLECTION
import java.util.*

object TRANSFER_ORDER_COLLECTION : NoDTOCollectionDescriptor<TransferOrder>(
    TransferOrder::class.java,
    "transfer_orfer",
    indexedStringProperties = listOf(
        IndexedStringPropertyName("sku")
    )
) {
    object PROPERTIES {
        val sku = PRODUCT_COLLECTION.indexedStringProperties[0]
    }
}

data class TransferOrder(
    val productTransfer: ProductTransfer,
    val deliveryAddress: String,
    val status: TransferStatus,
)

enum class TransferStatus {
    COMPLETED, CANCELED, IN_TRANSIT, SCHEDULED
}

data class ProductTransfer(
    val quantity: Int,
    val productId: UUID
)
