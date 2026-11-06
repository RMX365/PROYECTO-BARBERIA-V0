package barberia.modelo;

/** Servicio de tipo Corte. */
public class Corte extends Servicio {

    public Corte(String codigo, String nombre, double precio, int duracionMin) {
        super(codigo, nombre, precio, duracionMin);
    }

    @Override
    public String getTipo() {
        return "Corte";
    }
}
