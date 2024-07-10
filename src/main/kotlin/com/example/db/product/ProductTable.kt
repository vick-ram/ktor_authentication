package com.example.db.product

import com.example.db.util.CustomUUIDEntity
import com.example.db.util.CustomUUIDEntityClass
import com.example.db.util.CustomUUIDTable
import com.example.db.util.tsVector
import com.example.models.product.ProductResponse
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

object ProductTable : CustomUUIDTable("products") {
    val name = varchar("name", 255)
    val description = text("description", eagerLoading = true)
    val price = decimal("price", 10, 2)
    val image = varchar("image", 255).nullable()
    val stock = integer("stock").default(1)
    val reorderLevel = integer("reorder_level").default(1)
    val sku = varchar("sku", 50).nullable()
    val tsv = tsVector("tsv")
}

class Product(id: EntityID<UUID>) : CustomUUIDEntity(id, ProductTable) {
    companion object : CustomUUIDEntityClass<Product>(ProductTable)

    var name by ProductTable.name
    var description by ProductTable.description
    var price by ProductTable.price
    var image by ProductTable.image
    var stock by ProductTable.stock
    var reorderLevel by ProductTable.reorderLevel
    var sku by ProductTable.sku
    var tsv by ProductTable.tsv

    fun toProductResponse() = ProductResponse(
        productId = this.id.value,
        name = this.name,
        description = this.description,
        unitPrice = this.price,
        image = this.image,
        stock = this.stock,
        reorderLevel = this.reorderLevel,
        sku = sku ?: "",
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun getFirstLettersOfWords(input: String): String {
    return input.split(" ").joinToString("") { it.first().uppercase() }
}
