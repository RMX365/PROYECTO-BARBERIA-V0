package barberia.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Combo: un servicio formado por varios servicios, con precio propio.
 * Los servicios existen por sí solos en el catálogo (relación de agregación).
 */
public class Combo extends Servicio {

    private final List<Servicio> servicios;

    public Combo(String codigo, String nombre, double precio, List<Servicio> servicios) {
        super(codigo, nombre, precio, 0);
        this.servicios = new ArrayList<>(servicios);
    }

    public List<Servicio> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    /** La duración del combo es la suma de la duración de sus servicios. */
    @Override
    public int getDuracion() {
        int total = 0;
        for (Servicio s : servicios) {
            total += s.getDuracion();
        }
        return total;
    }

    @Override
    public String getTipo() {
        return "Combo";
    }

    @Override
    public String toString() {
        StringBuilder incluye = new StringBuilder();
        for (Servicio s : servicios) {
            if (incluye.length() > 0) {
                incluye.append(" + ");
            }
            incluye.append(s.getNombre());
        }
        return super.toString() + " | incluye: " + incluye;
    }
}
