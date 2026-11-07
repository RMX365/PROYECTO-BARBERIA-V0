package barberia.modelo;

/** Estados posibles de una cita. */
public enum EstadoCita {
    PENDIENTE("Pendiente"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada"),
    INASISTENCIA("Inasistencia");

    private final String etiqueta;

    EstadoCita(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
