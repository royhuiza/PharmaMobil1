
package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobil.platform.CompartirProducto

actual val platformModule: Module = module {

    single<HttpClientEngine> {
        MockEngine { request ->
            error(
                "MockEngine de pruebas: no se configuró " +
                        "una respuesta para ${request.url}"
            )
        }
    }

    single(named("urlBase")) {
        "http://localhost:8080/api/v1/"
    }

    single<CompartirProducto> {
        object : CompartirProducto {
            override fun compartir(texto: String) {
                // No abre interfaces gráficas en JVM.
            }
        }
    }
}
