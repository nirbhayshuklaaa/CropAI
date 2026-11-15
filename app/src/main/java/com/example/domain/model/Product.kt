package com.example.domain.model

enum class ProductCategory {
    SEEDS,
    FERTILIZERS,
    ORGANIC_BIO,
    SOIL_AMENDMENTS,
    TOOLS_EQUIPMENT,
    IRRIGATION
}

data class Product(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val category: ProductCategory,
    val description: String,
    val priceInr: Double,
    val imageUrl: String,
    val externalUrl: String, // Legitimate URL (e.g. Flipkart, IFFCO, National Seeds Corp)
    val retailerName: String = "Flipkart",
    val inStock: Boolean = true,
    val relatedCropIds: List<String> = emptyList(),
    val relatedNutrients: List<String> = emptyList() // "N", "P", "K", "pH"
)
