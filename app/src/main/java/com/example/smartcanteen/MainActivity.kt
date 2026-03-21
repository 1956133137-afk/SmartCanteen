package com.example.smartcanteen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartcanteen.presentation.balance.BalanceQueryScreen
import com.example.smartcanteen.presentation.payment.PaymentScreen
import com.example.smartcanteen.presentation.whitelist.WhitelistSyncScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "payment") {
                    composable("payment") {
                        PaymentScreen(
                            onMenuItemClick = { title ->
                                when (title) {
                                    "白名单同步" -> navController.navigate("whitelist_sync")
                                }
                            },
                            onBalanceClick = { 
                                navController.navigate("balance_query_screen") 
                            }
                        )
                    }
                    composable("whitelist_sync") {
                        WhitelistSyncScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("balance_query_screen") {
                        BalanceQueryScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
