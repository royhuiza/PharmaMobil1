package pe.edu.upeu.pharmamobil.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobil.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobil.presentation.components.MensajeExito
import pe.edu.upeu.pharmamobil.presentation.components.ValidatedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var mensajeCompartido by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        FormularioProductoCard(
            formulario = uiState.formulario,
            registrando = uiState.registrando || uiState.actualizando,
            editando = uiState.productoEditandoId != null,
            onCancelarEdicion = viewModel::cancelarEdicion,
            categorias = uiState.categorias,
            cargandoCategorias = uiState.cargandoCategorias,
            errorCategorias = uiState.errorCategorias,
            onCategoriaChange = viewModel::onCategoriaChange,
            onRecargarCategorias = viewModel::cargarCategorias,
            onNombreChange = viewModel::onNombreChange,
            onPrecioChange = viewModel::onPrecioChange,
            onStockChange = viewModel::onStockChange,
            onRegistrar = {
                if (uiState.productoEditandoId != null) viewModel.actualizar()
                else viewModel.registrar()
            }
        )

        uiState.mensajeExito?.let { mensaje ->
            MensajeExito(mensaje)
        }

        uiState.mensajeError?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        mensajeCompartido?.let { mensaje ->
            Text(mensaje, color = MaterialTheme.colorScheme.primary)
        }

        EncabezadoInventario(uiState.fase)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (val fase = uiState.fase) {

                ProductoUiState.Fase.Cargando -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()

                        Text(
                            text = "Cargando inventario...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ProductoUiState.Fase.SinProductos -> {
                    EstadoVacio(
                        icono = Icons.Default.Inventory2,
                        titulo = "Todavía no hay productos",
                        descripcion = "Registra el primero con el formulario de arriba.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProductoUiState.Fase.ConProductos -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = fase.productos,
                            key = { it.id }
                        ) { producto ->
                            ProductoItem(
                                producto = producto,
                                onCompartir = {
                                    viewModel.compartir(producto)
                                },
                                onEditar = {
                                    viewModel.editarPorId(producto.id)
                                },
                                onEliminar = {
                                    viewModel.solicitarEliminar(producto.id)
                                }
                            )
                        }
                    }
                }

                is ProductoUiState.Fase.Error -> {
                    EstadoVacio(
                        icono = Icons.Default.CloudOff,
                        titulo = "No pudimos cargar el inventario",
                        descripcion = fase.mensaje,
                        colorIcono = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center),
                        accion = {
                            FilledTonalButton(
                                onClick = viewModel::cargarProductos
                            ) {
                                Text("Reintentar")
                            }
                        }
                    )
                }
            }
        }
    }

    if (uiState.productoPendienteEliminarId != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminar,
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que deseas eliminar este producto? Los productos sincronizados requieren eliminación en el servidor.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarEliminar) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEliminar) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FormularioProductoCard(
    formulario: FormularioProducto,
    registrando: Boolean,
    editando: Boolean,
    onCancelarEdicion: () -> Unit,
    categorias: List<CategoriaUi>,
    cargandoCategorias: Boolean,
    errorCategorias: String?,
    onCategoriaChange: (Long) -> Unit,
    onRecargarCategorias: () -> Unit,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onRegistrar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (editando) "Editar producto" else "Registrar producto",
                style = MaterialTheme.typography.titleMedium
            )

            ValidatedTextField(
                value = formulario.nombre,
                onValueChange = onNombreChange,
                label = "Nombre",
                error = formulario.nombreError,
                leadingIcon = Icons.Default.Medication,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ValidatedTextField(
                    value = formulario.precio,
                    onValueChange = onPrecioChange,
                    label = "Precio",
                    error = formulario.precioError,
                    ayuda = "En soles",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )

                ValidatedTextField(
                    value = formulario.stock,
                    onValueChange = onStockChange,
                    label = "Stock",
                    error = formulario.stockError,
                    ayuda = "Unidades",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            SelectorCategoria(
                categorias = categorias,
                categoriaSeleccionadaId = formulario.categoriaId,
                cargando = cargandoCategorias,
                error = errorCategorias,
                categoriaError = formulario.categoriaError,
                onCategoriaChange = onCategoriaChange,
                onRecargar = onRecargarCategorias
            )

            Button(
                onClick = onRegistrar,
                enabled = !registrando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        registrando && editando -> "Guardando..."
                        registrando -> "Registrando..."
                        editando -> "Guardar cambios"
                        else -> "Registrar"
                    }
                )
            }
            if (editando) {
                TextButton(onClick = onCancelarEdicion, enabled = !registrando) {
                    Text("Cancelar edición")
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun SelectorCategoria(
    categorias: List<CategoriaUi>,
    categoriaSeleccionadaId: Long?,
    cargando: Boolean,
    error: String?,
    categoriaError: String?,
    onCategoriaChange: (Long) -> Unit,
    onRecargar: () -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    val categoriaSeleccionada = categorias.firstOrNull {
        it.id == categoriaSeleccionadaId
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expandido && categorias.isNotEmpty(),
            onExpandedChange = {
                if (!cargando && categorias.isNotEmpty()) {
                    expandido = !expandido
                }
            }
        ) {
            OutlinedTextField(
                value = categoriaSeleccionada?.nombre.orEmpty(),
                onValueChange = {},
                readOnly = true,
                enabled = !cargando && categorias.isNotEmpty(),
                label = { Text("Categoría") },
                placeholder = {
                    Text(
                        if (cargando) {
                            "Cargando categorías..."
                        } else {
                            "Seleccionar categoría"
                        }
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expandido
                    )
                },
                isError = categoriaError != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandido && categorias.isNotEmpty(),
                onDismissRequest = {
                    expandido = false
                }
            ) {
                categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = {
                            Text(categoria.nombre)
                        },
                        onClick = {
                            onCategoriaChange(categoria.id)
                            expandido = false
                        }
                    )
                }
            }
        }

        if (cargando) {
            Text(
                text = "Consultando categorías...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (categoriaError != null) {
            Text(
                text = categoriaError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (error != null) {
            Text(
                text = "No se pudieron cargar las categorías: $error",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (!cargando && categorias.isEmpty()) {
            Text(
                text = "No hay categorías disponibles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FilledTonalButton(
                onClick = onRecargar
            ) {
                Text("Reintentar categorías")
            }
        }
    }
}

@Composable
private fun EncabezadoInventario(
    fase: ProductoUiState.Fase
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Inventario",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        if (fase is ProductoUiState.Fase.ConProductos) {
            val cantidad = fase.productos.size

            Text(
                text = if (cantidad == 1) {
                    "1 producto"
                } else {
                    "$cantidad productos"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProductoItem(
    producto: ProductoUi,
    onCompartir: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(producto.nombre, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${producto.precio} · ${producto.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (producto.requiereReposicion) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(
                            "Reponer",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCompartir) { Text("Compartir") }
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onEliminar) { Text("Eliminar") }
            }
        }
    }
}
