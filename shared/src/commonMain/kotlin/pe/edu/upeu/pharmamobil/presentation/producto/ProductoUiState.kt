package pe.edu.upeu.pharmamobil.presentation.producto

data class ProductoUiState(
    val fase: Fase = Fase.Cargando,
    val formulario: FormularioProducto = FormularioProducto(),
    val registrando: Boolean = false,
    val mensajeExito: String? = null,
    val categorias: List<CategoriaUi> = emptyList(),
    val cargandoCategorias: Boolean = false,
    val errorCategorias: String? = null
) {

    /** Fases excluyentes del inventario: solo una puede estar activa. */
    sealed interface Fase {
        data object Cargando : Fase
        data object SinProductos : Fase
        data class ConProductos(val productos: List<ProductoUi>) : Fase
        data class Error(val mensaje: String) : Fase
    }
}

data class CategoriaUi(
    val id: Long,
    val nombre: String
)

data class FormularioProducto(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val categoriaId: Long? = null,
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val categoriaError: String? = null
)
