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
        println("DEBUG_POST: Entrando a registrar()")

        val request = producto.aRequestDto(categoriaPorDefecto)

        println(
            "DEBUG_POST: DTO nombre=${request.nombre}, " +
                    "precio=${request.precio}, " +
                    "stock=${request.stock}, " +
                    "categoriaId=${request.categoriaId}"
        )

        return ejecutarLlamada {
            println("DEBUG_POST: Antes de api.crear()")

            val respuesta = api.crear(request)

            println("DEBUG_POST: POST completado. id=${respuesta.id}")

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