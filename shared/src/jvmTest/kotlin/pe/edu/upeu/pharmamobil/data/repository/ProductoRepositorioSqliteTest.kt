package pe.edu.upeu.pharmamobil.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.SesionAuth
import pe.edu.upeu.pharmamobil.data.remote.api.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductoRepositorioSqliteTest {
    private val json = Json { ignoreUnknownKeys = true }

    private class Entorno(
        val driver: JdbcSqliteDriver,
        val local: ProductoLocal,
        val repositorio: ProductoRepositorioSqlite,
        val cliente: HttpClient
    ) {
        fun cerrar() {
            cliente.close()
            driver.close()
        }
    }

    private fun crearEntorno(
        productosRemotos: MutableList<ProductoResponseDto> = mutableListOf(),
        permitirRed: Boolean = true,
        alRegistrar: (() -> Unit)? = null
    ): Entorno {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        val local = ProductoLocal(PharmaDb(driver))
        var siguienteId = (productosRemotos.maxOfOrNull { it.id } ?: 100L) + 1L

        val cliente = HttpClient(MockEngine { solicitud ->
            if (!permitirRed) error("Sin conexión simulada")
            val respuesta = when (solicitud.method) {
                HttpMethod.Get -> {
                    json.encodeToString(
                        PaginaResponseDto(
                            contenido = productosRemotos.toList(),
                            pagina = 0,
                            tamanio = 20,
                            totalElementos = productosRemotos.size.toLong(),
                            totalPaginas = 1,
                            esUltima = true
                        )
                    )
                }
                HttpMethod.Post -> {
                    alRegistrar?.invoke()
                    val nuevo = ProductoResponseDto(
                        id = siguienteId++,
                        nombre = "Producto local",
                        precio = 12.0,
                        stock = 3,
                        categoriaId = 1L
                    )
                    productosRemotos.add(nuevo)
                    json.encodeToString(nuevo)
                }
                else -> error("Método inesperado: ${solicitud.method}")
            }
            respond(
                content = respuesta,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }) {
            install(ContentNegotiation) { json(json) }
        }
        val sesion = SesionAuth().apply { guardarToken("token-de-prueba") }
        val api = ProductoApi(cliente, "http://localhost/api/v1/", sesion)
        return Entorno(driver, local, ProductoRepositorioSqlite(local, api), cliente)
    }

    private fun producto(nombre: String = "Producto local") = Producto(
        id = 0L, nombre = nombre, precio = 12.0, stock = 3, categoriaId = 1L
    )

    @Test
    fun sincronizacionConRedGuardaProductosYActualizaFlow() = runTest {
        val remoto = mutableListOf(
            ProductoResponseDto(10L, "Paracetamol", 8.5, 7, categoriaId = 1L),
            ProductoResponseDto(11L, "Ibuprofeno", 9.0, 4, categoriaId = 1L)
        )
        val entorno = crearEntorno(remoto)
        try {
            assertEquals(0, entorno.repositorio.sincronizar())
            assertEquals(2, entorno.local.listar().size)
            assertEquals(setOf(10L, 11L), entorno.repositorio.observar().first().map { it.id }.toSet())
        } finally { entorno.cerrar() }
    }

    @Test
    fun registroOfflineQuedaPendienteYSeObservaEnFlow() = runTest {
        val entorno = crearEntorno(permitirRed = false)
        try {
            val creado = entorno.repositorio.registrar(producto())
            assertTrue(creado.id < 0L)
            assertEquals(creado.id, entorno.local.listarPendientes().single().id)
            assertEquals(creado.id, entorno.repositorio.observar().first().single().id)
        } finally { entorno.cerrar() }
    }

    @Test
    fun reconexionSubePendienteYQuitaMarca() = runTest {
        var publicaciones = 0
        val entorno = crearEntorno(alRegistrar = { publicaciones++ })
        try {
            val creado = entorno.repositorio.registrar(producto())
            assertEquals(1, entorno.repositorio.sincronizar())
            assertEquals(1, publicaciones)
            assertTrue(entorno.local.listarPendientes().isEmpty())
            assertFalse(entorno.local.listar().any { it.id == creado.id })
            assertTrue(entorno.local.listar().single().id > 0L)
        } finally { entorno.cerrar() }
    }

    @Test
    fun descargaNoEliminaPendienteLocal() = runTest {
        val remoto = mutableListOf(
            ProductoResponseDto(21L, "Remoto", 5.0, 2, categoriaId = 1L)
        )
        val entorno = crearEntorno(remoto)
        try {
            // Sin categoría válida no se envía; debe permanecer durante la descarga.
            val pendiente = entorno.repositorio.registrar(producto("Sin categoría").copy(categoriaId = null))
            assertEquals(0, entorno.repositorio.sincronizar())
            assertTrue(entorno.local.listarPendientes().any { it.id == pendiente.id })
            assertEquals(setOf(pendiente.id, 21L), entorno.local.listar().map { it.id }.toSet())
        } finally { entorno.cerrar() }
    }

    @Test
    fun idsLocalesNegativosYRemotosPositivosConviven() = runTest {
        val entorno = crearEntorno(mutableListOf(ProductoResponseDto(50L, "Remoto", 6.0, 4)))
        try {
            entorno.repositorio.sincronizar()
            val local = entorno.repositorio.registrar(producto())
            assertTrue(local.id < 0L)
            assertEquals(2, entorno.local.listar().map { it.id }.distinct().size)
            assertTrue(entorno.local.listar().any { it.id == 50L })
        } finally { entorno.cerrar() }
    }

    @Test
    fun sincronizarDosVecesNoDuplicaProductos() = runTest {
        var publicaciones = 0
        val entorno = crearEntorno(alRegistrar = { publicaciones++ })
        try {
            entorno.repositorio.registrar(producto())
            assertEquals(1, entorno.repositorio.sincronizar())
            assertEquals(0, entorno.repositorio.sincronizar())
            assertEquals(1, publicaciones)
            assertEquals(1, entorno.local.listar().size)
            assertTrue(entorno.local.listarPendientes().isEmpty())
        } finally { entorno.cerrar() }
    }
}
