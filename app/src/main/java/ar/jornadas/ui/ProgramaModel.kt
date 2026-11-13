
package ar.jornadas.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ar.jornadas.Servicios
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ProgramaModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private val cargaAutomaticaIniciada = AtomicBoolean(false)
    }

    private val servicios = Servicios(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda

    private val preferencias = app.getSharedPreferences(
        "estado_programa",
        Context.MODE_PRIVATE
    )

    val charlas = repo.programa.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val dia = MutableStateFlow(0)
    val aviso = MutableStateFlow("")
    val cargando = MutableStateFlow(false)

    val hayProgramaGuardado = MutableStateFlow(
        preferencias.getBoolean("descarga_exitosa", false)
    )

    init {
        if (cargaAutomaticaIniciada.compareAndSet(false, true)) {
            recargar()
        }
    }

    fun recargar() {

        if (cargando.value) return

        cargando.value = true
        aviso.value = ""

        viewModelScope.launch {
            try {
                repo.sincronizar()

                preferencias.edit()
                    .putBoolean("descarga_exitosa", true)
                    .apply()

                hayProgramaGuardado.value = true

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                aviso.value = if (hayProgramaGuardado.value) {
                    "Sin conexión. Se están utilizando datos guardados."
                } else {
                    "No se pudo consultar el programa. Presioná Reintentar."
                }

            } finally {
                cargando.value = false
            }
        }
    }

    fun marcar(id: String) {
        viewModelScope.launch {
            repo.cambiarFavorita(id)
        }
    }
}
