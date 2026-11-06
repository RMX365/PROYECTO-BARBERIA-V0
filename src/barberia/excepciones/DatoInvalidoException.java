package barberia.excepciones;

/** Se lanza cuando un dato ingresado no cumple las validaciones. */
public class DatoInvalidoException extends Exception {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
