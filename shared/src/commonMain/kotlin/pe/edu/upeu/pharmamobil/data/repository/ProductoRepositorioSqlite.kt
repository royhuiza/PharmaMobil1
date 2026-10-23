package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.api.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.aModeloDominio
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioSqlite(
    private val local: ProductoLocal,
    private val api: ProductoApi
) : ProductoRepository {

    private val mutex = Mutex()

    override suspend fun registrar(producto: Producto): Producto =
        mutex.withLock {
            val menorId = local.listar()
                .map { it.id }
                .filter { it < 0L }
                .minOrNull() ?: 0L

            val nuevoProducto = producto.copy(id = menorId - 1L)

            local.guardar(nuevoProducto, pendiente = true)
            nuevoProducto
        }

    override suspend fun actualizar(producto: Producto): Producto =
        mutex.withLock {
            require(local.listar().any { it.id == producto.id }) {
                "Producto no encontrado"
            }

            local.actualizarProducto(producto)
            producto
        }

    override suspend fun eliminar(id: Long) {
        mutex.withLock {
            require(local.listar().any { it.id == id }) {
                "Producto no encontrado"
            }

            if (id < 0L) {
                local.eliminarProductoLocal(id)
            } else {
                error(
                    "La eliminación de productos sincronizados " +
                    "estará disponible al implementar la cola de sincronización"
                )
            }
        }
    }

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun sincronizar(): Int = mutex.withLock {
        var sincronizados = 0

        val pendientes = local.listarPendientes()

        for (producto in pendientes) {
            val categoriaId = producto.categoriaId
                ?.takeIf { it > 0L }
                ?: continue

            val respuesta = api.registrarProducto(
                ProductoRequestDto(
                    nombre = producto.nombre,
                    precio = producto.precio,
                    stock = producto.stock,
                    estado = producto.estado,
                    categoriaId = categoriaId
                )
            )

            local.reemplazarPendiente(
                idLocal = producto.id,
                productoServidor = respuesta.aModeloDominio()
            )

            sincronizados++
        }

        var pagina = 0
        var ultimaPagina = false

        while (!ultimaPagina) {
            val respuesta = api.listarProductos(
                pagina = pagina,
                tamanio = 20
            )

            for (producto in respuesta.contenido) {
                local.guardar(
                    producto = producto.aModeloDominio(),
                    pendiente = false
                )
            }

            ultimaPagina = respuesta.esUltima
            pagina++
        }

        sincronizados
    }
}


