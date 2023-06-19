import java.time.LocalDate
import java.time.LocalTime

interface Evento{
    fun ejecutarEvento(tarea: Tarea)
}
class NotificacionWapp(val whatsApp:WhatsApp) : Evento{
    override fun ejecutarEvento(tarea: Tarea) {
        val message = DataMessage(
            numFrom = vito.number,
            numReceptor = vito.number,
            content = tarea.mensajeClave + " - "+tarea.dineroARecibir()
        )
        whatsApp.sendMessage(message)
    }
}
class IntervencionAFIP():Evento{
    val UMBRAL = 1000000
    override fun ejecutarEvento(tarea: Tarea) {
        if (tarea.bandaACargo?.dineroRecaudado!! > UMBRAL){
            AFIP(LocalDate.now(), LocalTime.now(), tarea.MOVIMIENTO, importe = tarea.dineroARecibir())
        }
    }

}

object vito{
    val number: Long = 1124003822
    var tareas = mutableListOf<Tarea>()
    var bandas = mutableListOf<Banda>()
    var capital :Double = 0.0
    val eventos = mutableListOf<Evento>()

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

    fun gastarDinero(dinero:Double){
        capital -= dinero
    }
    fun recibirDinero(dinero: Double) {
        capital += dinero
    }
    fun ejecutarTareasDelMes(){
        tareasMesActual().forEach { tarea ->
            tarea.ejecutar()
            eventos.forEach { it.ejecutarEvento(tarea) }
        }
    }
}
class Persona(
    var ingreso:Double,
    var nombre:String
){
    fun recibirDinero(dinero:Double) {
        ingreso += dinero
    }

    fun gastarDinero(dinero:Double) {
        ingreso -= dinero
    }
}
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
    fun recibirDinero(dinero:Double){
        dineroRecaudado += dinero
    }
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


val PROCENTAJE_COBRO_BANDA = 0.20
abstract class Tarea(numMes: Int,numAnio: Int,var personaInvolucrada : Persona, val mensajeClave:String) {
    val DIA_MES_PREDEF:Int = 5
    lateinit var fechaCreacion: LocalDate
    var bandaACargo : Banda? = null
    abstract val MOVIMIENTO : Int
    var tareaPendiente = true
    init {
        validarMes(numMes)
        fechaCreacion = LocalDate.of(numAnio,numMes, DIA_MES_PREDEF)
    }
    fun AsignarBanda(banda: Banda?) {
        bandaACargo = banda
    }
    fun asignarFecha(fecha:LocalDate){
        fechaCreacion = fecha
    }
    fun cobroBandaEstimado(): Double = PROCENTAJE_COBRO_BANDA * dineroARecibir()
    abstract fun dineroARecibir() : Double
    abstract fun ejecutar()

    //------------------------ Validaciones ------------------------
    fun validarMes(numMes: Int){
        if (!(1..12).contains(numMes)){
            throw BussinesExpetion("numero de mes invalido")
        }
    }
}
class RecolceccionDinero(
    numMes: Int,
    numAnio: Int,
    personaInvolucrada: Persona,
    mensajeClave: String)
    :
    Tarea(
    numMes,
    numAnio,
    personaInvolucrada,
    mensajeClave
){
    override val MOVIMIENTO = 1
    val PORCENTAJE = 0.1
    override fun dineroARecibir(): Double = PORCENTAJE * personaInvolucrada.ingreso

    override fun ejecutar() {
        val dineroBanda = cobroBandaEstimado()
        tareaPendiente = false
        bandaACargo?.recibirDinero(dineroBanda)
        vito.recibirDinero(dineroARecibir()-cobroBandaEstimado())
    }
}
class AbrirDeposito(numMes: Int, numAnio: Int, val dimDeposito: Double, personaInvolucrada: Persona,
                    mensajeClave: String
) : Tarea(numMes, numAnio,
    personaInvolucrada, mensajeClave
){
    override val MOVIMIENTO = 2
    val PRECIO_BASE_DEPO = 100.0
    override fun dineroARecibir(): Double = 0.0

    fun dineroAPagar():Double =  dimDeposito * PRECIO_BASE_DEPO
    override fun ejecutar() {
        tareaPendiente = false
        vito.gastarDinero(dineroAPagar())
    }
}
val CANT_CUOTAS = 4
class PrestarDinero(numMes: Int, numAnio: Int, val monto:Double, personaInvolucrada: Persona, mensajeClave: String):Tarea(numMes, numAnio,
    personaInvolucrada,
    mensajeClave
){
    override val MOVIMIENTO = 1
    override fun dineroARecibir(): Double = 0.0

    override fun ejecutar() {
        val MONTO_POR_MES = (monto * 2)/CANT_CUOTAS
        tareaPendiente = false
        vito.gastarDinero(monto)
        personaInvolucrada.recibirDinero(monto)
        repeat(CANT_CUOTAS){index -> vito.crearTarea(CobrarCuota(fechaCreacion.monthValue+index, fechaCreacion.year,  personaInvolucrada,MONTO_POR_MES,"Cuota N°$index"))}
    }

}
class CobrarCuota(numMes: Int, numAnio: Int, personaInvolucrada: Persona, val monto:Double, mensajeClave: String) : Tarea(numMes, numAnio, personaInvolucrada,
    mensajeClave
){
    override val MOVIMIENTO = 2
    override fun dineroARecibir(): Double = monto

    override fun ejecutar() {
        tareaPendiente = false
        bandaACargo!!.recibirDinero(cobroBandaEstimado())
        vito.recibirDinero(dineroARecibir() - cobroBandaEstimado())
    }
}
//cosas para el funcionamiento del codigo
interface WhatsApp{
    fun sendMessage(dataMessage:DataMessage)
}
data class DataMessage(
    val numFrom:Long,
    val numReceptor:Long,
    val content:String
)
data class AFIP(
    val fecha: LocalDate,
    val hora: LocalTime,
    val movimiento:Int,
    var concepto : String = "varios",
    val importe: Double
)
class BussinesExpetion(override val message: String) : Exception(message)