package com.freshcheck.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.freshcheck.ai.data.AppDatabase
import com.freshcheck.ai.data.ProductEntity
import com.freshcheck.ai.data.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing product data and logic.
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProductRepository
    val allProducts: Flow<List<ProductEntity>>

    init {
        val productDao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(productDao)
        allProducts = repository.allProducts
    }

    /**
     * Adds a new product to the database.
     */
    fun addProduct(name: String, expiryDate: Long) {
        viewModelScope.launch {
            val newProduct = ProductEntity(
                name = name,
                expiryDate = expiryDate
            )
            repository.insert(newProduct)
        }
    }

    /**
     * Removes a product from the database.
     */
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }

    /**
     * Helper to get status color/label logic.
     * Logic: < 3 days = Yellow; Expired = Red; Else = Normal (Green).
     */
    fun getProductStatus(product: ProductEntity): ProductStatus {
        val currentTime = System.currentTimeMillis()
        val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L
        
        return when {
            product.expiryDate < currentTime -> ProductStatus.EXPIRED
            product.expiryDate < currentTime + threeDaysInMillis -> ProductStatus.EXPIRING_SOON
            else -> ProductStatus.FRESH
        }
    }

    enum class ProductStatus {
        EXPIRED,        // Red
        EXPIRING_SOON,  // Yellow
        FRESH           // Normal (Green)
    }
}
