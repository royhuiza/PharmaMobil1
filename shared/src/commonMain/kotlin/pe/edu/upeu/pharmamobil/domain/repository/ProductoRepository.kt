package pe.edu.upeu.pharmamobil.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobil.domain.model.Producto

interface ProductoRepository {

    suspend fun registrar(producto: Producto): Producto

    suspend fun actualizar(producto: Producto): Producto

    suspend fun eliminar(id: Long)

    suspend fun listar(): List<Producto>

    fun observar(): Flow<List<Producto>>

    suspend fun sincronizar(): Int
}

