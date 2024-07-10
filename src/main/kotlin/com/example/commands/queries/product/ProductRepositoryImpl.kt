package com.example.commands.queries.product

import com.example.commands.customMatch
import com.example.dao.ProductRepository
import com.example.db.product.*
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.product.ProductRequest
import com.example.models.product.ProductResponse
import com.example.models.util.generateRandomString
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProductById(id: UUID): ProductResponse? = dbQuery {
        return@dbQuery Product.findById(id)
            ?.toProductResponse()
    }

    override suspend fun getAllProducts(): List<ProductResponse> = dbQuery {
        return@dbQuery Product.all()
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toProductResponse() }
    }

    override suspend fun addProduct(product: ProductRequest): ProductResponse = dbQuery {
        val productExists = Product.find { ProductTable.name eq product.name }.singleOrNull()
        if (productExists == null) {
            return@dbQuery Product.new {
                this.name = product.name
                this.description = product.description
                this.price = product.unitPrice
                this.image = product.image
                this.stock = product.stock
                this.reorderLevel = product.reorderLevel
                this.sku = getFirstLettersOfWords(this.name) + "-" + generateRandomString(5)
                this.tsv = "to_tsvector('english', '${this.name} ${this.description}')"
            }.toProductResponse()
        } else {
            throw IllegalArgumentException("Product already exists")
        }
    }

    override suspend fun updateProductQuantity(id: UUID, quantity: Int): Boolean = dbQuery {
        Product.findByIdAndUpdate(id) {
            it.stock += quantity
        }
        return@dbQuery true
    }

    override suspend fun updateProduct(id: UUID, product: ProductRequest): Boolean = dbQuery {
        Product.findByIdAndUpdate(id) { update ->
            update.name = product.name
            update.description = product.description
            update.price = product.unitPrice
            update.image = product.image
            update.stock = product.stock
            update.reorderLevel = product.reorderLevel
        }
        return@dbQuery true
    }

    override suspend fun deleteProduct(id: UUID): Boolean = dbQuery {
        val p = Product.find { ProductTable.id eq id }.singleOrNull()
        p?.delete()
            ?: false
        return@dbQuery true

    }

    override suspend fun searchProduct(query: String): List<ProductResponse> = dbQuery {
        return@dbQuery ProductTable.selectAll()
            .where { ProductTable.tsv.customMatch(query) }
            .map { Product.wrapRow(it).toProductResponse() }
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
    }
}

