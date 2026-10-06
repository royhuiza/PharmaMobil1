package pe.edu.upeu.pharmamobil.domain.platform

import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.platform.formatearSoles

interface Compartidor {
    fun compartir(texto: String)
}

fun Producto.comoTextoParaCompartir(): String =
    "$nombre — ${formatearSoles(precio)} · Stock: $stock"