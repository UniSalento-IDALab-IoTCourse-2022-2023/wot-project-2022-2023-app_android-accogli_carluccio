package com.example.ssgapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.ssgapp.common.JwtDecoder
import com.example.ssgapp.common.LocalStorage
import com.example.ssgapp.common.domain.model.UserRole
import com.example.ssgapp.common.presentation.navigation.AppNavHost
import com.example.ssgapp.common.presentation.navigation.NavigationItem
import com.example.ssgapp.ui.theme.SSGAppTheme
import com.example.ssgapp.util.Event
import com.example.ssgapp.util.EventBus
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SSGAppTheme {
                val lifecycle = LocalLifecycleOwner.current.lifecycle
                LaunchedEffect(key1 = lifecycle) {
                    repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                        EventBus.events.collect { event ->
                            if (event is Event.Toast) {
                                Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFFFF)//MaterialTheme.colorScheme.background
                ) {

                    val jwt: String? = LocalStorage(LocalContext.current).getValue("JWT")
                    var role: UserRole? = null
                    if (jwt != null) {
                        // Controllo che il token non sia scaduto
                        if (isExpired(jwt)) {
                            role = null
                        } else {
                            role = JwtDecoder().getRole(jwt)
                        }
                    }



                    AppNavHost(
                        navController = rememberNavController(),
                        startDestination = when(role) {
                            UserRole.GROUND_WORKER -> NavigationItem.SafetyScannerScreen.route
                            UserRole.EQUIPMENT_OPERATOR -> NavigationItem.MachinerySelectionScreen.route
                            else -> NavigationItem.Login.route
                        }
                    )
                }
            }
        }
    }

    fun isExpired(token: String): Boolean {
        val jwt: DecodedJWT = JWT.decode(token)
        val exp: Long = jwt.expiresAt.time / 1000  // Ottieni il campo 'exp' e convertilo in secondi

        val currentTime = Instant.now().epochSecond  // Ottieni l'ora corrente in secondi

        return exp <= currentTime
    }



    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

    }

    companion object {
        val TAG = "MainActivity"
        val PERMISSION_REQUEST_BACKGROUND_LOCATION = 0
        val PERMISSION_REQUEST_BLUETOOTH_SCAN = 1
        val PERMISSION_REQUEST_BLUETOOTH_CONNECT = 2
        val PERMISSION_REQUEST_FINE_LOCATION = 3
    }

}