package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobil.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobil.domain.platform.Compartidor

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val actualizarProducto: ActualizarProductoUseCase,
    private val eliminarProducto: EliminarProductoUseCase,
    private val compartidor: Compartidor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(fase = ProductoUiState.Fase.Cargando)
            }

            val fase = listarProductos().fold(
                onSuccess = { productos ->
                    if (productos.isEmpty()) {
                        ProductoUiState.Fase.SinProductos
                    } else {
                        ProductoUiState.Fase.ConProductos(
                            productos.map { it.aUi() }
                        )
                    }
                },
                onFailure = { fallo ->
                    ProductoUiState.Fase.Error(
                        mensajeError(fallo)
                    )
                }
            )

            _uiState.update {
                it.copy(fase = fase)
            }
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    nombre = nombre,
                    nombreError = null
                ),
                mensajeExito = null
            )
        }
    }

    fun onPrecioChange(precio: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    precio = precio,
                    precioError = null
                ),
                mensajeExito = null
            )
        }
    }

    fun onStockChange(stock: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    stock = stock,
                    stockError = null
                ),
                mensajeExito = null
            )
        }
    }

    fun registrar() {
        if (_uiState.value.procesando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    registrando = true,
                    mensajeExito = null
                )
            }

            val formulario = _uiState.value.formulario

            registrarProducto(
                nombre = formulario.nombre,
                precio = formulario.precio,
                stock = formulario.stock
            ).fold(
                onSuccess = { producto ->
                    _uiState.update {
                        it.copy(
                            registrando = false,
                            formulario = FormularioProducto(),
                            mensajeExito =
                                "Producto \"${producto.nombre}\" registrado correctamente"
                        )
                    }

                    cargarProductos()
                },
                onFailure = { fallo ->
                    manejarErrorFormulario(
                        fallo = fallo,
                        registrando = false
                    )
                }
            )
        }
    }

    fun editar(producto: ProductoUi) {
        if (_uiState.value.procesando) return

        _uiState.update {
            it.copy(
                productoEditandoId = producto.id,
                formulario = FormularioProducto(
                    nombre = producto.nombre,
                    precio = producto.precio
                        .removePrefix("S/ ")
                        .trim(),
                    stock = producto.stock
                        .removeSuffix(" u.")
                        .trim()
                ),
                mensajeExito = null
            )
        }
    }

    fun cancelarEdicion() {
        if (_uiState.value.procesando) return

        _uiState.update {
            it.copy(
                productoEditandoId = null,
                formulario = FormularioProducto(),
                mensajeExito = null
            )
        }
    }

    fun actualizar() {
        val id = _uiState.value.productoEditandoId ?: return

        if (_uiState.value.procesando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    actualizando = true,
                    mensajeExito = null
                )
            }

            val formulario = _uiState.value.formulario

            actualizarProducto(
                id = id,
                nombre = formulario.nombre,
                precio = formulario.precio,
                stock = formulario.stock
            ).fold(
                onSuccess = { producto ->
                    _uiState.update {
                        it.copy(
                            actualizando = false,
                            productoEditandoId = null,
                            formulario = FormularioProducto(),
                            mensajeExito =
                                "Producto \"${producto.nombre}\" actualizado correctamente"
                        )
                    }

                    cargarProductos()
                },
                onFailure = { fallo ->
                    manejarErrorFormulario(
                        fallo = fallo,
                        actualizando = false
                    )
                }
            )
        }
    }

    fun eliminar(producto: ProductoUi) {
        if (_uiState.value.procesando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    eliminandoId = producto.id,
                    mensajeExito = null
                )
            }

            eliminarProducto(producto.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            eliminandoId = null,
                            mensajeExito =
                                "Producto \"${producto.nombre}\" eliminado correctamente"
                        )
                    }

                    cargarProductos()
                },
                onFailure = { fallo ->
                    _uiState.update {
                        it.copy(
                            eliminandoId = null,
                            fase = ProductoUiState.Fase.Error(
                                mensajeError(fallo)
                            )
                        )
                    }
                }
            )
        }
    }

    fun compartir(producto: ProductoUi) {
        val texto = "${producto.nombre} — ${producto.precio} · Stock: ${producto.stock}"
        compartidor.compartir(texto)
    }
    private fun manejarErrorFormulario(
        fallo: Throwable,
        registrando: Boolean = _uiState.value.registrando,
        actualizando: Boolean = _uiState.value.actualizando
    ) {
        when (fallo) {

            is ProductoInvalidoException -> {
                _uiState.update {
                    it.copy(
                        registrando = registrando,
                        actualizando = actualizando,
                        formulario = it.formulario.copy(
                            nombreError = fallo.errores.nombre,
                            precioError = fallo.errores.precio,
                            stockError = fallo.errores.stock
                        )
                    )
                }
            }

            is ErrorApiException -> {
                when (val error = fallo.error) {

                    is ErrorApi.Validacion -> {
                        _uiState.update {
                            it.copy(
                                registrando = registrando,
                                actualizando = actualizando,
                                formulario = it.formulario.copy(
                                    nombreError = error.campos["nombre"],
                                    precioError = error.campos["precio"],
                                    stockError = error.campos["stock"]
                                )
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                registrando = registrando,
                                actualizando = actualizando,
                                fase = ProductoUiState.Fase.Error(
                                    mensajeError(fallo)
                                )
                            )
                        }
                    }
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        registrando = registrando,
                        actualizando = actualizando,
                        fase = ProductoUiState.Fase.Error(
                            fallo.message ?: "Ocurrió un error inesperado"
                        )
                    )
                }
            }
        }
    }

    private fun mensajeError(fallo: Throwable): String {
        return when (fallo) {

            is ErrorApiException -> {
                when (fallo.error) {
                    is ErrorApi.Validacion ->
                        "Los datos enviados no son válidos"

                    ErrorApi.NoEncontrado ->
                        "El producto no fue encontrado"

                    ErrorApi.Conflicto ->
                        "La operación genera un conflicto"

                    ErrorApi.Servidor ->
                        "Ocurrió un error en el servidor"

                    ErrorApi.SinConexion ->
                        "No se pudo conectar con el servidor"

                    ErrorApi.TiempoAgotado ->
                        "La conexión tardó demasiado"
                }
            }

            else ->
                fallo.message ?: "Ocurrió un error inesperado"
        }
    }
}