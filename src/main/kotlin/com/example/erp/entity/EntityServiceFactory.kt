package com.example.erp.entity

interface EntityServiceFactory {
    fun <T : Any, DTO : Any> getServiceForEntity(collectionDescriptor: CollectionDescriptor<T, DTO>): EntityService<T>
    fun getServiceForGenericEntity(collectionName: String): EntityService<Map<String, Any>>
}