package barberia.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Cita agendada para un cliente con un barbero y un servicio.
 */
public class Cita {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String codigo;
    private final Cliente cliente;
    private final Barbero barbero;
    private final Servicio servicio;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final int duracionMin;
    private EstadoCita estado;

    public Cita(String codigo, Cliente cliente, Barbero barbero, Servicio servicio, LocalDate fecha, LocalTime horaInicio) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.barbero = barbero;
        this.servicio = servicio;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.duracionMin = servicio.getDuracion();   // se guarda la duración vigente al agendar
        this.estado = EstadoCita.PENDIENTE;
    }

    public String getCodigo() {
        return codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Barbero getBarbero() {
        return barbero;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public LocalDateTime getInicio() {
        return LocalDateTime.of(fecha, horaInicio);
    }

    public LocalDateTime getFin() {
        return getInicio().plusMinutes(duracionMin);
    }

    public LocalTime getHoraFin() {
        return getFin().toLocalTime();
    }

    /** Dos citas se cruzan si son del mismo barbero y sus horarios se solapan (citas consecutivas no se cruzan). */
    public boolean seCruzaCon(Cita otra) {
        return barbero.getCodigo().equals(otra.barbero.getCodigo())
                && getInicio().isBefore(otra.getFin())
                && otra.getInicio().isBefore(getFin());
    }

    public void marcarAtendida() {
        estado = EstadoCita.ATENDIDA;
    }

    public void marcarInasistencia() {
        estado = EstadoCita.INASISTENCIA;
    }

    @Override
    public String toString() {
        return codigo + " | " + fecha.format(FORMATO_FECHA) + " " + horaInicio + "-" + getHoraFin()
                + " | " + cliente.getNombres() + " | Barbero: " + barbero.getNombres()
                + " | " + servicio.getNombre() + " | " + estado;
    }
}
