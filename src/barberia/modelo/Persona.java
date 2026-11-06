package barberia.modelo;

/**
 * Clase abstracta base de las personas del sistema (clientes y barberos).
 */
public abstract class Persona {

    private String nombres;
    private String celular;

    public Persona(String nombres, String celular) {
        this.nombres = nombres;
        this.celular = celular;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }
}
