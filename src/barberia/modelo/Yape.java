package barberia.modelo;

/** Pago con Yape. */
public class Yape implements Pagable {

    @Override
    public boolean pagar(double monto) {
        return monto > 0;
    }

    @Override
    public String getMetodo() {
        return "Yape";
    }
}
