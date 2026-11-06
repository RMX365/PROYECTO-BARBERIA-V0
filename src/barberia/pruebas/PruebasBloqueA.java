package barberia.pruebas;

import barberia.excepciones.CelularDuplicadoException;
import barberia.excepciones.ClienteNoEncontradoException;
import barberia.excepciones.DatoInvalidoException;
import barberia.modelo.Barbero;
import barberia.modelo.Cliente;
import barberia.repositorio.BarberoRepositorioMemoria;
import barberia.repositorio.ClienteRepositorioMemoria;
import barberia.servicio.BarberoService;
import barberia.servicio.ClienteService;
import java.time.LocalTime;
import java.util.List;

/**
 * Prueba los escenarios de aceptación (3 por historia) de los requerimientos del bloque A.
 * Cada línea [OK] / [FALLÓ] sirve como evidencia en el Excel de historias de usuario.
 */
public class PruebasBloqueA {

    private static int total = 0;
    private static int correctas = 0;

    private static void verificar(String historia, int escenario, String titulo, boolean cumple, String detalle) {
        total++;
        if (cumple) {
            correctas++;
        }
        System.out.println((cumple ? "[OK]    " : "[FALLÓ] ") + historia + " / Esc. " + escenario
                + " - " + titulo + "\n          -> " + detalle);
    }

    private static ClienteService nuevoClienteService() {
        return new ClienteService(new ClienteRepositorioMemoria());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("===== PRUEBAS BLOQUE A: PERSONAS =====\n");
        pruebasRegistrarCliente();
        pruebasEditarCliente();
        pruebasBuscarCliente();
        pruebasCelularUnico();
        pruebasRegistrarBarbero();
        System.out.println("\nResultado: " + correctas + " de " + total + " escenarios correctos");
    }

    // ---------------- REQ-CLI-01 ----------------
    private static void pruebasRegistrarCliente() throws Exception {
        ClienteService s = nuevoClienteService();

        Cliente c = s.registrar("Juan Pérez Quispe", "987654321");
        verificar("HU-CLI-01", 1, "Registro exitoso",
                c.getCodigo().equals("CLI-001") && c.isActivo(), "Se creó " + c);

        try {
            s.registrar("Pedro Ramos", "98765A321");
            verificar("HU-CLI-01", 2, "Celular inválido", false, "No lanzó excepción");
        } catch (DatoInvalidoException e) {
            verificar("HU-CLI-01", 2, "Celular inválido",
                    e.getMessage().equals("El celular debe tener 9 dígitos numéricos"), "Mensaje: " + e.getMessage());
        }

        try {
            s.registrar("   ", "912345678");
            verificar("HU-CLI-01", 3, "Nombres obligatorios", false, "No lanzó excepción");
        } catch (DatoInvalidoException e) {
            verificar("HU-CLI-01", 3, "Nombres obligatorios",
                    e.getMessage().equals("Los nombres son obligatorios"), "Mensaje: " + e.getMessage());
        }
    }

    // ---------------- REQ-CLI-02 ----------------
    private static void pruebasEditarCliente() throws Exception {
        ClienteService s = nuevoClienteService();
        Cliente a = s.registrar("Juan Pérez", "987654321");
        Cliente b = s.registrar("Carlos Ñañez", "912345678");

        Cliente editado = s.editar(a.getCodigo(), "Juan Pérez Quispe", "987654322");
        verificar("HU-CLI-02", 1, "Edición exitosa",
                editado.getNombres().equals("Juan Pérez Quispe") && editado.getCelular().equals("987654322"),
                "Quedó " + editado);

        try {
            s.editar(a.getCodigo(), "Juan Pérez Quispe", b.getCelular());
            verificar("HU-CLI-02", 2, "Celular de otro cliente", false, "No lanzó excepción");
        } catch (CelularDuplicadoException e) {
            verificar("HU-CLI-02", 2, "Celular de otro cliente",
                    e.getMessage().equals("El celular ya está registrado") && a.getCelular().equals("987654322"),
                    "Mensaje: " + e.getMessage() + " (el cliente conserva su celular)");
        }

        try {
            s.editar("", "Nombre", "999999999");
            verificar("HU-CLI-02", 3, "Sin cliente seleccionado", false, "No lanzó excepción");
        } catch (ClienteNoEncontradoException e) {
            verificar("HU-CLI-02", 3, "Sin cliente seleccionado",
                    e.getMessage().equals("Seleccione un cliente"), "Mensaje: " + e.getMessage());
        }
    }

    // ---------------- REQ-CLI-03 ----------------
    private static void pruebasBuscarCliente() throws Exception {
        ClienteService s = nuevoClienteService();
        s.registrar("Juan Pérez Quispe", "987654321");
        s.registrar("Carlos Ñañez Torres", "912345678");

        List<Cliente> r1 = s.buscar("juan");
        verificar("HU-CLI-03", 1, "Búsqueda por nombre",
                r1.size() == 1 && r1.get(0).getNombres().startsWith("Juan"), "Escribió 'juan' y encontró: " + r1);

        List<Cliente> r2 = s.buscar("912345678");
        verificar("HU-CLI-03", 2, "Búsqueda por celular",
                r2.size() == 1 && r2.get(0).getNombres().startsWith("Carlos"), "Encontró: " + r2);

        List<Cliente> r3 = s.buscar("zzz");
        verificar("HU-CLI-03", 3, "Sin resultados", r3.isEmpty(), "Lista vacía -> 'No se encontraron clientes'");
    }

    // ---------------- REQ-CLI-05 ----------------
    private static void pruebasCelularUnico() throws Exception {
        ClienteService s = nuevoClienteService();

        Cliente nuevo = s.registrar("Ana Gómez", "944444444");
        verificar("HU-CLI-05", 1, "Celular nuevo", nuevo != null, "Permitió registrar " + nuevo);

        try {
            s.registrar("Otra Ana", "944444444");
            verificar("HU-CLI-05", 2, "Celular repetido", false, "No lanzó excepción");
        } catch (CelularDuplicadoException e) {
            verificar("HU-CLI-05", 2, "Celular repetido",
                    e.getMessage().equals("Ya existe un cliente con ese celular: Ana Gómez"), "Mensaje: " + e.getMessage());
        }

        nuevo.desactivar();
        try {
            s.registrar("Otra Ana", "944444444");
            verificar("HU-CLI-05", 3, "Celular de cliente inactivo", false, "No lanzó excepción");
        } catch (CelularDuplicadoException e) {
            verificar("HU-CLI-05", 3, "Celular de cliente inactivo",
                    e.getMessage().contains("inactivo") && e.getMessage().contains("Reactívelo"),
                    "Mensaje: " + e.getMessage());
        }
    }

    // ---------------- REQ-BAR-01 ----------------
    private static void pruebasRegistrarBarbero() throws Exception {
        BarberoService s = new BarberoService(new BarberoRepositorioMemoria());

        Barbero b = s.registrar("Pedro Ramos", "944444444", LocalTime.of(9, 0), LocalTime.of(19, 0));
        verificar("HU-BAR-01", 1, "Registro exitoso",
                b.getCodigo().equals("BAR-001"), "Se creó " + b);

        try {
            s.registrar("Luis Díaz", "955555555", LocalTime.of(19, 0), LocalTime.of(9, 0));
            verificar("HU-BAR-01", 2, "Horario inválido", false, "No lanzó excepción");
        } catch (DatoInvalidoException e) {
            verificar("HU-BAR-01", 2, "Horario inválido",
                    e.getMessage().equals("El horario de atención no es válido"), "Mensaje: " + e.getMessage());
        }

        try {
            s.registrar("", "955555555", LocalTime.of(9, 0), LocalTime.of(19, 0));
            verificar("HU-BAR-01", 3, "Nombres obligatorios", false, "No lanzó excepción");
        } catch (DatoInvalidoException e) {
            verificar("HU-BAR-01", 3, "Nombres obligatorios",
                    e.getMessage().equals("Los nombres son obligatorios"), "Mensaje: " + e.getMessage());
        }
    }
}
