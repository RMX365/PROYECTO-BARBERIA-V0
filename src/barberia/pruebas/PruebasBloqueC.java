package barberia.pruebas;

import barberia.modelo.Atencion;
import barberia.modelo.Barbero;
import barberia.modelo.Cita;
import barberia.modelo.Cliente;
import barberia.modelo.EstadoCita;
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
import barberia.servicio.ServicioService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Prueba los escenarios de aceptación (3 por historia) de los requerimientos del bloque C.
 * Se usa un reloj fijo (lunes 02/11/2026, 08:00) para que las pruebas den siempre el mismo resultado.
 */
public class PruebasBloqueC {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 2);
    private static final LocalDate MANANA = LocalDate.of(2026, 11, 3);
    private static final LocalDate AYER = LocalDate.of(2026, 11, 1);

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

    /** Ejecuta una acción y devuelve el mensaje del error que lanzó (o null si no hubo error). */
    private static String error(Callable<?> accion) {
        try {
            accion.call();
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /** Todo lo necesario para una prueba, con datos base nuevos. */
    private static class Contexto {
        final Clock reloj = Clock.fixed(LocalDateTime.of(2026, 11, 2, 8, 0)
                .atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        final ClienteService clientes = new ClienteService(new ClienteRepositorioMemoria());
        final BarberoService barberos = new BarberoService(new BarberoRepositorioMemoria());
        final ServicioService servicios = new ServicioService(new ServicioRepositorioMemoria());
        final CitaRepositorioMemoria citasRepo = new CitaRepositorioMemoria();
        final AtencionRepositorioMemoria atencionesRepo = new AtencionRepositorioMemoria();
        final AgendaService agenda;
        final AtencionService atenciones;
        final CajaService caja;
        final Cliente jose;
        final Barbero alan;
        final Servicio corte;
        final Servicio barba;

        Contexto() throws Exception {
            agenda = new AgendaService(citasRepo, clientes, barberos, servicios, reloj);
            atenciones = new AtencionService(atencionesRepo, citasRepo, clientes, barberos, servicios, reloj);
            caja = new CajaService(atencionesRepo, reloj);
            jose = clientes.registrar("Jose Quispe", "963258417");
            alan = barberos.registrar("Alan Ruiz", "987456132", LocalTime.of(9, 0), LocalTime.of(19, 0));
            corte = servicios.registrar("corte", "Corte clásico", 25, 30);
            barba = servicios.registrar("barba", "Barba completa", 15, 20);
        }

        Cita cita(LocalDate fecha, int hora, int minuto) throws Exception {
            return agenda.agendar(jose.getCodigo(), alan.getCodigo(), corte.getCodigo(), fecha, LocalTime.of(hora, minuto));
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("===== PRUEBAS BLOQUE C: CITAS Y CAJA =====\n");
        pruebasAgendar();
        pruebasSinCruces();
        pruebasHorarios();
        pruebasMarcarCita();
        pruebasBarberoDeLaAtencion();
        pruebasPago();
        System.out.println("\nResultado: " + correctas + " de " + total + " escenarios correctos");
    }

    // ---------------- REQ-CIT-01 ----------------
    private static void pruebasAgendar() throws Exception {
        Contexto x = new Contexto();

        Cita c = x.cita(MANANA, 10, 0);
        verificar("HU-CIT-01", 1, "Cita agendada",
                c.getEstado() == EstadoCita.PENDIENTE && c.getCodigo().equals("CIT-001"), "Se creó " + c);

        String sinServicio = error(() -> x.agenda.agendar(x.jose.getCodigo(), x.alan.getCodigo(), "", MANANA, LocalTime.of(11, 0)));
        String sinFecha = error(() -> x.agenda.agendar(x.jose.getCodigo(), x.alan.getCodigo(), x.corte.getCodigo(), null, LocalTime.of(11, 0)));
        verificar("HU-CIT-01", 2, "Datos incompletos",
                "Falta indicar el dato: servicio".equals(sinServicio) && "Falta indicar el dato: fecha".equals(sinFecha),
                "Sin servicio -> '" + sinServicio + "' | Sin fecha -> '" + sinFecha + "'");

        String ayer = error(() -> x.cita(AYER, 10, 0));
        String horaPasada = error(() -> x.cita(HOY, 7, 0));
        verificar("HU-CIT-01", 3, "Fecha pasada",
                "No se puede agendar en una fecha pasada".equals(ayer) && "No se puede agendar en una fecha pasada".equals(horaPasada),
                "Ayer -> '" + ayer + "' | Hoy 07:00 (ya pasó) -> '" + horaPasada + "'");
    }

    // ---------------- REQ-CIT-02 ----------------
    private static void pruebasSinCruces() throws Exception {
        Contexto x = new Contexto();
        x.cita(MANANA, 10, 0);   // ocupa 10:00 - 10:30

        Cita libre = x.cita(MANANA, 11, 0);
        verificar("HU-CIT-02", 1, "Horario libre",
                libre.getEstado() == EstadoCita.PENDIENTE, "Cita a las 11:00 agendada: " + libre);

        String cruce = error(() -> x.cita(MANANA, 10, 15));
        String cruceBarba = error(() -> x.agenda.agendar(x.jose.getCodigo(), x.alan.getCodigo(), x.barba.getCodigo(), MANANA, LocalTime.of(9, 50)));
        verificar("HU-CIT-02", 2, "Cruce de horarios",
                "El barbero ya tiene una cita en ese horario".equals(cruce) && "El barbero ya tiene una cita en ese horario".equals(cruceBarba),
                "Corte 10:15 -> '" + cruce + "' | Barba (20 min) 09:50 -> '" + cruceBarba + "'");

        Cita consecutiva = x.cita(MANANA, 10, 30);
        verificar("HU-CIT-02", 3, "Citas consecutivas",
                consecutiva.getHoraInicio().equals(LocalTime.of(10, 30)),
                "La cita de 10:30 empieza justo cuando termina la de 10:00 y se permite: " + consecutiva);
    }

    // ---------------- REQ-CIT-03 ----------------
    private static void pruebasHorarios() throws Exception {
        Contexto x = new Contexto();
        x.cita(MANANA, 10, 0);

        List<LocalTime> libres = x.agenda.horariosDisponibles(x.alan.getCodigo(), MANANA, x.corte.getCodigo());
        verificar("HU-CIT-03", 1, "Barbero con espacios libres",
                libres.contains(LocalTime.of(9, 0)) && libres.contains(LocalTime.of(9, 30))
                        && !libres.contains(LocalTime.of(10, 0)) && libres.contains(LocalTime.of(10, 30))
                        && libres.contains(LocalTime.of(18, 30)) && !libres.contains(LocalTime.of(19, 0)),
                "Corte (30 min) con una cita a las 10:00: " + libres.size() + " horarios libres, empezando en "
                        + libres.subList(0, 3) + " (el de las 10:00 no aparece)");

        Barbero luis = x.barberos.registrar("Luis Díaz", "955555555", LocalTime.of(9, 0), LocalTime.of(10, 0));
        x.agenda.agendar(x.jose.getCodigo(), luis.getCodigo(), x.corte.getCodigo(), MANANA, LocalTime.of(9, 0));
        x.agenda.agendar(x.jose.getCodigo(), luis.getCodigo(), x.corte.getCodigo(), MANANA, LocalTime.of(9, 30));
        List<LocalTime> ninguno = x.agenda.horariosDisponibles(luis.getCodigo(), MANANA, x.corte.getCodigo());
        verificar("HU-CIT-03", 2, "Día completo",
                ninguno.isEmpty(), "Barbero con horario 09:00-10:00 y dos cortes agendados -> sin horarios -> 'No hay horarios disponibles en esta fecha'");

        String pasada = error(() -> x.agenda.horariosDisponibles(x.alan.getCodigo(), AYER, x.corte.getCodigo()));
        verificar("HU-CIT-03", 3, "Fecha pasada",
                "No se pueden consultar horarios de fechas pasadas".equals(pasada), "Mensaje: " + pasada);
    }

    // ---------------- REQ-CIT-06 ----------------
    private static void pruebasMarcarCita() throws Exception {
        Contexto x = new Contexto();
        Cita a = x.cita(HOY, 10, 0);
        Cita b = x.cita(HOY, 11, 0);
        Cita futura = x.cita(MANANA, 10, 0);

        x.agenda.marcarAtendida(a.getCodigo());
        verificar("HU-CIT-06", 1, "Cita atendida",
                a.getEstado() == EstadoCita.ATENDIDA, "La cita " + a.getCodigo() + " quedó: " + a.getEstado());

        x.agenda.marcarInasistencia(b.getCodigo());
        verificar("HU-CIT-06", 2, "Inasistencia",
                b.getEstado() == EstadoCita.INASISTENCIA, "La cita " + b.getCodigo() + " quedó: " + b.getEstado());

        String mensaje = error(() -> x.agenda.marcarAtendida(futura.getCodigo()));
        verificar("HU-CIT-06", 3, "Cita futura",
                "No se puede cambiar el estado de una cita futura".equals(mensaje) && futura.getEstado() == EstadoCita.PENDIENTE,
                "Mensaje: " + mensaje + " (la cita sigue " + futura.getEstado() + ")");
    }

    // ---------------- REQ-BAR-03 ----------------
    private static void pruebasBarberoDeLaAtencion() throws Exception {
        Contexto x = new Contexto();
        Cita cita = x.cita(HOY, 10, 0);
        x.agenda.marcarAtendida(cita.getCodigo());

        Atencion conCita = x.atenciones.registrarDesdeCita(cita.getCodigo(), null);
        verificar("HU-BAR-03", 1, "Atención con cita",
                conCita.getBarbero() == x.alan && conCita.getCita() == cita, "Quedó asociada al barbero de la cita -> " + conCita);

        String sinBarbero = error(() -> x.atenciones.registrarSinCita(x.jose.getCodigo(), "", Arrays.asList(x.corte.getCodigo())));
        Atencion sinCita = x.atenciones.registrarSinCita(x.jose.getCodigo(), x.alan.getCodigo(), Arrays.asList(x.corte.getCodigo(), x.barba.getCodigo()));
        verificar("HU-BAR-03", 2, "Atención sin cita",
                "Seleccione el barbero que atendió".equals(sinBarbero) && sinCita.getCita() == null && sinCita.getTotal() == 40,
                "Sin barbero -> '" + sinBarbero + "' | Con barbero -> " + sinCita);

        Barbero luis = x.barberos.registrar("Luis Díaz", "955555555", LocalTime.of(9, 0), LocalTime.of(19, 0));
        Atencion cambiada = x.atenciones.cambiarBarbero(conCita.getCodigo(), luis.getCodigo());
        x.caja.pagar(conCita.getCodigo(), x.caja.crearMetodo("efectivo"));
        String tardia = error(() -> x.atenciones.cambiarBarbero(conCita.getCodigo(), x.alan.getCodigo()));
        verificar("HU-BAR-03", 3, "Cambio de barbero",
                cambiada.getBarbero() == luis && "No se puede cambiar el barbero de una atención ya pagada".equals(tardia),
                "La cita era de Alan pero atendió Luis -> " + cambiada.getBarbero().getNombres()
                        + " | Después de pagar -> '" + tardia + "'");
    }

    // ---------------- REQ-CAJ-01 ----------------
    private static void pruebasPago() throws Exception {
        Contexto x = new Contexto();
        Atencion a = x.atenciones.registrarSinCita(x.jose.getCodigo(), x.alan.getCodigo(), Arrays.asList(x.corte.getCodigo()));
        Atencion b = x.atenciones.registrarSinCita(x.jose.getCodigo(), x.alan.getCodigo(), Arrays.asList(x.barba.getCodigo()));

        x.caja.pagar(a.getCodigo(), x.caja.crearMetodo("yape"));
        verificar("HU-CAJ-01", 1, "Pago exitoso",
                a.isPagada() && a.getMetodoPago().getMetodo().equals("Yape") && a.getMontoPagado() == 25 && a.getFechaPago() != null,
                "Quedó: " + a + " | fecha de pago: " + a.getFechaPago());

        String sinMetodo = error(() -> x.caja.pagar(b.getCodigo(), x.caja.crearMetodo("")));
        verificar("HU-CAJ-01", 2, "Sin método de pago",
                "Seleccione el método de pago".equals(sinMetodo) && !b.isPagada(),
                "Mensaje: " + sinMetodo + " (la atención sigue sin pagar)");

        String repetido = error(() -> x.caja.pagar(a.getCodigo(), x.caja.crearMetodo("efectivo")));
        verificar("HU-CAJ-01", 3, "Atención ya pagada",
                "La atención ya está pagada".equals(repetido) && a.getMetodoPago().getMetodo().equals("Yape"),
                "Mensaje: " + repetido + " (conserva su pago original con Yape)");
    }
}
