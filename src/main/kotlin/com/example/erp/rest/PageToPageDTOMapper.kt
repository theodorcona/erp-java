package com.example.erp.rest

data class PageDTO<T>(
    val items: List<T>,
    val nextCursor: String?
) {
    fun <T2> map(block: (T) -> T2): PageDTO<T2> {
        return PageDTO(this.items.map { block(it) }, nextCursor)
    }
}

data class RangeQuery(
    val cursor: String?,
    val pageSize: Int
)
