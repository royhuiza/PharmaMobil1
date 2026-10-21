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
