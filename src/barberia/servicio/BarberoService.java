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

    /** Busca un barbero activo por su código. */
    public Barbero obtenerActivo(String codigo) throws DatoInvalidoException {
        String c = codigo.trim().toUpperCase();
        Barbero barbero = repositorio.buscarPorCodigo(c)
                .orElseThrow(() -> new DatoInvalidoException("No existe el barbero " + c));
        if (!barbero.isActivo()) {
            throw new DatoInvalidoException("El barbero " + c + " está inactivo");
        }
        return barbero;
    }
}
