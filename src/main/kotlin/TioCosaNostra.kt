import java.time.LocalDate

object Vito{
    var tareas = mutableListOf<Tarea>()
    var bandas = mutableListOf<Banda>()

    fun tareasMesActual(): List<Tarea> =
        tareas.filter { (it.fechaCreacion.month == LocalDate.now().month) && (it.fechaCreacion.year == LocalDate.now().year)}
    fun crearTarea(tarea: Tarea){
        tareas.add(tarea)
    }
    fun asignarTareas(){
        tareasMesActual().forEach { tarea -> tarea.AsignarBanda(bandas.find { it.puedeCumplir(tarea) })}
    }
    fun agregarIntegrante(banda: Banda, integrante: Integrante){
        banda.integrantes.add(integrante)
    }
    fun quitarIntegrante(banda: Banda, integrante: Integrante){
        banda.integrantes.remove(integrante)
    }
}
data class Persona(
    var nombre:String
)
class Integrante(var personalidad : Personalidad){

    fun cambiarPersonalidad(personalidadNueva: Personalidad) {personalidad = personalidadNueva}
    fun puedeHacerTarea(tarea: Tarea) = personalidad.puedeHacerTarea(tarea)
}
interface Personalidad {
    fun puedeHacerTarea(tarea: Tarea): Boolean
}
class AltoPerfil(): Personalidad{
    override fun puedeHacerTarea(tarea: Tarea) = tarea.cobroBandaEstimado() > 1000.0
}
class Cabulero() : Personalidad{
    val letraIndesesada = "X"
    override fun puedeHacerTarea(tarea: Tarea) = !tarea.personaInvolucrada.nombre.contains(letraIndesesada)
}
class Combinada():Personalidad{
    val personalidades = mutableSetOf<Personalidad>()
    override fun puedeHacerTarea(tarea: Tarea) = personalidades.all{it.puedeHacerTarea(tarea)}

}
//TODO: crear el resto de personalidades

abstract class Banda(var lider: Integrante) {
    var dineroRecaudado : Double = 0.0
    var integrantes = mutableListOf<Integrante>()
    fun puedeCumplir(tarea: Tarea): Boolean =
        (dineroRecaudado > 0) && condicionBanda(tarea)

    abstract fun condicionBanda(tarea: Tarea): Boolean
}
class Forajida(lider: Integrante) : Banda(lider) {
    override fun condicionBanda(tarea: Tarea): Boolean = integrantes.any { it.puedeHacerTarea(tarea) }
}
class Sorora(lider: Integrante) : Banda(lider) {
    override fun condicionBanda(tarea: Tarea): Boolean = integrantes.all { it.puedeHacerTarea(tarea) }

}
class Tipica(lider: Integrante) :Banda(lider){
    override fun condicionBanda(tarea: Tarea): Boolean = lider.puedeHacerTarea(tarea)

}



class Tarea(val fechaCreacion: LocalDate) {
    var bandaACargo : Banda? = null
    lateinit var personaInvolucrada : Persona
    fun AsignarBanda(banda: Banda?) {
        bandaACargo = banda
    }

    fun cobroBandaEstimado(): Double = //TODO
}
