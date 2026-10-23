package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class FakeProductoRepository(
    private val productos: MutableList<Producto> = mutableListOf()
) : ProductoRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null

    private var siguienteId = 1L

    private val flujoProductos = MutableStateFlow(productos.toList())

    override suspend fun registrar(producto: Producto): Producto {
        fallaAlRegistrar?.let { throw it }

        val guardado = producto.copy(id = siguienteId++)
        productos.add(guardado)

        flujoProductos.value = productos.toList()

        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        val indice = productos.indexOfFirst { it.id == producto.id }
        require(indice >= 0) { "Producto no encontrado" }

        productos[indice] = producto
        flujoProductos.value = productos.toList()
        return producto
    }

    override suspend fun eliminar(id: Long) {
        val eliminado = productos.removeAll { it.id == id }
        require(eliminado) { "Producto no encontrado" }

        flujoProductos.value = productos.toList()
    }

    override suspend fun listar(): List<Producto> {
        fallaAlListar?.let { throw it }
        return productos.toList()
    }

    override fun observar(): Flow<List<Producto>> =
        flujoProductos.onStart {
            fallaAlListar?.let { throw it }
        }

    override suspend fun sincronizar(): Int {
        return 0
    }
}

