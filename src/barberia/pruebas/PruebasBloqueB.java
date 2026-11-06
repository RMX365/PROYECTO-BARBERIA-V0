package barberia.pruebas;

import barberia.excepciones.DatoInvalidoException;
import barberia.excepciones.ServicioDuplicadoException;
import barberia.modelo.Combo;
import barberia.modelo.Corte;
import barberia.modelo.Servicio;
import barberia.repositorio.ServicioRepositorioMemoria;
import barberia.servicio.ServicioService;
import barberia.servicio.TotalesAtencion;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Prueba los escenarios de aceptación (3 por historia) de los requerimientos del bloque B.
 */
public class PruebasBloqueB {

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

    private static ServicioService nuevo() {
        return new ServicioService(new ServicioRepositorioMemoria());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("===== PRUEBAS BLOQUE B: SERVICIOS =====\n");
        pruebasRegistrarServicio();
        pruebasCatalogo();
        pruebasCombo();
        pruebasTotales();
        System.out.println("\nResultado: " + correctas + " de " + total + " escenarios correctos");
    }

    // ---------------- REQ-SER-01 ----------------
    private static void pruebasRegistrarServicio() throws Exception {
        ServicioService s = nuevo();

        Servicio corte = s.registrar("corte", "Corte clásico", 25, 30);
        verificar("HU-SER-01", 1, "Registro exitoso",
                corte instanceof Corte && corte.getTipo().equals("Corte") && corte.isActivo()
                        && s.catalogoActivos().contains(corte),
                "Se creó " + corte);

        try {
            s.registrar("barba", "Barba completa", 0, 20);
            verificar("HU-SER-01", 2, "Precio inválido", false, "No lanzó excepción");
        } catch (DatoInvalidoException e) {
            boolean negativo = false;
            try {
                s.registrar("barba", "Barba completa", -5, 20);
            } catch (DatoInvalidoException e2) {
                negativo = e2.getMessage().equals("El precio debe ser mayor a 0");
            }
            verificar("HU-SER-01", 2, "Precio inválido",
                    e.getMessage().equals("El precio debe ser mayor a 0") && negativo,
                    "Con precio 0 y con precio -5: " + e.getMessage());
        }

        try {
            s.registrar("corte", "CORTE CLÁSICO", 30, 30);
            verificar("HU-SER-01", 3, "Servicio duplicado", false, "No lanzó excepción");
        } catch (ServicioDuplicadoException e) {
            verificar("HU-SER-01", 3, "Servicio duplicado",
                    e.getMessage().equals("El servicio ya existe"), "Mensaje: " + e.getMessage());
        }
    }

    // ---------------- REQ-SER-03 ----------------
    private static void pruebasCatalogo() throws Exception {
        ServicioService s = nuevo();
        Servicio corte = s.registrar("corte", "Corte clásico", 25, 30);
        Servicio barba = s.registrar("barba", "Barba completa", 15, 20);

        List<Servicio> catalogo = s.catalogoActivos();
        verificar("HU-SER-03", 1, "Catálogo con servicios",
                catalogo.size() == 2, "Muestra:\n            " + catalogo.get(0) + "\n            " + catalogo.get(1));

        barba.desactivar();
        verificar("HU-SER-03", 2, "Servicio desactivado",
                s.catalogoActivos().size() == 1 && !s.catalogoActivos().contains(barba),
                "Tras desactivar 'Barba completa' el catálogo muestra solo: " + s.catalogoActivos());

        corte.desactivar();
        verificar("HU-SER-03", 3, "Catálogo vacío",
                s.catalogoActivos().isEmpty(), "Sin servicios activos -> 'No hay servicios registrados'");
    }

    // ---------------- REQ-SER-05 ----------------
    private static void pruebasCombo() throws Exception {
        ServicioService s = nuevo();
        Servicio corte = s.registrar("corte", "Corte clásico", 25, 30);
        Servicio barba = s.registrar("barba", "Barba completa", 15, 20);

        Combo combo = s.crearCombo("Corte + Barba", 35, Arrays.asList(corte.getCodigo(), barba.getCodigo()));
        verificar("HU-SER-05", 1, "Combo exitoso",
                combo.getServicios().size() == 2 && combo.getPrecio() == 35 && combo.getTipo().equals("Combo"),
                "Se creó " + combo);

        try {
            s.crearCombo("Combo solo", 20, Arrays.asList(corte.getCodigo()));
            verificar("HU-SER-05", 2, "Menos de dos servicios", false, "No lanzó excepción");
        } catch (DatoInvalidoException e) {
            verificar("HU-SER-05", 2, "Menos de dos servicios",
                    e.getMessage().equals("Un combo debe tener al menos 2 servicios"), "Mensaje: " + e.getMessage());
        }

        try {
            s.crearCombo("Combo gratis", 0, Arrays.asList(corte.getCodigo(), barba.getCodigo()));
            verificar("HU-SER-05", 3, "Precio inválido", false, "No lanzó excepción");
        } catch (DatoInvalidoException e) {
            verificar("HU-SER-05", 3, "Precio inválido",
                    e.getMessage().equals("El precio debe ser mayor a 0"), "Mensaje: " + e.getMessage());
        }
    }

    // ---------------- REQ-SER-06 ----------------
    private static void pruebasTotales() throws Exception {
        ServicioService s = nuevo();
        Servicio corte = s.registrar("corte", "Corte clásico", 25, 30);
        Servicio barba = s.registrar("barba", "Barba completa", 15, 20);
        Combo combo = s.crearCombo("Corte + Barba", 35, Arrays.asList(corte.getCodigo(), barba.getCodigo()));

        TotalesAtencion t1 = s.calcularTotales(Arrays.asList(corte));
        verificar("HU-SER-06", 1, "Un servicio",
                t1.getPrecioTotal() == 25 && t1.getDuracionTotal() == 30,
                "Corte clásico -> S/ " + String.format(Locale.US, "%.2f", t1.getPrecioTotal()) + " y " + t1.getDuracionTotal() + " min");

        TotalesAtencion t2 = s.calcularTotales(Arrays.asList(corte, barba));
        verificar("HU-SER-06", 2, "Varios servicios",
                t2.getPrecioTotal() == 40 && t2.getDuracionTotal() == 50,
                "Corte + Barba sueltos -> S/ " + String.format(Locale.US, "%.2f", t2.getPrecioTotal()) + " y " + t2.getDuracionTotal() + " min");

        TotalesAtencion t3 = s.calcularTotales(Arrays.asList(combo));
        verificar("HU-SER-06", 3, "Combo",
                t3.getPrecioTotal() == 35 && t3.getDuracionTotal() == 50,
                "Combo (precio propio) -> S/ " + String.format(Locale.US, "%.2f", t3.getPrecioTotal()) + " y " + t3.getDuracionTotal() + " min");
    }
}
