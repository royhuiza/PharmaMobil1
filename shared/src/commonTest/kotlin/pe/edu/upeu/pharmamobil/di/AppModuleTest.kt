
package pe.edu.upeu.pharmamobil.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

import pe.edu.upeu.pharmamobil.data.remote.SesionAuth
import pe.edu.upeu.pharmamobil.data.remote.api.CategoriaApi
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioEnMemoria

import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

import pe.edu.upeu.pharmamobil.platform.CompartirProducto
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {

    @BeforeTest
    fun instalarMain() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun detenerKoin() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun grafoCompleto(): Koin {

        val clienteDePrueba = HttpClient(
            MockEngine {
                respond(
                    content = "[]",
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        "application/json"
                    )
                )
            }
        ) {
            install(ContentNegotiation) {
                json()
            }
        }

        val sesionDePrueba = SesionAuth().apply {
            guardarToken("token-de-prueba")
        }

        return startKoin {
            modules(
                module {

                    single<ProductoRepository> {
                        ProductoRepositorioEnMemoria()
                    }

                    single<ClienteRepository> {
                        ClienteRepositorioEnMemoria()
                    }

                    single<CategoriaApi> {
                        CategoriaApi(
                            client = clienteDePrueba,
                            urlBase = "http://localhost:8080/api/v1/",
                            sesionAuth = sesionDePrueba
                        )
                    }

                    single<CompartirProducto> {
                        object : CompartirProducto {
                            override fun compartir(texto: String) {
                                // No abre aplicaciones durante las pruebas.
                            }
                        }
                    }
                },

                domainModule,
                presentationModule
            )
        }.koin
    }

    @Test
    fun resuelveLosRepositoriosPorSuInterfazDeDominio() {
        val koin = grafoCompleto()

        assertIs<ProductoRepositorioEnMemoria>(
            koin.get<ProductoRepository>()
        )

        assertIs<ClienteRepositorioEnMemoria>(
            koin.get<ClienteRepository>()
        )
    }

    @Test
    fun losRepositoriosSonUnicosEnTodaLaAplicacion() {
        val koin = grafoCompleto()

        assertSame(
            koin.get<ProductoRepository>(),
            koin.get<ProductoRepository>()
        )

        assertSame(
            koin.get<ClienteRepository>(),
            koin.get<ClienteRepository>()
        )
    }

    @Test
    fun resuelveLosCuatroCasosDeUsoConSusRepositorios() {
        val koin = grafoCompleto()

        koin.get<RegistrarProductoUseCase>()
        koin.get<ListarProductosUseCase>()
        koin.get<RegistrarClienteUseCase>()
        koin.get<ListarClientesUseCase>()
    }

    @Test
    fun resuelveLosViewModelConSusCasosDeUso() {
        val koin = grafoCompleto()

        koin.get<ProductoViewModel>()
        koin.get<ClienteViewModel>()
    }
}
