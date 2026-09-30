package pe.edu.upeu.pharmamobil.data.dto

import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobil.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobil.data.remote.dto.aModeloDominio
import pe.edu.upeu.pharmamobil.data.remote.dto.aResponseDto
import pe.edu.upeu.pharmamobil.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoResponseDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun deserializaJsonACorrectamente() {
        val rawJson = """
            {
                "id": 1,
                "nombre": "Paracetamol",
                "precio": 5.5,
                "stock": 100
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductoResponseDto>(rawJson)

        assertEquals(1L, dto.id)
        assertEquals("Paracetamol", dto.nombre)
        assertEquals(5.5, dto.precio)
        assertEquals(100, dto.stock)
        assertTrue(dto.estado)
    }

    @Test
    fun mapeaAModeloDeDominioCorrectamente() {
        val dto = ProductoResponseDto(
            id = 1L,
            nombre = "Paracetamol",
            precio = 5.5,
            stock = 100
        )

        val producto = dto.aModeloDominio()

        assertEquals(1L, producto.id)
        assertEquals("Paracetamol", producto.nombre)
        assertEquals(5.5, producto.precio)
        assertEquals(100, producto.stock)
    }

    @Test
    fun mapeaDesdeModeloDeDominioCorrectamente() {
        val producto = Producto(
            id = 2L,
            nombre = "Ibuprofeno",
            precio = 12.0,
            stock = 50
        )

        val dto = producto.aResponseDto()

        assertEquals(2L, dto.id)
        assertEquals("Ibuprofeno", dto.nombre)
        assertEquals(12.0, dto.precio)
        assertEquals(50, dto.stock)
    }
}
