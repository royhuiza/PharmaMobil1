package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobil.db.PharmaDb
import pe.edu.upeu.pharmamobil.domain.model.Producto

class ProductoLocal(private val db: PharmaDb) {

    private val consultas = db.productoQueries

    private fun convertir(
        id: Long,
        nombre: String,
        precio: Double,
        stock: Long,
        estado: Long,
        categoriaId: Long?
    ): Producto = Producto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock.toInt(),
        estado = estado == 1L,
        categoriaId = categoriaId
    )

    fun listar(): List<Producto> =
        consultas.listarTodos().executeAsList().map { fila ->
            convertir(
                fila.id,
                fila.nombre,
                fila.precio,
                fila.stock,
                fila.estado,
                fila.categoriaId
            )
        }

    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas ->
                filas.map { fila ->
                    convertir(
                        fila.id,
                        fila.nombre,
                        fila.precio,
                        fila.stock,
                        fila.estado,
                        fila.categoriaId
                    )
                }
            }

    fun guardar(producto: Producto, pendiente: Boolean = true) {
        consultas.insertar(
            id = producto.id,
            nombre = producto.nombre,
            precio = producto.precio,
            stock = producto.stock.toLong(),
            pendiente = if (pendiente) 1L else 0L,
            estado = if (producto.estado) 1L else 0L,
            categoriaId = producto.categoriaId
        )
    }

    fun listarPendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList().map { fila ->
            convertir(
                fila.id,
                fila.nombre,
                fila.precio,
                fila.stock,
                fila.estado,
                fila.categoriaId
            )
        }

    fun reemplazarPendiente(
        idLocal: Long,
        productoServidor: Producto
    ) {
        db.transaction {
            guardar(productoServidor, pendiente = false)
            consultas.borrarPorId(idLocal)
        }
    }

    fun actualizarProducto(producto: Producto) {
        require(producto.id != 0L) {
            "El producto debe tener un ID válido"
        }

        consultas.actualizarProducto(
            nombre = producto.nombre,
            precio = producto.precio,
            stock = producto.stock.toLong(),
            estado = if (producto.estado) 1L else 0L,
            categoriaId = producto.categoriaId,
            id = producto.id
        )
    }
    fun eliminarProductoLocal(id: Long) {
        require(id < 0L) {
            "Solo se pueden eliminar directamente productos no sincronizados"
        }

        consultas.borrarPorId(id)
    }

    fun actualizarCategoria(
        idProducto: Long,
        categoriaId: Long
    ) {
        require(categoriaId > 0L) {
            "La categoría debe ser válida"
        }

        consultas.actualizarCategoria(
            categoriaId = categoriaId,
            id = idProducto
        )
    }
}



