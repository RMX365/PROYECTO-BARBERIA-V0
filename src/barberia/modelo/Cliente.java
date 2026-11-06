package barberia.modelo;

/**
 * Cliente de la barbería. Hereda nombres y celular de Persona.
 */
public class Cliente extends Persona {

    private final String codigo;
    private int visitas;
    private boolean activo;

    public Cliente(String codigo, String nombres, String celular) {
        super(nombres, celular);
        this.codigo = codigo;
        this.visitas = 0;
        this.activo = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getVisitas() {
        return visitas;
    }

    public boolean isActivo() {
        return activo;
    }

    public void registrarVisita() {
        visitas++;
    }

    public boolean esNuevo() {
        return visitas == 0;
    }

    public void desactivar() {
        activo = false;
    }

    @Override
    public String toString() {
        return codigo + " | " + getNombres() + " | " + getCelular()
                + " | visitas: " + visitas + " | " + (activo ? "Activo" : "Inactivo");
    }
}
