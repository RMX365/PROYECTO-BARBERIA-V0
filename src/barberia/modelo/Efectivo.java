package barberia.modelo;

/** Pago con Efectivo. */
public class Efectivo implements Pagable {

    @Override
    public boolean pagar(double monto) {
        return monto > 0;
    }

    @Override
    public String getMetodo() {
        return "Efectivo";
    }
}
