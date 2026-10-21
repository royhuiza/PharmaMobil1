package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobil.domain.model.Producto

@Serializable
data class ProductoResponseDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null
)

fun ProductoResponseDto.aModeloDominio(): Producto =
    Producto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock,
        estado = estado,
        categoriaId = categoriaId
    )

fun Producto.aResponseDto(): ProductoResponseDto =
    ProductoResponseDto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock,
        estado = estado,
        categoriaId = categoriaId
    )
