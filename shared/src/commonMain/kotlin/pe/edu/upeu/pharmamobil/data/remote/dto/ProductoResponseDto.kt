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

fun ProductoResponseDto.aModeloDominio(): Producto {
    return Producto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock
    )
}

fun Producto.aResponseDto(): ProductoResponseDto {
    return ProductoResponseDto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock
    )
}
