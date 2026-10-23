package pe.edu.upeu.pharmamobil.platform

import android.content.Context
import android.content.Intent

class CompartirProductoAndroid(
    private val context: Context
) : CompartirProducto {

    override fun compartir(texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }

        val chooser = Intent.createChooser(
            intent,
            "Compartir producto"
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooser)
    }

}