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
import pe.edu.upeu.pharmamobil.domain.model.Producto
import pe.edu.upeu.pharmamobil.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobil.platform.CompartirProducto



class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase,
    private val categoriaApi: CategoriaApi,
    private val productoRepository: ProductoRepository,
    private val compartirProducto: CompartirProducto
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
                stock = formulario.stock, categoriaId = formulario.categoriaId
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

    fun editar(producto: Producto) {
        _uiState.update {
            it.copy(
                productoEditandoId = producto.id,
                formulario = FormularioProducto(
                    nombre = producto.nombre,
                    precio = producto.precio.toString(),
                    stock = producto.stock.toString(),
                    categoriaId = producto.categoriaId
                ),
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update {
            it.copy(
                productoEditandoId = null,
                formulario = FormularioProducto(),
                mensajeError = null
            )
        }
    }

    fun actualizar() {
        val estado = _uiState.value
        val id = estado.productoEditandoId ?: return

        if (estado.actualizando) return

        val formulario = estado.formulario
        val nombre = formulario.nombre.trim()
        val precio = formulario.precio.toDoubleOrNull()
        val stock = formulario.stock.toIntOrNull()

        if (nombre.isBlank() || precio == null || precio <= 0.0 ||
            !precio.isFinite() || stock == null || stock < 0
        ) {
            _uiState.update {
                it.copy(
                    mensajeError = "Verifica el nombre, precio y stock."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actualizando = true,
                    mensajeError = null
                )
            }

            try {
                val original = productoRepository.listar()
                    .firstOrNull { it.id == id }
                    ?: error("Producto no encontrado")

                productoRepository.actualizar(
                    original.copy(
                        nombre = nombre,
                        precio = precio,
                        stock = stock,
                        categoriaId = formulario.categoriaId
                    )
                )

                _uiState.update {
                    it.copy(
                        actualizando = false,
                        productoEditandoId = null,
                        formulario = FormularioProducto(),
                        mensajeExito = "Producto actualizado correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        actualizando = false,
                        mensajeError = e.message
                            ?: "No se pudo actualizar el producto"
                    )
                }
            }
        }
    }

    fun solicitarEliminar(id: Long) {
        _uiState.update {
            it.copy(
                productoPendienteEliminarId = id,
                mensajeError = null
            )
        }
    }

    fun cancelarEliminar() {
        _uiState.update {
            it.copy(productoPendienteEliminarId = null)
        }
    }

    fun confirmarEliminar() {
        val id = _uiState.value.productoPendienteEliminarId ?: return

        if (_uiState.value.eliminandoId != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    eliminandoId = id,
                    productoPendienteEliminarId = null,
                    mensajeError = null
                )
            }

            try {
                productoRepository.eliminar(id)

                _uiState.update {
                    it.copy(
                        eliminandoId = null,
                        mensajeExito = "Producto eliminado correctamente"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        eliminandoId = null,
                        mensajeError = e.message
                            ?: "No se pudo eliminar el producto"
                    )
                }
            }
        }
    }
    fun editarPorId(id: Long) {
        viewModelScope.launch {
            try {
                val producto = productoRepository.listar()
                    .firstOrNull { it.id == id }
                    ?: error("Producto no encontrado")

                editar(producto)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        mensajeError = e.message
                            ?: "No se pudo abrir el producto"
                    )
                }
            }
        }
    }

    fun compartir(producto: ProductoUi) {
        val texto = buildString {
            appendLine("Producto: ${producto.nombre}")
            appendLine("Precio: ${producto.precio}")
            append("Stock: ${producto.stock}")
        }

        compartirProducto.compartir(texto)
    }

}





