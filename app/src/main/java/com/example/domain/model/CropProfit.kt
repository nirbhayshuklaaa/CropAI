package com.example.domain.model

data class CropCostBreakdown(
    val seedCostPerAcre: Double,
    val fertilizerCostPerAcre: Double,
    val irrigationCostPerAcre: Double,
    val laborAndTillageCostPerAcre: Double,
    val pesticideCostPerAcre: Double,
    val harvestingMachineryCostPerAcre: Double
) {
    val totalCostPerAcre: Double
        get() = seedCostPerAcre + fertilizerCostPerAcre + irrigationCostPerAcre +
                laborAndTillageCostPerAcre + pesticideCostPerAcre + harvestingMachineryCostPerAcre
}

data class CropBenchmark(
    val cropName: String,
    val cropNameHindi: String,
    val defaultYieldQuintalsPerAcre: Double,
    val mspRatePerQuintal: Double,
    val openMarketRatePerQuintal: Double,
    val defaultCosts: CropCostBreakdown
)

data class ProfitCalculationResult(
    val cropName: String,
    val acreage: Double,
    val totalCost: Double,
    val costPerAcre: Double,
    val expectedTotalYieldQuintals: Double,
    val sellingPricePerQuintal: Double,
    val grossRevenue: Double,
    val netProfit: Double,
    val netProfitPerAcre: Double,
    val returnOnInvestmentPercent: Double,
    val breakEvenPricePerQuintal: Double
)
