package barberia.pruebas;

import barberia.modelo.Atencion;
import barberia.modelo.Barbero;
import barberia.modelo.Cita;
import barberia.modelo.Cliente;
import barberia.modelo.Combo;
import barberia.modelo.Servicio;
import barberia.repositorio.AtencionRepositorioMemoria;
import barberia.repositorio.BarberoRepositorioMemoria;
import barberia.repositorio.CitaRepositorioMemoria;
import barberia.repositorio.ClienteRepositorioMemoria;
import barberia.repositorio.ServicioRepositorioMemoria;
import barberia.servicio.AgendaService;
import barberia.servicio.AtencionService;
import barberia.servicio.BarberoService;
import barberia.servicio.CajaService;
import barberia.servicio.ClienteService;
import barberia.servicio.EstadoFidelidad;
import barberia.servicio.ServicioService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Prueba los escenarios de aceptación (3 por historia) de los requerimientos del bloque D:
 * CLI-04, CLI-06, CLI-07, SER-02 y SER-04.
 */
public class PruebasBloqueD {

    private static final LocalDate MANANA = LocalDate.of(2026, 11, 3);

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

    private static String error(Callable<?> accion) {
        try {
            accion.call();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /** Reloj que se puede mover, para probar atenciones pagadas en momentos distintos. */
    private static class RelojManual extends Clock {
        private Instant instante;
        private final ZoneId zona = ZoneId.systemDefault();

        RelojManual(LocalDateTime inicial) {
            fijar(inicial);
        }

        void fijar(LocalDateTime momento) {
            instante = momento.atZone(zona).toInstant();
        }

        @Override
        public ZoneId getZone() {
            return zona;
        }

        @Override
        public Clock withZone(ZoneId z) {
            return this;
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }

    private static class Contexto {
        final RelojManual reloj = new RelojManual(LocalDateTime.of(2026, 11, 2, 8, 0));
        final ClienteService clientes;
        final BarberoService barberos = new BarberoService(new BarberoRepositorioMemoria());
        final ServicioService servicios = new ServicioService(new ServicioRepositorioMemoria());
        final AgendaService agenda;
        final AtencionService atenciones;
        final CajaService caja;
        Cliente jose;
        Barbero alan;
        Servicio corte;
        Servicio barba;

        Contexto(int visitasParaDescuento) throws Exception {
            clientes = new ClienteService(new ClienteRepositorioMemoria(), visitasParaDescuento);
            CitaRepositorioMemoria citas = new CitaRepositorioMemoria();
            AtencionRepositorioMemoria atencionesRepo = new AtencionRepositorioMemoria();
            agenda = new AgendaService(citas, clientes, barberos, servicios, reloj);
            atenciones = new AtencionService(atencionesRepo, citas, clientes, barberos, servicios, reloj);
            caja = new CajaService(atencionesRepo, reloj);
            jose = clientes.registrar("Jose Quispe", "963258417");
            alan = barberos.registrar("Alan Ruiz", "987456132", LocalTime.of(9, 0), LocalTime.of(19, 0));
            corte = servicios.registrar("corte", "Corte clásico", 25, 30);
            barba = servicios.registrar("barba", "Barba completa", 15, 20);
        }

        Contexto() throws Exception {
            this(ClienteService.VISITAS_PARA_DESCUENTO);
        }

        /** Registra una atención sin cita para el cliente y la paga en efectivo. */
        Atencion atenderYCobrar(Cliente cliente, Servicio... lista) throws Exception {
            List<String> codigos = new java.util.ArrayList<>();
            for (Servicio s : lista) {
                codigos.add(s.getCodigo());
            }
            Atencion a = atenciones.registrarSinCita(cliente.getCodigo(), alan.getCodigo(), codigos);
            caja.pagar(a.getCodigo(), caja.crearMetodo("efectivo"));
            return a;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("===== PRUEBAS BLOQUE D: CLIENTES Y SERVICIOS =====\n");
        pruebasListarClientes();
        pruebasHistorial();
        pruebasFidelidad();
        pruebasModificarServicio();
        pruebasDesactivarServicio();
        System.out.println("\nResultado: " + correctas + " de " + total + " escenarios correctos");
    }

    // ---------------- REQ-CLI-04 ----------------
    private static void pruebasListarClientes() throws Exception {
        ClienteService s = new ClienteService(new ClienteRepositorioMemoria());
        verificar("HU-CLI-04", 2, "Sin clientes",
                s.listar(false).isEmpty() && s.totalClientes() == 0,
                "Lista vacía -> 'Aún no hay clientes registrados'");

        s.registrar("Zoe Vargas", "955555551");
        Cliente ana = s.registrar("Ana Gómez", "955555552");
        s.registrar("Mario Díaz", "955555553");
        List<Cliente> lista = s.listar(false);
        verificar("HU-CLI-04", 1, "Lista con clientes",
                lista.size() == 3 && lista.get(0).getNombres().equals("Ana Gómez")
                        && lista.get(1).getNombres().equals("Mario Díaz") && lista.get(2).getNombres().equals("Zoe Vargas"),
                "Ordenados por nombres: " + lista.get(0).getNombres() + ", " + lista.get(1).getNombres() + ", " + lista.get(2).getNombres());

        ana.desactivar();
        verificar("HU-CLI-04", 3, "Clientes inactivos",
                s.listar(false).size() == 2 && s.listar(true).size() == 3,
                "Sin marcar 'Mostrar inactivos': " + s.listar(false).size() + " clientes | Marcando la opción: " + s.listar(true).size());
    }

    // ---------------- REQ-CLI-06 ----------------
    private static void pruebasHistorial() throws Exception {
        Contexto x = new Contexto();
        Atencion primera = x.atenderYCobrar(x.jose, x.corte);
        x.reloj.fijar(LocalDateTime.of(2026, 11, 2, 10, 0));
        Atencion segunda = x.atenderYCobrar(x.jose, x.corte, x.barba);

        List<Atencion> historial = x.atenciones.historialDeCliente(x.jose.getCodigo());
        verificar("HU-CLI-06", 1, "Cliente con historial",
                historial.size() == 2 && historial.get(0) == segunda && historial.get(1) == primera,
                "De la más reciente a la más antigua:\n            " + historial.get(0).resumenHistorial()
                        + "\n            " + historial.get(1).resumenHistorial());

        Cliente ana = x.clientes.registrar("Ana Gómez", "955555552");
        verificar("HU-CLI-06", 2, "Cliente sin historial",
                x.atenciones.historialDeCliente(ana.getCodigo()).isEmpty(),
                "Lista vacía -> 'El cliente aún no tiene atenciones'");

        Contexto y = new Contexto();
        Atencion pendiente = y.atenciones.registrarSinCita(y.jose.getCodigo(), y.alan.getCodigo(), Arrays.asList(y.corte.getCodigo()));
        int antes = y.atenciones.historialDeCliente(y.jose.getCodigo()).size();
        y.caja.pagar(pendiente.getCodigo(), y.caja.crearMetodo("yape"));
        int despues = y.atenciones.historialDeCliente(y.jose.getCodigo()).size();
        verificar("HU-CLI-06", 3, "Registro automático",
                antes == 0 && despues == 1,
                "Antes de pagar la atención: " + antes + " en el historial | Después de pagar: " + despues);
    }

    // ---------------- REQ-CLI-07 ----------------
    private static void pruebasFidelidad() throws Exception {
        Contexto x = new Contexto(3);   // en esta prueba el descuento se gana con 3 visitas
        verificar("HU-CLI-07", 1, "Visita contada",
                x.jose.getVisitas() == 0 && x.atenderYCobrar(x.jose, x.corte) != null && x.jose.getVisitas() == 1,
                "Visitas antes del pago: 0 | después de pagar una atención: " + x.jose.getVisitas());

        EstadoFidelidad aunNo = x.clientes.estadoFidelidad(x.jose.getCodigo());
        x.atenderYCobrar(x.jose, x.barba);
        x.atenderYCobrar(x.jose, x.corte);
        EstadoFidelidad disponible = x.clientes.estadoFidelidad(x.jose.getCodigo());
        verificar("HU-CLI-07", 2, "Descuento disponible",
                disponible.isDescuentoDisponible() && disponible.getMensaje().equals("El cliente tiene descuento por fidelidad"),
                "Con 3 visitas -> '" + disponible.getMensaje() + "'");

        verificar("HU-CLI-07", 3, "Descuento aún no disponible",
                !aunNo.isDescuentoDisponible() && aunNo.getFaltan() == 2
                        && aunNo.getMensaje().equals("Visitas acumuladas: 1 de 3. Faltan 2 para el descuento por fidelidad"),
                "Con 1 visita -> '" + aunNo.getMensaje() + "'");
    }

    // ---------------- REQ-SER-02 ----------------
    private static void pruebasModificarServicio() throws Exception {
        Contexto x = new Contexto();
        Servicio s = x.servicios.modificar(x.corte.getCodigo(), 30, 40);
        verificar("HU-SER-02", 1, "Modificación exitosa",
                s.getPrecio() == 30 && s.getDuracion() == 40, "Quedó: " + s);

        String precioMalo = error(() -> x.servicios.modificar(x.barba.getCodigo(), 0, 20));
        String duracionMala = error(() -> x.servicios.modificar(x.barba.getCodigo(), 15, -1));
        verificar("HU-SER-02", 2, "Valores inválidos",
                "El precio debe ser mayor a 0".equals(precioMalo) && "La duración debe ser mayor a 0".equals(duracionMala)
                        && x.barba.getPrecio() == 15 && x.barba.getDuracion() == 20,
                "Precio 0 -> '" + precioMalo + "' | Duración -1 -> '" + duracionMala + "' (el servicio no cambió)");

        Contexto y = new Contexto();
        Cita vieja = y.agenda.agendar(y.jose.getCodigo(), y.alan.getCodigo(), y.corte.getCodigo(), MANANA, LocalTime.of(10, 0));
        y.servicios.modificar(y.corte.getCodigo(), 30, 40);
        Cita nueva = y.agenda.agendar(y.jose.getCodigo(), y.alan.getCodigo(), y.corte.getCodigo(), MANANA, LocalTime.of(15, 0));
        verificar("HU-SER-02", 3, "Citas ya agendadas",
                vieja.getPrecio() == 25 && vieja.getDuracionMin() == 30 && nueva.getPrecio() == 30 && nueva.getDuracionMin() == 40,
                "Cita anterior: S/ " + vieja.getPrecio() + " y " + vieja.getDuracionMin() + " min | Cita nueva: S/ "
                        + nueva.getPrecio() + " y " + nueva.getDuracionMin() + " min");
    }

    // ---------------- REQ-SER-04 ----------------
    private static void pruebasDesactivarServicio() throws Exception {
        Contexto x = new Contexto();
        x.servicios.desactivar(x.barba.getCodigo(), false);
        String enCita = error(() -> x.agenda.agendar(x.jose.getCodigo(), x.alan.getCodigo(), x.barba.getCodigo(), MANANA, LocalTime.of(10, 0)));
        verificar("HU-SER-04", 1, "Desactivación exitosa",
                !x.barba.isActivo() && !x.servicios.catalogoActivos().contains(x.barba)
                        && ("El servicio " + x.barba.getCodigo() + " está inactivo").equals(enCita),
                "Ya no sale en el catálogo y al agendar con él -> '" + enCita + "'");

        Contexto y = new Contexto();
        Combo combo = y.servicios.crearCombo("Corte + Barba", 35, Arrays.asList(y.corte.getCodigo(), y.barba.getCodigo()));
        String aviso = error(() -> y.servicios.desactivar(y.barba.getCodigo(), false));
        boolean sigueActivo = y.barba.isActivo() && combo.isActivo();
        y.servicios.desactivar(y.barba.getCodigo(), true);
        verificar("HU-SER-04", 2, "Servicio dentro de un combo",
                aviso != null && aviso.contains("Corte + Barba") && sigueActivo && !y.barba.isActivo() && !combo.isActivo(),
                "Aviso: '" + aviso + "' | Al confirmar se desactivaron el servicio y el combo");

        Contexto z = new Contexto();
        Atencion pasada = z.atenderYCobrar(z.jose, z.corte);
        z.servicios.desactivar(z.corte.getCodigo(), false);
        List<Atencion> historial = z.atenciones.historialDeCliente(z.jose.getCodigo());
        verificar("HU-SER-04", 3, "Historial conservado",
                historial.size() == 1 && historial.get(0) == pasada && pasada.getServicios().get(0) == z.corte
                        && pasada.resumenHistorial().contains("Corte clásico"),
                "Con el servicio desactivado, la atención pasada sigue mostrando: " + pasada.resumenHistorial());
    }
}
