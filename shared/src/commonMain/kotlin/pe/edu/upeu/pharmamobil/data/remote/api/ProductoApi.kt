package pe.edu.upeu.pharmamobil.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto

class ProductoApi(
    private val client: HttpClient,
    private val urlBase: String
) {
    suspend fun listarProductos(
        pagina: Int = 0,
        tamanio: Int = 20
    ): PaginaResponseDto<ProductoResponseDto> {
        return client.get("${urlBase}productos") {
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
        }.body()
    }
}
