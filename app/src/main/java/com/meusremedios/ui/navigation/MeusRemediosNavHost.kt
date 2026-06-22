package com.meusremedios.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meusremedios.ui.medications.form.MedicationFormScreen
import com.meusremedios.ui.medications.list.MedicationListScreen

/** Grafo de navegação principal do app. */
@Composable
fun MeusRemediosNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.MEDICATIONS_LIST,
        modifier = modifier,
    ) {
        composable(Routes.MEDICATIONS_LIST) {
            MedicationListScreen(
                onAddMedication = { navController.navigate(Routes.medicationForm()) },
                onOpenMedication = { id -> navController.navigate(Routes.medicationForm(id)) },
            )
        }
        composable(
            route = Routes.MEDICATION_FORM_PATTERN,
            arguments = listOf(
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
    }
}
