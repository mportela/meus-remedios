package com.meusremedios.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meusremedios.R
import com.meusremedios.ui.medications.detail.MedicationDetailScreen
import com.meusremedios.ui.medications.form.MedicationFormScreen
import com.meusremedios.ui.medications.list.MedicationListScreen
import com.meusremedios.ui.recognition.RecognitionScreen
import com.meusremedios.ui.settings.SettingsScreen
import com.meusremedios.ui.today.TodayScreen

/** Destinos exibidos na barra de navegação inferior. */
private enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int,
) {
    RECOGNITION(Routes.RECOGNITION, Icons.Default.Search, R.string.nav_recognition),
    TODAY(Routes.TODAY, Icons.Default.DateRange, R.string.nav_today),
    MEDICATIONS(Routes.MEDICATIONS_LIST, Icons.AutoMirrored.Filled.List, R.string.nav_medications),
    SETTINGS(Routes.SETTINGS, Icons.Default.Settings, R.string.nav_settings),
}

/** Grafo de navegação principal do app. */
@Composable
fun MeusRemediosNavHost(
    modifier: Modifier = Modifier,
    navigateToRecognition: Boolean = false,
    onRecognitionNavigated: () -> Unit = {},
) {
    val navController = rememberNavController()

    LaunchedEffect(navigateToRecognition) {
        if (navigateToRecognition) {
            navController.navigate(Routes.RECOGNITION) {
                launchSingleTop = true
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            }
            onRecognitionNavigated()
        }
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar =
        TopLevelDestination.entries.any { dest ->
            currentDestination?.hierarchy?.any { it.route == dest.route } == true
        }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { dest ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = {
                                Text(
                                    stringResource(dest.labelRes),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.RECOGNITION,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            composable(Routes.RECOGNITION) {
                RecognitionScreen()
            }
            composable(Routes.TODAY) {
                TodayScreen(
                    onOpenMedication = { id -> navController.navigate(Routes.medicationDetail(id)) },
                )
            }
            composable(Routes.MEDICATIONS_LIST) {
                MedicationListScreen(
                    onAddMedication = { navController.navigate(Routes.medicationForm()) },
                    onOpenMedication = { id -> navController.navigate(Routes.medicationDetail(id)) },
                )
            }
            composable(
                route = Routes.MEDICATION_DETAIL_PATTERN,
                arguments =
                    listOf(
                        navArgument(Routes.ARG_MEDICATION_ID) { type = NavType.LongType },
                    ),
            ) {
                MedicationDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.medicationForm(id)) },
                )
            }
            composable(
                route = Routes.MEDICATION_FORM_PATTERN,
                arguments =
                    listOf(
                        navArgument(Routes.ARG_MEDICATION_ID) {
                            type = NavType.LongType
                            defaultValue = 0L
                        },
                    ),
            ) {
                MedicationFormScreen(
                    onDone = { navController.popBackStack() },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}
