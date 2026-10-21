package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.lifecycle.ViewModel
import pe.edu.upeu.pharmamobil.data.remote.api.CategoriaApi
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobil.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobil.domain.usecase.ProductoInvalidoException
import pe.edu.upeu.pharmamobil.domain.usecase.RegistrarProductoUseCase


class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val categoriaApi: CategoriaApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        observarProductos()
        cargarCategorias()
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(cargandoCategorias = true, errorCategorias = null)
            }

            try {
                val categorias = categoriaApi.listarCategorias()
                    .filter { it.estado }
                    .map { CategoriaUi(id = it.id, nombre = it.nombre) }

                _uiState.update {
                    it.copy(
                        categorias = categorias,
                        cargandoCategorias = false
                    )
                }
            } catch (fallo: Exception) {
                _uiState.update {
                    it.copy(
                        cargandoCategorias = false,
                        errorCategorias = fallo.message
                            ?: "No se pudieron cargar las categorias"
                    )
                }
            }
        }
    }

    fun onCategoriaChange(categoriaId: Long) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(
                    categoriaId = categoriaId,
                    categoriaError = null
                ),
                mensajeExito = null
            )
        }
    }
    fun cargarProductos() {
        // Flow ya observa SQLite automáticamente.
        // Este método conserva la compatibilidad con el botón Reintentar.
    }

    private fun observarProductos() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(fase = ProductoUiState.Fase.Cargando)
            }

            listarProductos.observar()
                .catch { fallo ->
                    _uiState.update {
                        it.copy(
                            fase = ProductoUiState.Fase.Error(
                                fallo.message ?: "No se pudo observar el inventario"
                            )
                        )
                    }
                }
                .collect { productos ->
                    val fase = if (productos.isEmpty()) {
                        ProductoUiState.Fase.SinProductos
                    } else {
                        ProductoUiState.Fase.ConProductos(productos.map { it.aUi() })
                    }

                    _uiState.update {
                        it.copy(fase = fase)
                    }
                }
        }
    }

    fun onNombreChange(nombre: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(nombre = nombre, nombreError = null),
                mensajeExito = null
            )
        }
    }

    fun onPrecioChange(precio: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(precio = precio, precioError = null),
                mensajeExito = null
            )
        }
    }

    fun onStockChange(stock: String) {
        _uiState.update {
            it.copy(
                formulario = it.formulario.copy(stock = stock, stockError = null),
                mensajeExito = null
            )
        }
    }

    fun registrar() {

        if (_uiState.value.registrando) return

        viewModelScope.launch {

            _uiState.update {
                it.copy(registrando = true, mensajeExito = null)
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
                            mensajeExito = "Producto \"${producto.nombre}\" registrado correctamente"
                        )
                    }
                },
                onFailure = { fallo ->
                    when (fallo) {

                        is ProductoInvalidoException -> _uiState.update {
                            it.copy(
                                registrando = false,
                                formulario = it.formulario.copy(
                                    nombreError = fallo.errores.nombre,
                                    precioError = fallo.errores.precio,
                                    stockError = fallo.errores.stock
                                )
                            )
                        }

                        else -> _uiState.update {
                            it.copy(
                                registrando = false,
                                fase = ProductoUiState.Fase.Error(
                                    fallo.message ?: "No se pudo registrar el producto"
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}




