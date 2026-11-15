package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object SoilAnalysis : Screen("soil_analysis")
    data object CropRecommendation : Screen("crop_recommendation")
    data object FertilizerRecommendation : Screen("fertilizer_recommendation")
    data object FarmManagement : Screen("farm_management")
    data object ProductStore : Screen("product_store")
    data object KnowledgeCenter : Screen("knowledge_center")
    data object ProfileSettings : Screen("profile_settings")
    data object History : Screen("history")
    data object PestDiagnostic : Screen("pest_diagnostic")
    data object ProfitabilityCalculator : Screen("profitability_calculator")
}
