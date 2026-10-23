package pe.edu.upeu.pharmamobil.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.data.remote.api.AuthApi
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException

@Composable
fun LoginScreen(
    authApi: AuthApi,
    onLoginExitoso: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text("Iniciar sesión en PharmaSoft")

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !cargando
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !cargando
        )

        if (error != null) {
            Text(error ?: "")
        }

        Button(
            onClick = {
                if (usuario.isBlank() || contrasena.isBlank()) {
                    error = "Ingresa usuario y contraseña"
                } else {
                    scope.launch {
                        cargando = true
                        error = null

                        try {
                            authApi.login(
                                username = usuario.trim(),
                                password = contrasena
                            )
                            contrasena = ""
                            onLoginExitoso()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: ClientRequestException) {
                            error = when (e.response.status.value) {
                                400 -> "Error 400: solicitud de inicio de sesión inválida."
                                401 -> "Error 401: usuario o contraseña incorrectos."
                                403 -> "Error 403: acceso denegado."
                                404 -> "Error 404: no se encontró la ruta de autenticación."
                                else -> "Error HTTP ${e.response.status.value}."
                            }
                        } catch (e: ServerResponseException) {
                            error = "Error ${e.response.status.value}: problema en el servidor PharmaSoft."
                        } catch (e: ResponseException) {
                            error = "Error HTTP ${e.response.status.value}."
                        } catch (e: Exception) {
                            error = "Error de conexión o configuración: ${e::class.simpleName ?: "desconocido"}"
                        } finally {
                            cargando = false
                        }
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator()
            } else {
                Text("Ingresar")
            }
        }
    }
}



