package barberia.excepciones;

/** Se lanza cuando una operación necesita que el usuario la confirme antes de continuar. */
public class ConfirmacionRequeridaException extends Exception {

    public ConfirmacionRequeridaException(String mensaje) {
        super(mensaje);
    }
}
