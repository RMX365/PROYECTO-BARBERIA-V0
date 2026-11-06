package barberia.excepciones;

/** Se lanza cuando se busca un cliente que no existe o no se seleccionó ninguno. */
public class ClienteNoEncontradoException extends Exception {

    public ClienteNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
