package barberia.modelo;

import java.util.Locale;

/**
 * Clase abstracta base de los servicios que ofrece la barbería.
 * Cada tipo de servicio (Corte, Barba, Tinte, Cejas) indica su tipo con getTipo().
 */
public abstract class Servicio {

    private final String codigo;
    private final String nombre;
    private double precio;
    private int duracionMin;
    private boolean activo;

    public Servicio(String codigo, String nombre, double precio, int duracionMin) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.duracionMin = duracionMin;
        this.activo = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    /** Duración en minutos. Un Combo la redefine sumando la de sus servicios. */
    public int getDuracion() {
        return duracionMin;
    }

    /** Cambia el precio y la duración. Las citas ya agendadas guardan su propia copia y no se alteran. */
    public void actualizar(double precio, int duracionMin) {
        this.precio = precio;
        this.duracionMin = duracionMin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void desactivar() {
        activo = false;
    }

    /** Cada subclase devuelve su tipo ("Corte", "Barba", "Tinte", "Cejas" o "Combo"). */
    public abstract String getTipo();

    @Override
    public String toString() {
        return String.format(Locale.US, "%s | %s | %s | S/ %.2f | %d min%s",
                codigo, nombre, getTipo(), getPrecio(), getDuracion(), activo ? "" : " | Inactivo");
    }
}
