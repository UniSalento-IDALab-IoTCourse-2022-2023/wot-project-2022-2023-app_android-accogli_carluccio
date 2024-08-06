package com.example.ssgapp.common.presentation.login_screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ssgapp.common.presentation.navigation.NavigationItem

@OptIn(ExperimentalComposeUiApi::class)
@Composable // TODO: da cambiare in onLoginSuccess: () -> Unit. lo tengo giusto perché è un esempio completo
fun LoginScreen(navController: NavHostController, /*onLoginSuccess: (Boolean) -> Unit, */authenticationViewModel: AuthenticationViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val username by authenticationViewModel.username
    val password by authenticationViewModel.password
    val focusManager = LocalFocusManager.current

    // Per nascondere tastiera quando l'utente tappa una parte dello schermo che non sia una textfield
    // Ottieni il riferimento al servizio SoftwareKeyboardController
    val keyboardController = LocalSoftwareKeyboardController.current

    // Stato per tenere traccia se la tastiera è attualmente visibile o meno
    var isKeyboardVisible by remember { mutableStateOf(false) }

    // Funzione per nascondere la tastiera
    fun hideKeyboard() {
        keyboardController?.hide()
        isKeyboardVisible = false
    }

    BackHandler {
        // Ottieni l'attività corrente e spostala in background
        (context as? Activity)?.moveTaskToBack(true)
    }

    // Effetto per nascondere la tastiera quando l'utente tocca un punto diverso dalla tastiera stessa
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            // Nascondi la tastiera quando l'utente tocca altrove
            keyboardController?.hide()
        }
    }

    Box( // Questo contenitore serve per la logica per nascondere tastiera
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    // Quando l'utente tocca lo schermo, nascondi la tastiera
                    hideKeyboard()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /*
            SelectionContainer {
                Text("pwd")
            }*/

            Text(
                text = "SSGApp",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(38.dp))
            Text(
                text = "Log in",
                fontSize = 18.sp
            )

            OutlinedTextField(
                value = username,
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                onValueChange = { authenticationViewModel.setUsername(it) },
                label = { Text("Username") },
                //isError = isGuessWrong,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = { /*onKeyboardDone()*/ }
                ),
                //keyboardActions = KeyboardActions(onNext = { /* Handle next field */ })
            )
            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = password,
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                onValueChange = { authenticationViewModel.setPassword(it) },
                label = { Text("Password") },
                //isError = isGuessWrong,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password
                ),
                keyboardActions = KeyboardActions(
                    onDone = { /*onKeyboardDone()*/
                        focusManager.clearFocus()
                        authenticationViewModel.login(
                            {
                                navController.navigate(NavigationItem.SafetyScannerScreen.route) { popUpTo(NavigationItem.Login.route) { inclusive = true } }
                            },
                            {
                                navController.navigate(NavigationItem.MachinerySelectionScreen.route) { popUpTo(NavigationItem.Login.route) { inclusive = true } }
                            }
                        )
                    }
                ),
                visualTransformation = PasswordVisualTransformation(),


                )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    authenticationViewModel.setUsername("PX441JP")
                    authenticationViewModel.setPassword("nIaXNHGfucO4")

                    authenticationViewModel.login(
                        {
                            navController.navigate(NavigationItem.SafetyScannerScreen.route) { popUpTo(NavigationItem.Login.route) { inclusive = true } }
                        },
                        {
                            navController.navigate(NavigationItem.MachinerySelectionScreen.route) { popUpTo(NavigationItem.Login.route) { inclusive = true } }
                        }
                    )

                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Login groundworker")
            }
            Button(
                onClick = {
                    authenticationViewModel.setUsername("SR230DC")
                    authenticationViewModel.setPassword("Ba0WQ1hXvS6t")

                    authenticationViewModel.login(
                        {
                            navController.navigate(NavigationItem.SafetyScannerScreen.route) { popUpTo(NavigationItem.Login.route) { inclusive = true } }
                        },
                        {
                            navController.navigate(NavigationItem.MachinerySelectionScreen.route) { popUpTo(NavigationItem.Login.route) { inclusive = true } }
                        }
                    )

                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Login operator")
            }


            /* // TODO: messo giusto per testare il TODO 1 in SafetyScannerViewModel
            Button(
                onClick = {
                    Log.d("DEBUG", "${authenticationViewModel.getAuthenticationState()}")
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Print state")
            }*/
        }
    }
}
