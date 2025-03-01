package com.example.erp.user

import com.example.erp.entity.NoDTOCollectionDescriptor
import java.util.*

object USER_COLLECTION : NoDTOCollectionDescriptor<User>(
    User::class.java,
    "user"
)

data class User(
    val id: UUID,
    val email: String,
    val name: String,
    val type: String
)
