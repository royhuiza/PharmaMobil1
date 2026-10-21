package pe.edu.upeu.pharmamobil.di

import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverAndroid
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CreadorDeDriver> { CreadorDeDriverAndroid(androidContext()) }
    single<HttpClientEngine> {
        OkHttp.create()
    }

    single(named("urlBase")) {
        "http://10.0.2.2:8080/api/v1/"
    }
}

