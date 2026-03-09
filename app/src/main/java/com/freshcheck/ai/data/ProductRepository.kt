package com.freshcheck.ai.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository to abstract data operations for ProductEntity.
 */
class ProductRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun insert(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun delete(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun update(product: ProductEntity) {
        productDao.updateProduct(product)
    }
}
