package pe.edu.upeu.pharmamobil.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import pe.edu.upeu.pharmamobil.data.remote.SesionAuth
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.LoginResponseDto

class AuthApi(
    private val client: HttpClient,
    private val urlBase: String,
    private val sesionAuth: SesionAuth
) {
    suspend fun login(
        username: String,
        password: String
    ): LoginResponseDto {
        val respuesta: LoginResponseDto =
            client.post("${urlBase}auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequestDto(username, password))
            }.body()

        sesionAuth.guardarToken(respuesta.token)

        return respuesta
    }
}
