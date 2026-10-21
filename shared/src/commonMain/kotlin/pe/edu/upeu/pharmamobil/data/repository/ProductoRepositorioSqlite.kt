package pe.edu.upeu.pharmamobil.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobil.data.local.ProductoLocal
import pe.edu.upeu.pharmamobil.data.remote.api.ProductoApi
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

    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> =
        local.listar()

    override suspend fun sincronizar(): Int {
        // Pendiente: enviar productos a PharmaSoft.
        return 0
    }
}



