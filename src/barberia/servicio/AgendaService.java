package barberia.servicio;

import barberia.excepciones.DatoInvalidoException;
import barberia.excepciones.OperacionNoPermitidaException;
import barberia.modelo.Barbero;
import barberia.modelo.Cita;
import barberia.modelo.Cliente;
import barberia.modelo.EstadoCita;
import barberia.modelo.Servicio;
import barberia.repositorio.CitaRepositorio;
import barberia.util.Validador;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de la agenda:
 * REQ-CIT-01 (agendar), REQ-CIT-02 (sin cruces), REQ-CIT-03 (horarios disponibles)
 * y REQ-CIT-06 (marcar atendida o inasistencia).
 */
public class AgendaService {

    /** Los horarios disponibles se ofrecen cada 30 minutos. */
    private static final int PASO_MINUTOS = 30;

    private final CitaRepositorio citas;
    private final ClienteService clientes;
    private final BarberoService barberos;
    private final ServicioService servicios;
    private final Clock reloj;

    public AgendaService(CitaRepositorio citas, ClienteService clientes, BarberoService barberos,
                         ServicioService servicios, Clock reloj) {
        this.citas = citas;
        this.clientes = clientes;
        this.barberos = barberos;
        this.servicios = servicios;
        this.reloj = reloj;
    }

    /** REQ-CIT-01 y REQ-CIT-02. */
    public Cita agendar(String codigoCliente, String codigoBarbero, String codigoServicio,
                        LocalDate fecha, LocalTime hora)
            throws DatoInvalidoException, OperacionNoPermitidaException {
        Validador.validarObligatorio(codigoCliente, "cliente");
        Validador.validarObligatorio(codigoBarbero, "barbero");
        Validador.validarObligatorio(codigoServicio, "servicio");
        if (fecha == null) {
            throw new DatoInvalidoException("Falta indicar el dato: fecha");
        }
        if (hora == null) {
            throw new DatoInvalidoException("Falta indicar el dato: hora");
        }

        Cliente cliente = clientes.obtenerActivo(codigoCliente);
        Barbero barbero = barberos.obtenerActivo(codigoBarbero);
        Servicio servicio = servicios.obtenerActivo(codigoServicio);

        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now(reloj))) {
            throw new DatoInvalidoException("No se puede agendar en una fecha pasada");
        }

        Cita candidata = new Cita("TEMP", cliente, barbero, servicio, fecha, hora);
        for (Cita existente : citasQueOcupanHorario(barbero.getCodigo(), fecha)) {
            if (candidata.seCruzaCon(existente)) {
                throw new OperacionNoPermitidaException("El barbero ya tiene una cita en ese horario");
            }
        }

        Cita nueva = new Cita(citas.siguienteCodigo(), cliente, barbero, servicio, fecha, hora);
        citas.guardar(nueva);
        return nueva;
    }

    /** REQ-CIT-03: horas de inicio libres de un barbero en una fecha, según la duración del servicio. */
    public List<LocalTime> horariosDisponibles(String codigoBarbero, LocalDate fecha, String codigoServicio)
            throws DatoInvalidoException {
        Validador.validarObligatorio(codigoBarbero, "barbero");
        Validador.validarObligatorio(codigoServicio, "servicio");
        if (fecha == null) {
            throw new DatoInvalidoException("Falta indicar el dato: fecha");
        }
        if (fecha.isBefore(LocalDate.now(reloj))) {
            throw new DatoInvalidoException("No se pueden consultar horarios de fechas pasadas");
        }

        Barbero barbero = barberos.obtenerActivo(codigoBarbero);
        Servicio servicio = servicios.obtenerActivo(codigoServicio);
        int duracion = servicio.getDuracion();
        List<Cita> ocupadas = citasQueOcupanHorario(barbero.getCodigo(), fecha);
        LocalDateTime ahora = LocalDateTime.now(reloj);

        List<LocalTime> libres = new ArrayList<>();
        int desde = barbero.getHoraInicio().toSecondOfDay() / 60;
        int hasta = barbero.getHoraFin().toSecondOfDay() / 60;
        for (int minuto = desde; minuto + duracion <= hasta; minuto += PASO_MINUTOS) {
            LocalTime hora = LocalTime.of(minuto / 60, minuto % 60);
            LocalDateTime inicio = LocalDateTime.of(fecha, hora);
            LocalDateTime fin = inicio.plusMinutes(duracion);
            if (inicio.isBefore(ahora)) {
                continue;
            }
            boolean libre = true;
            for (Cita c : ocupadas) {
                if (inicio.isBefore(c.getFin()) && c.getInicio().isBefore(fin)) {
                    libre = false;
                    break;
                }
            }
            if (libre) {
                libres.add(hora);
            }
        }
        return libres;
    }

    /** REQ-CIT-06: el cliente asistió y fue atendido. */
    public Cita marcarAtendida(String codigoCita) throws DatoInvalidoException, OperacionNoPermitidaException {
        Cita cita = obtenerPendienteDeHoyOAnterior(codigoCita);
        cita.marcarAtendida();
        citas.guardar(cita);
        return cita;
    }

    /** REQ-CIT-06: el cliente no llegó a su cita. */
    public Cita marcarInasistencia(String codigoCita) throws DatoInvalidoException, OperacionNoPermitidaException {
        Cita cita = obtenerPendienteDeHoyOAnterior(codigoCita);
        cita.marcarInasistencia();
        citas.guardar(cita);
        return cita;
    }

    private Cita obtenerPendienteDeHoyOAnterior(String codigoCita)
            throws DatoInvalidoException, OperacionNoPermitidaException {
        Validador.validarObligatorio(codigoCita, "cita");
        String codigo = codigoCita.trim().toUpperCase();
        Cita cita = citas.buscarPorCodigo(codigo)
                .orElseThrow(() -> new DatoInvalidoException("No existe la cita " + codigo));
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new OperacionNoPermitidaException("Solo se pueden modificar citas pendientes");
        }
        if (cita.getFecha().isAfter(LocalDate.now(reloj))) {
            throw new OperacionNoPermitidaException("No se puede cambiar el estado de una cita futura");
        }
        return cita;
    }

    /** Citas del barbero ese día que todavía ocupan su horario (pendientes y atendidas). */
    private List<Cita> citasQueOcupanHorario(String codigoBarbero, LocalDate fecha) {
        List<Cita> ocupadas = new ArrayList<>();
        for (Cita c : citas.buscarPorBarberoYFecha(codigoBarbero, fecha)) {
            if (c.getEstado() == EstadoCita.PENDIENTE || c.getEstado() == EstadoCita.ATENDIDA) {
                ocupadas.add(c);
            }
        }
        return ocupadas;
    }
}
