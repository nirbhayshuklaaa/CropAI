package com.example.domain.model

data class Farm(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val state: String = "",
    val district: String = "",
    val village: String = "",
    val totalAcreage: Double = 0.0,
    val fields: List<Field> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Field(
    val id: String = "",
    val farmId: String = "",
    val name: String = "",
    val acreage: Double = 0.0,
    val currentCrop: String? = null,
    val previousCrops: List<String> = emptyList(),
    val latestSoilReportId: String? = null,
    val notes: String = ""
)
