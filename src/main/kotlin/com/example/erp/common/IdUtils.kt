package com.example.erp.common

import org.joda.time.DateTime
import java.util.*

data class AnyWithId<out T>(
    val id: UUID,
    val obj: T
)

