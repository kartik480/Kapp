package com.kurakulas.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import com.kurakulas.app.ui.AddAppointmentScreen
import com.kurakulas.app.ui.LoginScreen
import com.kurakulas.app.ui.MainPanelScreen
import com.kurakulas.app.ui.theme.KurakulasAppTheme
import com.kurakulas.app.data.model.LoginResponse
import androidx.hilt.navigation.compose.hiltViewModel
import com.kurakulas.app.ui.viewmodel.MainPanelViewModel
import com.kurakulas.app.data.repository.LoginRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var loginRepository: LoginRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            KurakulasAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoggedIn by remember { mutableStateOf(false) }
                    val mainPanelViewModel: MainPanelViewModel = hiltViewModel()

                    // Check login state when the activity starts
                    LaunchedEffect(Unit) {
                        isLoggedIn = loginRepository.isLoggedIn()
                    }

                    if (isLoggedIn) {
                        Log.d("MainActivity", "Showing MainPanelScreen")
                        MainPanelScreen(
                            onNavigateToAddAppointment = {
                                // Handle navigation to AddAppointmentScreen
                            },
                            onLogout = {
                                mainPanelViewModel.clearUserData()
                                loginRepository.logout()
                                isLoggedIn = false
                                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Log.d("MainActivity", "Showing LoginScreen")
                        LoginScreen(
                            onLoginSuccess = { loginResponse ->
                                Log.d("MainActivity", "Login success callback received with user: ${loginResponse.user?.firstName} ${loginResponse.user?.lastName}")
                                mainPanelViewModel.updateUserData(loginResponse)
                                isLoggedIn = true
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
