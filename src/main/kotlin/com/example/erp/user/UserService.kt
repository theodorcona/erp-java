package com.example.erp.user

import com.example.erp.entity.CollectionDescriptor
import java.util.*

object USER_COLLECTION : CollectionDescriptor<User>(
    User::class.java,
    "user"
)

data class User(
    val id: UUID,
    val email: String,
    val name: String,
    val type: String
)
