package pe.edu.upeu.pharmamobil.presentation.producto

data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val formulario: FormularioProducto = FormularioProducto(),

    // null = estamos registrando un producto nuevo
    // distinto de null = estamos editando ese producto
    val productoEditandoId: Long? = null,

    val registrando: Boolean = false,
    val actualizando: Boolean = false,
    val eliminandoId: Long? = null,

    val mensajeExito: String? = null
) {

    val editando: Boolean
        get() = productoEditandoId != null

    val procesando: Boolean
        get() = registrando || actualizando || eliminandoId != null

    sealed interface Fase {

        data object Cargando : Fase

        data object SinProductos : Fase

        data class ConProductos(
            val productos: List<ProductoUi>
        ) : Fase

        data class Error(
            val mensaje: String
        ) : Fase
    }
}

data class FormularioProducto(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",

    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null
)