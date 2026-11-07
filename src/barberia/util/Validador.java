package barberia.util;

import barberia.excepciones.DatoInvalidoException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Validaciones reutilizables del sistema.
 */
public final class Validador {

    // Acepta "9:00" y "09:00"
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");
    // Acepta "3/11/2026" y "03/11/2026" y rechaza fechas que no existen (31/02/2026)
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Validador() {
    }

    /** Convierte un texto como "9:00" o "21:30" en una hora. */
    public static LocalTime parsearHora(String texto) throws DatoInvalidoException {
        try {
            return LocalTime.parse(texto.trim(), FORMATO_HORA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new DatoInvalidoException("Use el formato HH:mm para las horas (ejemplo 09:00)");
        }
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

    public static void validarNombreServicio(String nombre) throws DatoInvalidoException {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre del servicio es obligatorio");
        }
    }

    public static void validarPrecio(double precio) throws DatoInvalidoException {
        if (precio <= 0) {
            throw new DatoInvalidoException("El precio debe ser mayor a 0");
        }
    }

    public static void validarDuracion(int minutos) throws DatoInvalidoException {
        if (minutos <= 0) {
            throw new DatoInvalidoException("La duración debe ser mayor a 0");
        }
    }

    /** Convierte "25", "25.5" o "25,5" en número. Si está vacío devuelve 0 (luego lo rechaza validarPrecio). */
    public static double parsearPrecio(String texto) throws DatoInvalidoException {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        try {
            double precio = Double.parseDouble(texto.trim().replace(',', '.'));
            if (Double.isNaN(precio) || Double.isInfinite(precio)) {
                throw new NumberFormatException();
            }
            return precio;
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El precio debe ser un número");
        }
    }

    public static int parsearDuracion(String texto) throws DatoInvalidoException {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new DatoInvalidoException("La duración debe ser un número entero de minutos");
        }
    }

    /** Convierte un texto como "3/11/2026" en una fecha. */
    public static LocalDate parsearFecha(String texto) throws DatoInvalidoException {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new DatoInvalidoException("Use el formato dd/MM/aaaa para la fecha (ejemplo 03/11/2026)");
        }
    }

    /** Verifica que un dato obligatorio no esté vacío. */
    public static void validarObligatorio(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException("Falta indicar el dato: " + campo);
        }
    }
}
