package barberia.excepciones;

/** Se lanza cuando una operación no se puede realizar por una regla del negocio (horario ocupado, cita futura, atención ya pagada...). */
public class OperacionNoPermitidaException extends Exception {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
