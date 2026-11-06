package barberia.excepciones;

/** Se lanza cuando ya existe un servicio con el mismo nombre. */
public class ServicioDuplicadoException extends Exception {

    public ServicioDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
