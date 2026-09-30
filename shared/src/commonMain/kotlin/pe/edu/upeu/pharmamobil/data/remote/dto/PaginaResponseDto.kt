package pe.edu.upeu.pharmamobil.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginaResponseDto<T>(
    val contenido: List<T> = emptyList(),
    val pagina: Int = 0,
    val tamanio: Int = 20,
    val totalElementos: Long = 0L,
    val totalPaginas: Int = 0,
    val esUltima: Boolean = true
)
