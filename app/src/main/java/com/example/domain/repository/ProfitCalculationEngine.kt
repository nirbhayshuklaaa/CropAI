package com.example.domain.repository

import com.example.domain.model.CropBenchmark
import com.example.domain.model.CropCostBreakdown
import com.example.domain.model.ProfitCalculationResult

class ProfitCalculationEngine {

    val benchmarks = listOf(
        CropBenchmark(
            cropName = "Wheat",
            cropNameHindi = "गेहूं",
            defaultYieldQuintalsPerAcre = 20.0,
            mspRatePerQuintal = 2275.0,
            openMarketRatePerQuintal = 2450.0,
            defaultCosts = CropCostBreakdown(
                seedCostPerAcre = 2200.0,
                fertilizerCostPerAcre = 3500.0,
                irrigationCostPerAcre = 2400.0,
                laborAndTillageCostPerAcre = 4500.0,
                pesticideCostPerAcre = 1200.0,
                harvestingMachineryCostPerAcre = 3000.0
            )
        ),
        CropBenchmark(
            cropName = "Rice",
            cropNameHindi = "धान (चावल)",
            defaultYieldQuintalsPerAcre = 25.0,
            mspRatePerQuintal = 2300.0,
            openMarketRatePerQuintal = 2650.0,
            defaultCosts = CropCostBreakdown(
                seedCostPerAcre = 1800.0,
                fertilizerCostPerAcre = 4200.0,
                irrigationCostPerAcre = 4000.0,
                laborAndTillageCostPerAcre = 6500.0,
                pesticideCostPerAcre = 1800.0,
                harvestingMachineryCostPerAcre = 3500.0
            )
        ),
        CropBenchmark(
            cropName = "Cotton",
            cropNameHindi = "कपास",
            defaultYieldQuintalsPerAcre = 10.0,
            mspRatePerQuintal = 7120.0,
            openMarketRatePerQuintal = 7500.0,
            defaultCosts = CropCostBreakdown(
                seedCostPerAcre = 3200.0,
                fertilizerCostPerAcre = 5500.0,
                irrigationCostPerAcre = 3500.0,
                laborAndTillageCostPerAcre = 7000.0,
                pesticideCostPerAcre = 4500.0,
                harvestingMachineryCostPerAcre = 4000.0
            )
        ),
        CropBenchmark(
            cropName = "Tomato",
            cropNameHindi = "टमाटर",
            defaultYieldQuintalsPerAcre = 120.0,
            mspRatePerQuintal = 1200.0,
            openMarketRatePerQuintal = 1800.0,
            defaultCosts = CropCostBreakdown(
                seedCostPerAcre = 4500.0,
                fertilizerCostPerAcre = 8500.0,
                irrigationCostPerAcre = 5000.0,
                laborAndTillageCostPerAcre = 12000.0,
                pesticideCostPerAcre = 6500.0,
                harvestingMachineryCostPerAcre = 6000.0
            )
        ),
        CropBenchmark(
            cropName = "Potato",
            cropNameHindi = "आलू",
            defaultYieldQuintalsPerAcre = 110.0,
            mspRatePerQuintal = 1150.0,
            openMarketRatePerQuintal = 1600.0,
            defaultCosts = CropCostBreakdown(
                seedCostPerAcre = 16000.0, // Seed tubers are costly
                fertilizerCostPerAcre = 8000.0,
                irrigationCostPerAcre = 4000.0,
                laborAndTillageCostPerAcre = 7500.0,
                pesticideCostPerAcre = 4500.0,
                harvestingMachineryCostPerAcre = 5000.0
            )
        ),
        CropBenchmark(
            cropName = "Mustard",
            cropNameHindi = "सरसों",
            defaultYieldQuintalsPerAcre = 8.5,
            mspRatePerQuintal = 5650.0,
            openMarketRatePerQuintal = 5900.0,
            defaultCosts = CropCostBreakdown(
                seedCostPerAcre = 1200.0,
                fertilizerCostPerAcre = 2800.0,
                irrigationCostPerAcre = 1800.0,
                laborAndTillageCostPerAcre = 3500.0,
                pesticideCostPerAcre = 1200.0,
                harvestingMachineryCostPerAcre = 2500.0
            )
        ),
        CropBenchmark(
            cropName = "Maize",
            cropNameHindi = "मक्का",
            defaultYieldQuintalsPerAcre = 24.0,
            mspRatePerQuintal = 2090.0,
            openMarketRatePerQuintal = 2250.0,
            defaultCosts = CropCostBreakdown(
                seedCostPerAcre = 2500.0,
                fertilizerCostPerAcre = 4000.0,
                irrigationCostPerAcre = 2500.0,
                laborAndTillageCostPerAcre = 4200.0,
                pesticideCostPerAcre = 1600.0,
                harvestingMachineryCostPerAcre = 2800.0
            )
        )
    )

    fun getBenchmark(cropName: String): CropBenchmark {
        return benchmarks.firstOrNull { it.cropName.equals(cropName, ignoreCase = true) }
            ?: benchmarks.first()
    }

    fun calculateProfit(
        cropName: String,
        acreage: Double,
        customCosts: CropCostBreakdown? = null,
        customYieldPerAcre: Double? = null,
        customPricePerQuintal: Double? = null
    ): ProfitCalculationResult {
        val benchmark = getBenchmark(cropName)
        val validAcreage = if (acreage <= 0.0) 1.0 else acreage
        val costs = customCosts ?: benchmark.defaultCosts
        val yieldPerAcre = customYieldPerAcre ?: benchmark.defaultYieldQuintalsPerAcre
        val pricePerQuintal = customPricePerQuintal ?: benchmark.openMarketRatePerQuintal

        val costPerAcre = costs.totalCostPerAcre
        val totalCost = costPerAcre * validAcreage
        val totalYield = yieldPerAcre * validAcreage
        val grossRevenue = totalYield * pricePerQuintal
        val netProfit = grossRevenue - totalCost
        val netProfitPerAcre = netProfit / validAcreage
        val roi = if (totalCost > 0) (netProfit / totalCost) * 100.0 else 0.0
        val breakEven = if (totalYield > 0) totalCost / totalYield else 0.0

        return ProfitCalculationResult(
            cropName = benchmark.cropName,
            acreage = validAcreage,
            totalCost = totalCost,
            costPerAcre = costPerAcre,
            expectedTotalYieldQuintals = totalYield,
            sellingPricePerQuintal = pricePerQuintal,
            grossRevenue = grossRevenue,
            netProfit = netProfit,
            netProfitPerAcre = netProfitPerAcre,
            returnOnInvestmentPercent = roi,
            breakEvenPricePerQuintal = breakEven
        )
    }
}
