package barberia.modelo;

/**
 * Método de pago. Cada método (Efectivo, Yape, Tarjeta) lo implementa a su manera.
 */
public interface Pagable {

    /** Procesa el pago del monto indicado. Devuelve true si se aceptó. */
    boolean pagar(double monto);

    String getMetodo();
}
