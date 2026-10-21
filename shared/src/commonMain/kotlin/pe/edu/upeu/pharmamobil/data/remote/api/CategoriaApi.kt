package pe.edu.upeu.pharmamobil.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import pe.edu.upeu.pharmamobil.data.remote.SesionAuth
import pe.edu.upeu.pharmamobil.data.remote.dto.CategoriaResponseDto

class CategoriaApi(
    private val client: HttpClient,
    private val urlBase: String,
    private val sesionAuth: SesionAuth
) {
    suspend fun listarCategorias(): List<CategoriaResponseDto> {
        val token = sesionAuth.obtenerToken()
            ?: error("Debes iniciar sesión para consultar categorías")

        return client.get("${urlBase}categorias") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }.body()
    }
}
