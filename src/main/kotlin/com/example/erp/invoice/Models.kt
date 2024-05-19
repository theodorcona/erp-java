package com.example.erp.invoice

import com.example.erp.entity.CollectionDescriptor
import org.joda.time.DateTime
import java.util.*

object INVOICE_COLLECTION : CollectionDescriptor<Invoice>(
    Invoice::class.java,
    "invoice"
)

data class Invoice(
    val id: UUID,
    val type: InvoiceType,
    val productId: UUID?,
    val creationDate: DateTime,
    val paymentDate: DateTime,
    val amountLowestDenomination: Int,
    val currency: String
)

enum class InvoiceType {
    OUTGOING, INCOMING
}
