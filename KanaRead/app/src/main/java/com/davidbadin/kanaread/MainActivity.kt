package com.davidbadin.kanaread

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.davidbadin.kanaread.data.BestRecordsRepository
import com.davidbadin.kanaread.data.DatabaseSeeder
import com.davidbadin.kanaread.data.KanaDatabase
import com.davidbadin.kanaread.ui.PracticeScreen
import com.davidbadin.kanaread.ui.SelectionScreen
import com.davidbadin.kanaread.ui.theme.KanaReadTheme
import com.davidbadin.kanaread.viewmodel.PracticeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as KanaReadApplication
        val database = app.database
        val bestRecords = app.bestRecords

        setContent {
            KanaReadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AppRoot(
                        database = database,
                        bestRecords = bestRecords
                    )
                }
            }
        }
    }
}

/**
 * Top-level composable: handles the database-seeding loading state,
 * then hosts the NavHost.
 */
@Composable
private fun AppRoot(
    database: KanaDatabase,
    bestRecords: BestRecordsRepository
) {
    var seeded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            DatabaseSeeder.seedIfNeeded(database)
        }
        seeded = true
    }

    if (!seeded) {
        LoadingScreen()
    } else {
        AppNavHost(database = database, bestRecords = bestRecords)
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Preparing word database…",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AppNavHost(
    database: KanaDatabase,
    bestRecords: BestRecordsRepository
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "selection") {

        composable("selection") {
            SelectionScreen(
                onSelectMode = { mode ->
                    navController.navigate("practice/$mode")
                },
                bestRecords = bestRecords
            )
        }

        composable(
            route = "practice/{mode}",
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStack ->
            val mode = backStack.arguments?.getString("mode") ?: "hiragana"
            val practiceViewModel: PracticeViewModel = viewModel(
                factory = PracticeViewModel.Factory(database, bestRecords)
            )
            PracticeScreen(
                mode = mode,
                viewModel = practiceViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
