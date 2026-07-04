package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.AppViewModel
import com.example.ui.screens.AddProductScreen
import com.example.ui.screens.BlockScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.MapScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ButtonDefaults
import java.util.UUID
import com.example.ui.screens.LoginScreen
import com.example.data.UserSession

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val database = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java,
        "laguinera_db"
    )
    .fallbackToDestructiveMigration(true)
    .build()
    val repository = AppRepository(database.appDao())
    
    val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    var userId = sharedPrefs.getString("user_id", null)
    if (userId == null) {
        userId = UUID.randomUUID().toString()
        sharedPrefs.edit().putString("user_id", userId).apply()
    }
        
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LaGuineraApp(repository, userId!!)
        }
      }
    }
  }
}

@Composable
fun LaGuineraApp(repository: AppRepository, userId: String) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    val viewModel: AppViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val sessionUserId = sharedPrefs.getString("session_user_id", null)
                val initialSession = if (sessionUserId != null) {
                    val sessionName = sharedPrefs.getString("session_name", "") ?: ""
                    val sessionEmail = sharedPrefs.getString("session_email", "") ?: ""
                    val sessionAvatar = sharedPrefs.getString("session_avatar", "") ?: ""
                    val sessionIsAnonymous = sharedPrefs.getBoolean("session_is_anonymous", true)
                    val sessionUID = sharedPrefs.getString("session_uid", "") ?: ""
                    UserSession(sessionUserId, sessionName, sessionEmail, sessionAvatar, sessionIsAnonymous, sessionUID)
                } else {
                    null
                }
                return AppViewModel(repository, userId, initialSession) as T
            }
        }
    )

    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val startDestination = if (sharedPrefs.contains("session_user_id")) "map" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("map") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("map") {
            MapScreen(
                viewModel = viewModel,
                onLogout = {
                    viewModel.logout(context)
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                onBlockSelected = { blockName ->
                    viewModel.setBlock(blockName)
                    navController.navigate("block/$blockName")
                },
                onCartClicked = { navController.navigate("cart") },
                onSearchClicked = { navController.navigate("search") },
                onMyProductsClicked = { navController.navigate("my_products") },
                onDeveloperMode = { navController.navigate("developer") },
                onChatClicked = { navController.navigate("p2p_chat") }
            )
        }

        composable("p2p_chat") {
            com.example.ui.screens.P2PChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("cart") {
            CartScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            com.example.ui.screens.SearchScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("my_products") {
            com.example.ui.screens.MyProductsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddNew = { navController.navigate("add_product/none") },
                onLoginRedirect = {
                    viewModel.logout(context)
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }

        composable("developer") {
            com.example.ui.screens.DeveloperScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = "block/{blockName}",
            arguments = listOf(navArgument("blockName") { type = NavType.StringType })
        ) { backStackEntry ->
            val blockName = backStackEntry.arguments?.getString("blockName") ?: "Bloque 1"
            BlockScreen(
                blockName = blockName,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate("add_product/$blockName") },
                onLoginRedirect = {
                    viewModel.logout(context)
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }
        
        composable(
            route = "add_product/{blockName}",
            arguments = listOf(navArgument("blockName") { type = NavType.StringType })
        ) { backStackEntry ->
            val blockName = backStackEntry.arguments?.getString("blockName") ?: "Bloque 1"
            AddProductScreen(
                blockName = blockName,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }

    val userSession by viewModel.userSession.collectAsState()

    val unseenNotifications by viewModel.unseenNotifications.collectAsState(initial = emptyList())
    val activeUnseenNotifications = unseenNotifications.filter { it.isActive }
    if (activeUnseenNotifications.isNotEmpty()) {
        val notification = activeUnseenNotifications.first()
        val context = LocalContext.current
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.markNotificationSeen(notification.id) },
            title = { androidx.compose.material3.Text(notification.title) },
            text = { androidx.compose.material3.Text(notification.message) },
            confirmButton = {
                androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (notification.buttonText.isNotBlank() && notification.buttonUrl.isNotBlank()) {
                        androidx.compose.material3.TextButton(
                            onClick = { 
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(notification.buttonUrl))
                                context.startActivity(intent)
                                viewModel.markNotificationSeen(notification.id) 
                            }
                        ) {
                            androidx.compose.material3.Text(notification.buttonText)
                        }
                    }
                    androidx.compose.material3.Button(onClick = { viewModel.markNotificationSeen(notification.id) }) {
                        androidx.compose.material3.Text("Entendido")
                    }
                }
            }
        )
    }

    val surveys by viewModel.surveys.collectAsState(initial = emptyList())
    val activeSurveys = surveys.filter { it.isActive }
    
    val latestSurvey = activeSurveys.firstOrNull()
    if (latestSurvey != null) {
        var showResults by remember(latestSurvey.id) { mutableStateOf(false) }
        val hasVoted = sharedPrefs.getBoolean("survey_voted_${latestSurvey.id}", false)
        
        if (!hasVoted || showResults) {
            val isAnonymous = userSession?.isAnonymous ?: true

            androidx.compose.material3.AlertDialog(
                onDismissRequest = { 
                    sharedPrefs.edit().putBoolean("survey_voted_${latestSurvey.id}", true).apply()
                    showResults = false
                },
                title = { 
                    androidx.compose.material3.Text(
                        text = if (showResults || hasVoted) "Resultados de la Encuesta" else latestSurvey.question,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        if (latestSurvey.imageUrl.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = latestSurvey.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        
                        if (showResults || hasVoted) {
                            androidx.compose.material3.Text(
                                text = latestSurvey.question,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            val totalVotes = latestSurvey.results.values.sum()
                            latestSurvey.options.forEachIndexed { index, option ->
                                val votesForOption = latestSurvey.results[index.toString()] ?: 0
                                val percentage = if (totalVotes > 0) votesForOption.toFloat() / totalVotes else 0f
                                val percentageText = "${(percentage * 100).toInt()}%"
                                
                                androidx.compose.foundation.layout.Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    androidx.compose.foundation.layout.Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                                    ) {
                                        androidx.compose.material3.Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        androidx.compose.material3.Text(
                                            text = "$percentageText ($votesForOption votos)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    androidx.compose.material3.LinearProgressIndicator(
                                        progress = percentage,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        } else {
                            if (isAnonymous) {
                                androidx.compose.material3.Text(
                                    "Función Restringida",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                androidx.compose.material3.Text(
                                    "Para participar en las encuestas y decisiones del barrio, debes iniciar sesión con una cuenta de Google.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                androidx.compose.material3.Button(
                                    onClick = {
                                        viewModel.logout(context)
                                        navController.navigate("login") {
                                            popUpTo(0)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    androidx.compose.material3.Text("Acceder con Google")
                                }
                            } else {
                                latestSurvey.options.forEachIndexed { index, option ->
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            viewModel.voteSurvey(latestSurvey.id, index.toString())
                                            sharedPrefs.edit().putBoolean("survey_voted_${latestSurvey.id}", true).apply()
                                            showResults = true
                                        },
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        androidx.compose.material3.Text(option)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (showResults || hasVoted) {
                        androidx.compose.material3.Button(
                            onClick = { 
                                showResults = false 
                            }
                        ) {
                            androidx.compose.material3.Text("Cerrar")
                        }
                    } else {
                        androidx.compose.material3.TextButton(
                            onClick = { 
                                sharedPrefs.edit().putBoolean("survey_voted_${latestSurvey.id}", true).apply()
                            }
                        ) {
                            androidx.compose.material3.Text("Omitir")
                        }
                    }
                }
            )
        }
    }
}
