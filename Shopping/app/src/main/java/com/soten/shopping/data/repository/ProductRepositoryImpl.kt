package com.soten.shopping.data.repository

import com.soten.shopping.data.db.dao.ProductDao
import com.soten.shopping.data.entity.product.ProductEntity
import com.soten.shopping.data.network.ProductApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(
    private val productApiService: ProductApiService,
    private val ioDispatcher: CoroutineDispatcher,
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun getProductList(): List<ProductEntity> = withContext(ioDispatcher) {
        val response = productApiService.getProducts()
        return@withContext if (response.isSuccessful) {
            response.body()?.items?.map { it.toEntity() } ?: listOf()
        } else {
            listOf()
        }
    }

    override suspend fun getLocalProductList(): List<ProductEntity> = withContext(ioDispatcher) {
        productDao.getAll()
    }

    override suspend fun insertProductItem(productItem: ProductEntity): Long = withContext(ioDispatcher) {
        productDao.insert(productItem)
    }

    override suspend fun insertProductList(ProductList: List<ProductEntity>) = withContext(ioDispatcher) {
        // TODO("Not yet implemented")
    }

    override suspend fun updateProductItem(ProductItem: ProductEntity) = withContext(ioDispatcher) {
        // TODO("Not yet implemented")
    }

    override suspend fun getProductItem(itemId: Long): ProductEntity? = withContext(ioDispatcher) {
        val response = productApiService.getProduct(itemId)
        return@withContext if (response.isSuccessful) {
            response.body()?.toEntity()
        } else {
            null
        }
    }

    override suspend fun deleteAll() = withContext(ioDispatcher) {
        productDao.deleteAll()
    }

    override suspend fun deleteProductItem(id: Long) = withContext(ioDispatcher) {
        // TODO("Not yet implemented")
    }
}