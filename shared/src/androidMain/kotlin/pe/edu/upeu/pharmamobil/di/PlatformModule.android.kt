
package pe.edu.upeu.pharmamobil.di

import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriverAndroid
import pe.edu.upeu.pharmamobil.platform.CompartirProducto
import pe.edu.upeu.pharmamobil.platform.CompartirProductoAndroid
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {

    // Base de datos SQLite
    single<CreadorDeDriver> {
        CreadorDeDriverAndroid(androidContext())
    }

    // Motor de conexión HTTP
    single<HttpClientEngine> {
        OkHttp.create()
    }

    // Dirección del backend PharmaSoft
    single(named("urlBase")) {
        "http://172.20.10.3:8080/api/v1/"
    }

    // Compartir mediante el menú nativo de Android
    single<CompartirProducto> {
        CompartirProductoAndroid(androidContext())
    }
}
