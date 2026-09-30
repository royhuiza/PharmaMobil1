package pe.edu.upeu.pharmamobil.data.api

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobil.data.remote.api.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductoApiTest {

    @Test
    fun listarProductosRealizaGetConParametrosCorrectos() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/productos", request.url.encodedPath)
            assertEquals("0", request.url.parameters["pagina"])
            assertEquals("20", request.url.parameters["tamanio"])

            respond(
                content = ByteReadChannel(
                    """
                    {
                        "contenido": [
                            {
                                "id": 1,
                                "nombre": "Paracetamol",
                                "precio": 5.5,
                                "stock": 100
                            }
                        ],
                        "pagina": 0,
                        "tamanio": 20,
                        "totalElementos": 1,
                        "totalPaginas": 1,
                        "esUltima": true
                    }
                    """.trimIndent()
                ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = crearHttpClient(mockEngine)
        val api = ProductoApi(client, "http://10.0.2.2:8080/api/v1/")

        val resultado = api.listarProductos()

        assertEquals(1, resultado.contenido.size)
        assertEquals("Paracetamol", resultado.contenido.first().nombre)
        assertEquals(5.5, resultado.contenido.first().precio)
        assertEquals(100, resultado.contenido.first().stock)
    }
}
