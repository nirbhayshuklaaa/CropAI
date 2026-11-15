package com.example

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.style.TextOverflow
import com.example.core.firebase.configureAppCheck
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.LocalAppLanguage
import com.example.ui.MainViewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.CropRecommendationScreen
import com.example.ui.screens.FarmManagementScreen
import com.example.ui.screens.FertilizerRecommendationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KnowledgeCenterScreen
import com.example.ui.screens.OnboardingWizardScreen
import com.example.ui.screens.PestDiagnosticScreen
import com.example.ui.screens.ProfitabilityCalculatorScreen
import com.example.ui.screens.ProductStoreScreen
import com.example.ui.screens.SoilAnalysisScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CropAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalAppLanguage provides uiState.currentLanguage) {
                CropAITheme(dynamicColor = false) {
                    CropAiApp(mainViewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun CropAiApp(mainViewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Automatically check and request location permissions and fetch live GPS weather on app entry
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            mainViewModel.fetchExactLocation()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            mainViewModel.fetchExactLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val navigateToHome: () -> Unit = {
        val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
        if (!popped) {
            navController.navigate(Screen.Home.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.SoilAnalysis.route,
        Screen.CropRecommendation.route,
        Screen.FertilizerRecommendation.route,
        Screen.FarmManagement.route,
        Screen.ProductStore.route,
        Screen.KnowledgeCenter.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    val isHindi = uiState.currentLanguage == AppLanguage.HINDI

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                androidx.compose.foundation.layout.Column {
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.testTag("app_bottom_nav_bar")
                    ) {
                        val navItems = listOf(
                            Triple(Screen.Home.route, if (isHindi) "होम" else "Home", Icons.Default.Home),
                            Triple(Screen.SoilAnalysis.route, if (isHindi) "मिट्टी" else "Soil", Icons.Default.Science),
                            Triple(Screen.CropRecommendation.route, if (isHindi) "फसल" else "Crops", Icons.Default.Grass),
                            Triple(Screen.FertilizerRecommendation.route, if (isHindi) "खाद" else "Fertilizer", Icons.Default.Spa),
                            Triple(Screen.FarmManagement.route, if (isHindi) "खेत" else "My Farm", Icons.Default.Eco)
                        )

                        navItems.forEach { (route, label, icon) ->
                            val isSelected = currentRoute == route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (route == Screen.Home.route) {
                                        navigateToHome()
                                    } else if (currentRoute != route) {
                                        navController.navigate(route) {
                                            popUpTo(Screen.Home.route) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(icon, contentDescription = label)
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("nav_item_${route}")
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        val initialDestination = remember {
            if (uiState.farmerProfile.isOnboarded) Screen.Home.route else Screen.Welcome.route
        }
        NavHost(
            navController = navController,
            startDestination = initialDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    currentLanguage = uiState.currentLanguage,
                    onLanguageSelected = { lang -> mainViewModel.setLanguage(lang) },
                    onStartOnboarding = { navController.navigate(Screen.Onboarding.route) },
                    onCompleteOnboarding = { name, state, district, village ->
                        mainViewModel.completeOnboarding(name, state, district, village)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingWizardScreen(
                    currentLanguage = uiState.currentLanguage,
                    onLanguageSelected = { lang -> mainViewModel.setLanguage(lang) },
                    onFinishOnboarding = { name, state, district, village, farmName, acreage ->
                        mainViewModel.completeOnboardingWithFarm(name, state, district, village, farmName, acreage)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
                HomeScreen(
                    uiState = uiState,
                    onLanguageSelected = { lang -> mainViewModel.setLanguage(lang) },
                    onSignInWithGoogle = { activity?.let { mainViewModel.signInWithGoogle(it) } },
                    onSignOut = { mainViewModel.signOut() },
                    onNavigateToSoilAnalysis = { navController.navigate(Screen.SoilAnalysis.route) },
                    onNavigateToCropRecommendation = { navController.navigate(Screen.CropRecommendation.route) },
                    onNavigateToFertilizerRecommendation = { navController.navigate(Screen.FertilizerRecommendation.route) },
                    onNavigateToProductStore = { navController.navigate(Screen.ProductStore.route) },
                    onNavigateToKnowledgeCenter = { navController.navigate(Screen.KnowledgeCenter.route) },
                    onNavigateToFarmManagement = { navController.navigate(Screen.FarmManagement.route) },
                    onNavigateToPestDiagnostic = { navController.navigate(Screen.PestDiagnostic.route) },
                    onNavigateToProfitabilityCalculator = { navController.navigate(Screen.ProfitabilityCalculator.route) },
                    onDetectExactLocation = { mainViewModel.fetchExactLocation() },
                    onSpeak = { text -> mainViewModel.speakAdvisory(text) },
                    onStopSpeech = { mainViewModel.stopSpeaking() }
                )
            }

            composable(Screen.SoilAnalysis.route) {
                SoilAnalysisScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome,
                    onAnalyzeBitmap = { bitmap -> mainViewModel.analyzeSoilBitmap(bitmap) },
                    onClearError = { mainViewModel.clearSoilScanError() },
                    onLanguageSelected = { lang -> mainViewModel.setLanguage(lang) },
                    onAnalyzeManual = { n, p, k, ph, moisture, source ->
                        mainViewModel.analyzeSoilManual(n, p, k, ph, moisture, source)
                    },
                    onViewRecommendations = {
                        navController.navigate(Screen.CropRecommendation.route)
                    }
                )
            }

            composable(Screen.CropRecommendation.route) {
                CropRecommendationScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome,
                    onNavigateToFertilizer = { navController.navigate(Screen.FertilizerRecommendation.route) },
                    onNavigateToStore = { navController.navigate(Screen.ProductStore.route) },
                    onSubmitFeedback = { isUseful, comment ->
                        mainViewModel.submitFeedback(isUseful, comment)
                    },
                    onSpeak = { text -> mainViewModel.speakAdvisory(text) },
                    onStopSpeech = { mainViewModel.stopSpeaking() }
                )
            }

            composable(Screen.FertilizerRecommendation.route) {
                FertilizerRecommendationScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome,
                    onNavigateToStore = { navController.navigate(Screen.ProductStore.route) },
                    onSubmitFeedback = { isUseful, comment ->
                        mainViewModel.submitFeedback(isUseful, comment)
                    },
                    onSpeak = { text -> mainViewModel.speakAdvisory(text) },
                    onStopSpeech = { mainViewModel.stopSpeaking() }
                )
            }

            composable(Screen.ProductStore.route) {
                ProductStoreScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome,
                    onCategorySelected = { category -> mainViewModel.filterProducts(category) }
                )
            }

            composable(Screen.KnowledgeCenter.route) {
                KnowledgeCenterScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome
                )
            }

            composable(Screen.FarmManagement.route) {
                FarmManagementScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome,
                    onAddField = { name, acre, crop ->
                        mainViewModel.addField(name, acre, crop)
                    },
                    onDeleteField = { fieldId ->
                        mainViewModel.deleteField(fieldId)
                    }
                )
            }

            composable(Screen.PestDiagnostic.route) {
                PestDiagnosticScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome,
                    onSelectCrop = { crop -> mainViewModel.selectDiagnosticCrop(crop) },
                    onToggleSymptom = { symptom -> mainViewModel.toggleDiagnosticSymptom(symptom) },
                    onClearSymptoms = { mainViewModel.clearDiagnosticSymptoms() },
                    onClearError = { mainViewModel.clearLeafScanError() },
                    onSetPhotoUri = { uri -> mainViewModel.setCapturedLeafPhoto(uri) },
                    onDiagnoseLeafBitmap = { bitmap -> mainViewModel.diagnoseLeafBitmap(bitmap) },
                    onRunDiagnosis = { mainViewModel.runDiseaseDiagnosis() },
                    onSpeak = { text -> mainViewModel.speakAdvisory(text) },
                    onStopSpeech = { mainViewModel.stopSpeaking() }
                )
            }

            composable(Screen.ProfitabilityCalculator.route) {
                ProfitabilityCalculatorScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onNavigateHome = navigateToHome,
                    onSelectCrop = { crop -> mainViewModel.selectProfitCrop(crop) },
                    onUpdateAcreage = { acres -> mainViewModel.updateProfitAcreage(acres) },
                    onUpdateParameters = { costs, yield, price ->
                        mainViewModel.updateCustomProfitParameters(costs, yield, price)
                    },
                    onSpeak = { text -> mainViewModel.speakAdvisory(text) },
                    onStopSpeech = { mainViewModel.stopSpeaking() }
                )
            }
        }
    }
}
