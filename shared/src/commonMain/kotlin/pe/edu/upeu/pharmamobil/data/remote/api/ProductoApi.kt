package pe.edu.upeu.pharmamobil.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import pe.edu.upeu.pharmamobil.data.remote.SesionAuth
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto

class ProductoApi(
    private val client: HttpClient,
    private val urlBase: String,
    private val sesionAuth: SesionAuth
) {
    suspend fun listarProductos(
        pagina: Int = 0,
        tamanio: Int = 20
    ): PaginaResponseDto<ProductoResponseDto> {
        val token = sesionAuth.obtenerToken()
            ?: error("Debes iniciar sesión para consultar el servidor")

        return client.get("${urlBase}productos") {
            bearerAuth(token)
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
        }.body()
    }
    suspend fun registrarProducto(
        producto: ProductoRequestDto
    ): ProductoResponseDto {
        val token = sesionAuth.obtenerToken()
            ?: error("Debes iniciar sesión para sincronizar productos")

        return client.post("${urlBase}productos") {
            bearerAuth(token)
            setBody(producto)
        }.body()
    }
}

