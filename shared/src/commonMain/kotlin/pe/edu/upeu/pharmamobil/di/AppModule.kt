package pe.edu.upeu.pharmamobil.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.core.qualifier.named
import pe.edu.upeu.pharmamobil.data.remote.api.AuthApi
import pe.edu.upeu.pharmamobil.data.remote.api.CategoriaApi
import pe.edu.upeu.pharmamobil.data.remote.api.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.SesionAuth
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioSqlite
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel


val networkModule = module {
    single { SesionAuth() }
    single { crearHttpClient(get()) }
    single { ProductoApi(get(), get(named("urlBase")), get()) }
    single { AuthApi(get(), get(named("urlBase")), get()) }
    single { CategoriaApi(get(), get(named("urlBase")), get()) }
}

val dataModule = module {
    single { PharmaDb(get<CreadorDeDriver>().crear()) }
    single { ProductoLocal(get()) }
    single<ProductoRepository> { ProductoRepositorioSqlite(get(), get()) }
    single<ClienteRepository> { ClienteRepositorioEnMemoria() }
}

val domainModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}

val presentationModule = module {

    viewModel {
        ProductoViewModel(
            get(), // RegistrarProductoUseCase
            get(), // ListarProductosUseCase
            get(), // CategoriaApi
            get(), // ProductoRepository
            get()  // CompartirProducto
        )
    }

    viewModel { ClienteViewModel(get(), get()) }
}


expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            networkModule,
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}








