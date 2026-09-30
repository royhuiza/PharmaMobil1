package pe.edu.upeu.pharmamobil.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<HttpClientEngine> {
        Darwin.create()
    }

    single(named("urlBase")) {
        "http://localhost:8080/api/v1/"
    }
}
