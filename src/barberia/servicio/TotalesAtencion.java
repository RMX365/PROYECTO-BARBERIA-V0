package barberia.servicio;

/** Precio y duración total de una atención. */
public class TotalesAtencion {

    private final double precioTotal;
    private final int duracionTotal;

    public TotalesAtencion(double precioTotal, int duracionTotal) {
        this.precioTotal = precioTotal;
        this.duracionTotal = duracionTotal;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public int getDuracionTotal() {
        return duracionTotal;
    }
}
