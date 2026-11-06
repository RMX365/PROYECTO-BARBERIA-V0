package barberia.util;

import barberia.excepciones.DatoInvalidoException;
import java.time.LocalTime;

/**
 * Validaciones reutilizables del sistema.
 */
public final class Validador {

    private Validador() {
    }

    public static void validarNombres(String nombres) throws DatoInvalidoException {
        if (nombres == null || nombres.isBlank()) {
            throw new DatoInvalidoException("Los nombres son obligatorios");
        }
    }

    public static void validarCelular(String celular) throws DatoInvalidoException {
        if (celular == null || !celular.trim().matches("\\d{9}")) {
            throw new DatoInvalidoException("El celular debe tener 9 dígitos numéricos");
        }
    }

    public static void validarHorario(LocalTime inicio, LocalTime fin) throws DatoInvalidoException {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new DatoInvalidoException("El horario de atención no es válido");
        }
    }
}
