package barberia.modelo;

/** Servicio de tipo Cejas. */
public class Cejas extends Servicio {

    public Cejas(String codigo, String nombre, double precio, int duracionMin) {
        super(codigo, nombre, precio, duracionMin);
    }

    @Override
    public String getTipo() {
        return "Cejas";
    }
}
