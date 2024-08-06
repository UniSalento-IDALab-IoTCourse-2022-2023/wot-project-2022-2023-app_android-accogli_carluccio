package com.example.ssgapp.common.presentation.login_screen

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssgapp.common.JwtDecoder
import com.example.ssgapp.common.LocalStorage
import com.example.ssgapp.common.domain.model.User
import com.example.ssgapp.common.domain.model.UserRole
import com.example.ssgapp.common.domain.repository.AuthenticationRepository
import com.example.ssgapp.common.domain.repository.WorkerRepository
import com.example.ssgapp.operator.presentation.util.sendEvent
import com.example.ssgapp.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.net.NetworkInterface
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authenticationRepository: AuthenticationRepository, //<- Interfaccia! e non implementazione!
    private val workerRepository: WorkerRepository
): ViewModel() {
    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoginSuccess = mutableStateOf(false)
    val isLoginSuccess: State<Boolean> = _isLoginSuccess

    private val _isLoginError = mutableStateOf(false)
    val isLoginError: State<Boolean> = _isLoginError


    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun getMacAddress(): String {
        try {
            val all: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in all) {
                if (!networkInterface.name.equals("wlan0", ignoreCase = true)) continue

                val macBytes: ByteArray = networkInterface.hardwareAddress ?: return "02:00:00:00:00:00"
                val macAddress = StringBuilder()
                for (b in macBytes) {
                    macAddress.append(String.format("%02X:", b))
                }
                if (macAddress.isNotEmpty()) {
                    macAddress.deleteCharAt(macAddress.length - 1)
                }
                return macAddress.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "02:00:00:00:00:00"
    }
    fun login(
        navigateToGroundWorkerScreen: () -> Unit,
        navigateToEquipmentOperatorScreen: () -> Unit,
    ) { // implementare logica di login (API) qui



        viewModelScope.launch {

            val username = username.value
            val password = password.value
            val macAddress = getMacAddress()
            Log.d("LOGIN", "Logging ${username} ${password} ${macAddress}")

            val user = User(username = username, password = password, macAddress = macAddress)

            authenticationRepository.login(user = user)
                .onRight {
                    Log.d("LOGIN", "JWT Token: ${it.jwt}")

                    val jwt = it.jwt
                    val role = JwtDecoder().getRole(it.jwt)!!
                    if (role != UserRole.GROUND_WORKER && role != UserRole.EQUIPMENT_OPERATOR) {
                        // errore: non è ne di un ruolo ne dell'altro
                        _isLoginError.value = true
                        _isLoginSuccess.value = false
                        throw Exception("An error occurred!")
                    }

                    _isLoginError.value = false
                    _isLoginSuccess.value = true

                    // salvo info utente nello storage locale
                    LocalStorage(context = context).saveValue("JWT", jwt)

                    // Ottengo oggetto worker (dato che mi serve il workerId)
                    workerRepository.getWorkerFrom(JwtDecoder().getUserId(it.jwt))
                        .onRight {
                            // Salvo info utente nello storage locale
                            LocalStorage(context = context).saveValue("WORKER_ID", it.workerId)

                            // Vado nella relativa schermata, a seconda che sia groundworker o operator
                            if (role == UserRole.GROUND_WORKER) {
                                navigateToGroundWorkerScreen()
                            } else if (role == UserRole.EQUIPMENT_OPERATOR) {
                                navigateToEquipmentOperatorScreen()
                            }
                        }
                        .onLeft { error ->
                            _isLoginError.value = true

                            // rimuovo info utente dallo storage locale
                            LocalStorage(context = context).removeValue("JWT")

                            // funzione di estensione implementata in ViewModelExt.kt
                            sendEvent(Event.Toast("Login failed"))
                            error.t?.message?.let { Log.d("Network", it) }
                        }
                }
                .onLeft { error ->
                    _isLoginError.value = true

                    // funzione di estensione implementata in ViewModelExt.kt
                    sendEvent(Event.Toast("Login failed"))
                    error.t?.message?.let { Log.d("Network", it) }
                }


        }


    }

    fun logout() { // implementare logica di login (API) qui
        _isLoginSuccess.value = false
    }

    fun getAuthenticationState(): Boolean {
        return _isLoginSuccess.value
    }



    // Altre logiche del login, come la validazione delle credenziali, la chiamata API, ecc., se necessario
}