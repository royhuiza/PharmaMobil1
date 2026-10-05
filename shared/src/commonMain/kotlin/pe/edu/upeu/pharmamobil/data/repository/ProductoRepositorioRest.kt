package pe.edu.upeu.pharmamobil.data.repository

import pe.edu.upeu.pharmamobil.data.remote.api.ProductoApi
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobil.data.remote.dto.aModeloDominio
import pe.edu.upeu.pharmamobil.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository

class ProductoRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRepository {

    override suspend fun listar(): List<Producto> {
        return ejecutarLlamada {
            api.listarProductos()
                .contenido
                .filter { it.estado }
                .map { it.aModeloDominio() }
        }
    }

    override suspend fun obtener(id: Long): Producto {
        return ejecutarLlamada {
            api.obtener(id).aModeloDominio()
        }
    }

    override suspend fun registrar(producto: Producto): Producto {
        val request = producto.aRequestDto(categoriaPorDefecto)

        return ejecutarLlamada {
            val respuesta = api.crear(request)
            respuesta.aModeloDominio()
        }
    }

    override suspend fun actualizar(producto: Producto): Producto {
        return ejecutarLlamada {
            api.actualizar(
                producto.id,
                producto.aRequestDto(categoriaPorDefecto)
            ).aModeloDominio()
        }
    }

    override suspend fun eliminar(id: Long) {
        ejecutarLlamada {
            api.eliminar(id)
        }
    }
}

private fun Producto.aRequestDto(
    categoriaId: Long
): ProductoRequestDto {
    return ProductoRequestDto(
        nombre = nombre,
        precio = precio,
        stock = stock,
        estado = true,
        categoriaId = categoriaId
    )
}