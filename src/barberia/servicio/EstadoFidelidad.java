package barberia.servicio;

/** Situación de un cliente respecto al descuento por fidelidad. */
public class EstadoFidelidad {

    private final int visitas;
    private final int requeridas;

    public EstadoFidelidad(int visitas, int requeridas) {
        this.visitas = visitas;
        this.requeridas = requeridas;
    }

    public int getVisitas() {
        return visitas;
    }

    public int getRequeridas() {
        return requeridas;
    }

    public boolean isDescuentoDisponible() {
        return visitas >= requeridas;
    }

    public int getFaltan() {
        return Math.max(0, requeridas - visitas);
    }

    public String getMensaje() {
        if (isDescuentoDisponible()) {
            return "El cliente tiene descuento por fidelidad";
        }
        return "Visitas acumuladas: " + visitas + " de " + requeridas
                + ". Faltan " + getFaltan() + " para el descuento por fidelidad";
    }
}
