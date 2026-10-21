package pe.edu.upeu.pharmamobil.di

import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverIos
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CreadorDeDriver> { CreadorDeDriverIos() }
    single<HttpClientEngine> {
        Darwin.create()
    }

    single(named("urlBase")) {
        "http://localhost:8080/api/v1/"
    }
}

