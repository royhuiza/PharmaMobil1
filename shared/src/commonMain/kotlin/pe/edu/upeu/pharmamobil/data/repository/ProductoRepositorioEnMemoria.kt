package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {

    private val candado = Mutex()
    private val productos = mutableListOf<Producto>()
    private val productosFlow = MutableStateFlow<List<Producto>>(emptyList())
    private var siguienteId = 1L

    override suspend fun registrar(producto: Producto): Producto {
        delay(RETARDO_REGISTRO_MS)

        return candado.withLock {
            val guardado = producto.copy(id = siguienteId++)
            productos.add(guardado)

            productosFlow.value = productos.toList()

            guardado
        }
    }

    override suspend fun actualizar(producto: Producto): Producto =
        candado.withLock {
            val indice = productos.indexOfFirst { it.id == producto.id }
            require(indice >= 0) { "Producto no encontrado" }

            productos[indice] = producto
            productosFlow.value = productos.toList()
            producto
        }

    override suspend fun eliminar(id: Long) {
        candado.withLock {
            val eliminado = productos.removeAll { it.id == id }
            require(eliminado) { "Producto no encontrado" }

            productosFlow.value = productos.toList()
        }
    }
    override suspend fun listar(): List<Producto> {
        delay(RETARDO_LISTADO_MS)

        return candado.withLock {
            productos.toList()
        }
    }

    override fun observar(): Flow<List<Producto>> =
        productosFlow.asStateFlow()

    private companion object {
        const val RETARDO_REGISTRO_MS = 400L
        const val RETARDO_LISTADO_MS = 600L
    }

    override suspend fun sincronizar(): Int {
        return 0
    }
}


