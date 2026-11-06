package barberia.excepciones;

import barberia.modelo.Cliente;

/** Se lanza cuando el celular ya pertenece a otro cliente. */
public class CelularDuplicadoException extends Exception {

    private final Cliente existente;

    public CelularDuplicadoException(String mensaje, Cliente existente) {
        super(mensaje);
        this.existente = existente;
    }

    public Cliente getExistente() {
        return existente;
    }
}
