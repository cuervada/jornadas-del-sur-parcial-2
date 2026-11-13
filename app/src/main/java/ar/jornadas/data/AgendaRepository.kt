
package ar.jornadas.data

class AgendaRepository(
    private val api: ProgramaApi,
    private val dao: CarteleraDao
) {

    val programa = dao.observar()

    suspend fun sincronizar() {

        val fichas = api.consultar().map {
            Charla(
                id = it.id,
                titulo = it.title,
                tema = it.track,
                dia = it.day,
                hora = it.time,
                sala = it.room,
                ponente = it.speaker,
                resumen = it.summary
            )
        }

        dao.aplicarPrograma(fichas)
    }

    suspend fun cambiarFavorita(id: String) {
        dao.alternar(id)
    }
}
