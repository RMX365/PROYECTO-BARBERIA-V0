package barberia.modelo;

/** Servicio de tipo Tinte. */
public class Tinte extends Servicio {

    public Tinte(String codigo, String nombre, double precio, int duracionMin) {
        super(codigo, nombre, precio, duracionMin);
    }

    @Override
    public String getTipo() {
        return "Tinte";
    }
}
