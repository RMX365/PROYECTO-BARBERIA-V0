package barberia.modelo;

/** Servicio de tipo Barba. */
public class Barba extends Servicio {

    public Barba(String codigo, String nombre, double precio, int duracionMin) {
        super(codigo, nombre, precio, duracionMin);
    }

    @Override
    public String getTipo() {
        return "Barba";
    }
}
