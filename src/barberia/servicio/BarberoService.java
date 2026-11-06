package barberia.servicio;

import barberia.excepciones.DatoInvalidoException;
import barberia.modelo.Barbero;
import barberia.repositorio.BarberoRepositorio;
import barberia.util.Validador;
import java.time.LocalTime;

/**
 * Lógica de negocio de los barberos: REQ-BAR-01 (registrar barbero).
 */
public class BarberoService {

    private final BarberoRepositorio repositorio;

    public BarberoService(BarberoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Barbero registrar(String nombres, String celular, LocalTime horaInicio, LocalTime horaFin)
            throws DatoInvalidoException {
        Validador.validarNombres(nombres);
        Validador.validarCelular(celular);
        Validador.validarHorario(horaInicio, horaFin);

        Barbero nuevo = new Barbero(repositorio.siguienteCodigo(), nombres.trim(), celular.trim(), horaInicio, horaFin);
        repositorio.guardar(nuevo);
        return nuevo;
    }
}
