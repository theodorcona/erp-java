package com.example.erp.entity

interface EntityServiceFactory {
    fun <T : Any> getServiceForEntity(collectionDescriptor: CollectionDescriptor<T>): EntityService<T>
}