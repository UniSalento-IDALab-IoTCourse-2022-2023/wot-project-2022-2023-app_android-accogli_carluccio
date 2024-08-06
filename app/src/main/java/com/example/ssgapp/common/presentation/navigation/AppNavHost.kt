package com.example.ssgapp.common.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ssgapp.common.presentation.login_screen.LoginScreen
import com.example.ssgapp.groundworker.presentation.safety_scanner_screen.SafetyScannerScreen
import com.example.ssgapp.operator.presentation.machinery_dashboard_screen.MachineryDashboardScreen
import com.example.ssgapp.operator.presentation.machinery_selection_screen.MachinerySelectionScreen
import com.example.ssgapp.groundworker.presentation.safety_scanner_screen.SafetyScannerViewModel
import org.altbeacon.beacon.Beacon

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String
    ) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            NavigationItem.Login.route,
            enterTransition = null,
            exitTransition = null) {
            LoginScreen(navController)
        }

        composable(
            NavigationItem.SafetyScannerScreen.route,
            enterTransition = null,
            exitTransition = null
        ) {
            SafetyScannerScreen(
                navController = navController
            )
        }

        composable(
            NavigationItem.MachinerySelectionScreen.route,
            enterTransition = null,
            exitTransition = null
        ) {
            MachinerySelectionScreen(navController = navController)
        }

        composable(
            "${NavigationItem.MachineryDashboardScreen.route}/{machineryId}/{machineryMacAddress}/{isRemote}",//NavigationItem.MachineryDashboardScreen.route,
            arguments = listOf( // superfluo fare questo. funziona anche senza definire "arguments"
                navArgument("machineryId"){ type = NavType.StringType },
                navArgument("machineryMacAddress"){ type = NavType.StringType },
                navArgument("isRemote"){ type = NavType.StringType }
            ),
            enterTransition = null,
            exitTransition = null
        ) { backStackEntry ->
            val machineryId = backStackEntry.arguments?.getString("machineryId")!!
            val machineryMacAddress = backStackEntry.arguments?.getString("machineryMacAddress")!!
            val isRemote = backStackEntry.arguments?.getString("isRemote").toBoolean()

            MachineryDashboardScreen(
                machineryId = machineryId,
                machineryMacAddress = machineryMacAddress,
                isRemote = isRemote,
                navController = navController
            )
        }

    }
}