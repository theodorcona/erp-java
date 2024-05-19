package com.example.erp.entity

import java.util.*

interface IndexService {
    fun insertEntityIndex(id: UUID)
    fun updateEntityIndex(previousEntity: Entity, id: UUID)
    fun removeEntityIndex(id: UUID)
}