package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

/**
 * Repositorio falso utilizado exclusivamente en las pruebas.
 * Permite simular operaciones CRUD y errores sin acceder al servidor.
 */
class FakeProductoRepository(
    private val productos: MutableList<Producto> = mutableListOf()
) : ProductoRepository {

    var fallaAlRegistrar: Throwable? = null
    var fallaAlListar: Throwable? = null
    var fallaAlActualizar: Throwable? = null
    var fallaAlEliminar: Throwable? = null

    private var siguienteId =
        (productos.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(producto: Producto): Producto {
        fallaAlRegistrar?.let { throw it }

        val guardado = producto.copy(id = siguienteId++)
        productos.add(guardado)
        return guardado
    }

    override suspend fun listar(): List<Producto> {
        fallaAlListar?.let { throw it }

        return productos.toList()
    }

    override suspend fun obtener(id: Long): Producto {
        return productos.firstOrNull { it.id == id }
            ?: throw NoSuchElementException(
                "Producto con id $id no encontrado"
            )
    }

    override suspend fun actualizar(producto: Producto): Producto {
        fallaAlActualizar?.let { throw it }

        val indice = productos.indexOfFirst { it.id == producto.id }

        if (indice == -1) {
            throw NoSuchElementException(
                "Producto con id ${producto.id} no encontrado"
            )
        }

        productos[indice] = producto
        return producto
    }

    override suspend fun eliminar(id: Long) {
        fallaAlEliminar?.let { throw it }

        val eliminado = productos.removeAll { it.id == id }

        if (!eliminado) {
            throw NoSuchElementException(
                "Producto con id $id no encontrado"
            )
        }
    }
}