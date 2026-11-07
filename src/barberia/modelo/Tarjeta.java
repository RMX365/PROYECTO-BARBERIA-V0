package barberia.modelo;

/** Pago con Tarjeta. */
public class Tarjeta implements Pagable {

    @Override
    public boolean pagar(double monto) {
        return monto > 0;
    }

    @Override
    public String getMetodo() {
        return "Tarjeta";
    }
}
