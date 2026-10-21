package pe.edu.upeu.pharmamobil.data.remote

class SesionAuth {
    private var tokenActual: String? = null

    fun guardarToken(token: String) {
        require(token.isNotBlank()) { "El token no puede estar vacío" }
        tokenActual = token
    }

    fun obtenerToken(): String? = tokenActual

    fun cerrarSesion() {
        tokenActual = null
    }
}
