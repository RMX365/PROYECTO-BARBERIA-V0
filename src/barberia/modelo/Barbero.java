package barberia.modelo;

import java.time.LocalTime;

/**
 * Barbero de la barbería. Hereda nombres y celular de Persona.
 */
public class Barbero extends Persona {

    private final String codigo;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean activo;

    public Barbero(String codigo, String nombres, String celular, LocalTime horaInicio, LocalTime horaFin) {
        super(nombres, celular);
        this.codigo = codigo;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.activo = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        return codigo + " | " + getNombres() + " | " + getCelular()
                + " | horario: " + horaInicio + " - " + horaFin
                + " | " + (activo ? "Activo" : "Inactivo");
    }
}
