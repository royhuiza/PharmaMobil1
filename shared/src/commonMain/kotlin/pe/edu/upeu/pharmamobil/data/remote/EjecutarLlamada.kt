package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobil.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobil.domain.error.ErrorApi
import pe.edu.upeu.pharmamobil.domain.error.ErrorApiException

suspend fun <T> ejecutarLlamada(
    bloque: suspend () -> T
): T {
    try {
        return bloque()

    } catch (e: CancellationException) {
        throw e

    } catch (e: ClientRequestException) {

        val errorDto = runCatching {
            e.response.body<ErrorResponseDto>()
        }.getOrNull()

        val error = when (e.response.status.value) {

            400 -> ErrorApi.Validacion(
                campos = errorDto?.validationErrors.orEmpty()
            )

            404 -> ErrorApi.NoEncontrado

            409 -> ErrorApi.Conflicto

            else -> ErrorApi.Servidor
        }

        throw ErrorApiException(error)

    } catch (e: ServerResponseException) {

        throw ErrorApiException(
            ErrorApi.Servidor
        )

    } catch (e: HttpRequestTimeoutException) {

        throw ErrorApiException(
            ErrorApi.TiempoAgotado
        )

    } catch (e: Exception) {

        throw ErrorApiException(ErrorApi.SinConexion)
    }
}