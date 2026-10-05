package pe.edu.upeu.pharmamobil.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
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

    suspend fun obtener(id: Long): ProductoResponseDto {
        return client.get("${urlBase}productos/$id").body()
    }

    suspend fun crear(
        request: ProductoRequestDto
    ): ProductoResponseDto {
        return client.post("${urlBase}productos") {
            setBody(request)
        }.body()
    }

    suspend fun actualizar(
        id: Long,
        request: ProductoRequestDto
    ): ProductoResponseDto {
        return client.put("${urlBase}productos/$id") {
            setBody(request)
        }.body()
    }

    suspend fun eliminar(id: Long) {
        client.delete("${urlBase}productos/$id")
    }
}