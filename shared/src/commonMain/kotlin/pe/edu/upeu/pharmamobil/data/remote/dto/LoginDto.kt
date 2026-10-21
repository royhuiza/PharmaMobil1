package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String = "Bearer",
    val expiraEnSegundos: Long = 0L,
    val username: String = "",
    val nombreCompleto: String = "",
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)
