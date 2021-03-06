package com.soten.shopping.domain

import com.soten.shopping.data.entity.product.ProductEntity
import com.soten.shopping.data.repository.ProductRepository

internal class GetProductItemUseCase(
    private val productRepository: ProductRepository
): UseCase {

    suspend operator fun invoke(productId: Long): ProductEntity? {
        return productRepository.getProductItem(productId)
    }
}