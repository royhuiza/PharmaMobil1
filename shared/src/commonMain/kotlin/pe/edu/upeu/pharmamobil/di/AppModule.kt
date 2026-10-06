package pe.edu.upeu.pharmamobil.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

import pe.edu.upeu.pharmamobil.data.remote.api.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobil.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobil.data.repository.ProductoRepositorioRest

import pe.edu.upeu.pharmamobil.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase

import pe.edu.upeu.pharmamobil.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobil.presentation.producto.ProductoViewModel


val networkModule = module {

    single {
        crearHttpClient(get())
    }

    single {
        ProductoApi(
            get(),
            get(named("urlBase"))
        )
    }
}


val dataModule = module {

    single<ProductoRepository> {
        ProductoRepositorioRest(
            api = get(),
            categoriaPorDefecto = 1L
        )
    }

    single<ClienteRepository> {
        ClienteRepositorioEnMemoria()
    }
}


val domainModule = module {

    factory {
        RegistrarProductoUseCase(get())
    }

    factory {
        ListarProductosUseCase(get())
    }

    factory {
        ActualizarProductoUseCase(get())
    }

    factory {
        EliminarProductoUseCase(get())
    }

    factory {
        RegistrarClienteUseCase(get())
    }

    factory {
        ListarClientesUseCase(get())
    }
}


val presentationModule = module {

    viewModel {
        ProductoViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        ClienteViewModel(
            get(),
            get()
        )
    }
}


expect val platformModule: Module


fun initKoin(
    configuracionAdicional: KoinApplication.() -> Unit = {}
) {
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