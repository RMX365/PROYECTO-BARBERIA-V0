package barberia.consola;

import barberia.excepciones.CelularDuplicadoException;
import barberia.excepciones.ClienteNoEncontradoException;
import barberia.excepciones.DatoInvalidoException;
import barberia.excepciones.OperacionNoPermitidaException;
import barberia.excepciones.ServicioDuplicadoException;
import barberia.modelo.Atencion;
import barberia.modelo.Barbero;
import barberia.modelo.Cita;
import barberia.modelo.Cliente;
import barberia.modelo.Servicio;
import barberia.repositorio.AtencionRepositorio;
import barberia.repositorio.AtencionRepositorioMemoria;
import barberia.repositorio.BarberoRepositorioMemoria;
import barberia.repositorio.CitaRepositorio;
import barberia.repositorio.CitaRepositorioMemoria;
import barberia.repositorio.ClienteRepositorioMemoria;
import barberia.repositorio.ServicioRepositorioMemoria;
import barberia.servicio.AgendaService;
import barberia.servicio.AtencionService;
import barberia.servicio.BarberoService;
import barberia.servicio.CajaService;
import barberia.servicio.ClienteService;
import barberia.servicio.ServicioService;
import barberia.servicio.TotalesAtencion;
import barberia.util.Validador;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Menú de consola para probar los requerimientos implementados.
 */
public class MenuConsola {

    private final Scanner teclado = new Scanner(System.in);
    private final Clock reloj = Clock.systemDefaultZone();

    private final ClienteService clienteService = new ClienteService(new ClienteRepositorioMemoria());
    private final BarberoService barberoService = new BarberoService(new BarberoRepositorioMemoria());
    private final ServicioService servicioService = new ServicioService(new ServicioRepositorioMemoria());

    private final CitaRepositorio citaRepositorio = new CitaRepositorioMemoria();
    private final AtencionRepositorio atencionRepositorio = new AtencionRepositorioMemoria();
    private final AgendaService agendaService =
            new AgendaService(citaRepositorio, clienteService, barberoService, servicioService, reloj);
    private final AtencionService atencionService =
            new AtencionService(atencionRepositorio, citaRepositorio, clienteService, barberoService, servicioService, reloj);
    private final CajaService cajaService = new CajaService(atencionRepositorio, reloj);

    public void iniciar() {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerOpcion();
            switch (opcion) {
                case 1 -> registrarCliente();
                case 2 -> editarCliente();
                case 3 -> buscarCliente();
                case 4 -> registrarBarbero();
                case 5 -> registrarServicio();
                case 6 -> verCatalogo();
                case 7 -> crearCombo();
                case 8 -> calcularAtencion();
                case 9 -> agendarCita();
                case 10 -> verHorarios();
                case 11 -> marcarCita();
                case 12 -> registrarAtencion();
                case 13 -> registrarPago();
                case 0 -> System.out.println("Hasta pronto.");
                default -> System.out.println("[ERROR] Opción no válida");
            }
        } while (opcion != 0);
    }

    private void mostrarMenu() {
        System.out.println("\n===== BARBERÍA =====");
        System.out.println("-- Clientes y barberos --");
        System.out.println(" 1. Registrar cliente");
        System.out.println(" 2. Editar cliente");
        System.out.println(" 3. Buscar cliente");
        System.out.println(" 4. Registrar barbero");
        System.out.println("-- Servicios --");
        System.out.println(" 5. Registrar servicio");
        System.out.println(" 6. Ver catálogo de servicios");
        System.out.println(" 7. Crear combo");
        System.out.println(" 8. Calcular precio y duración de una atención");
        System.out.println("-- Citas --");
        System.out.println(" 9. Agendar cita");
        System.out.println("10. Ver horarios disponibles");
        System.out.println("11. Marcar cita (atendida / inasistencia)");
        System.out.println("-- Atención y caja --");
        System.out.println("12. Registrar atención");
        System.out.println("13. Registrar pago");
        System.out.println(" 0. Salir");
        System.out.print("Elija una opción: ");
    }

    private int leerOpcion() {
        if (!teclado.hasNextLine()) {
            return 0;
        }
        String linea = teclado.nextLine().trim();
        try {
            return Integer.parseInt(linea);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String pedir(String mensaje) {
        System.out.print(mensaje);
        return teclado.hasNextLine() ? teclado.nextLine() : "";
    }

    // ---------------- CLIENTES ----------------
    private void registrarCliente() {
        System.out.println("\n--- Registrar cliente ---");
        String nombres = pedir("Nombres: ");
        String celular = pedir("Celular: ");
        try {
            Cliente c = clienteService.registrar(nombres, celular);
            System.out.println("[OK] Cliente registrado correctamente: " + c);
        } catch (DatoInvalidoException | CelularDuplicadoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void editarCliente() {
        System.out.println("\n--- Editar cliente ---");
        String codigo = pedir("Código del cliente (ej. CLI-001): ");
        String nombres = pedir("Nuevos nombres: ");
        String celular = pedir("Nuevo celular: ");
        try {
            Cliente c = clienteService.editar(codigo, nombres, celular);
            System.out.println("[OK] Cliente actualizado: " + c);
        } catch (DatoInvalidoException | CelularDuplicadoException | ClienteNoEncontradoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void buscarCliente() {
        System.out.println("\n--- Buscar cliente ---");
        String texto = pedir("Nombre o celular: ");
        try {
            List<Cliente> encontrados = clienteService.buscar(texto);
            if (encontrados.isEmpty()) {
                System.out.println("No se encontraron clientes");
            } else {
                System.out.println("Se encontraron " + encontrados.size() + " cliente(s):");
                for (Cliente c : encontrados) {
                    System.out.println("  " + c);
                }
            }
        } catch (DatoInvalidoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // ---------------- BARBEROS ----------------
    private void registrarBarbero() {
        System.out.println("\n--- Registrar barbero ---");
        String nombres = pedir("Nombres: ");
        String celular = pedir("Celular: ");
        String inicio = pedir("Hora de inicio (HH:mm): ");
        String fin = pedir("Hora de fin (HH:mm): ");
        try {
            LocalTime horaInicio = Validador.parsearHora(inicio);
            LocalTime horaFin = Validador.parsearHora(fin);
            Barbero b = barberoService.registrar(nombres, celular, horaInicio, horaFin);
            System.out.println("[OK] Barbero registrado correctamente: " + b);
        } catch (DatoInvalidoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // ---------------- SERVICIOS ----------------
    private void registrarServicio() {
        System.out.println("\n--- Registrar servicio ---");
        String tipo = pedir("Tipo (corte, barba, tinte o cejas): ");
        String nombre = pedir("Nombre del servicio: ");
        String precioTexto = pedir("Precio (S/): ");
        String duracionTexto = pedir("Duración (minutos): ");
        try {
            double precio = Validador.parsearPrecio(precioTexto);
            int duracion = Validador.parsearDuracion(duracionTexto);
            Servicio s = servicioService.registrar(tipo, nombre, precio, duracion);
            System.out.println("[OK] Servicio registrado correctamente: " + s);
        } catch (DatoInvalidoException | ServicioDuplicadoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void verCatalogo() {
        System.out.println("\n--- Catálogo de servicios ---");
        List<Servicio> catalogo = servicioService.catalogoActivos();
        if (catalogo.isEmpty()) {
            System.out.println("No hay servicios registrados");
        } else {
            for (Servicio s : catalogo) {
                System.out.println("  " + s);
            }
        }
    }

    private void crearCombo() {
        System.out.println("\n--- Crear combo ---");
        String nombre = pedir("Nombre del combo: ");
        String precioTexto = pedir("Precio del combo (S/): ");
        String codigosTexto = pedir("Códigos de los servicios separados por coma (ej. SER-001, SER-002): ");
        try {
            double precio = Validador.parsearPrecio(precioTexto);
            List<String> codigos = separarCodigos(codigosTexto);
            Servicio combo = servicioService.crearCombo(nombre, precio, codigos);
            System.out.println("[OK] Combo creado correctamente: " + combo);
        } catch (DatoInvalidoException | ServicioDuplicadoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void calcularAtencion() {
        System.out.println("\n--- Calcular precio y duración ---");
        String codigosTexto = pedir("Códigos de los servicios separados por coma: ");
        try {
            List<Servicio> elegidos = new ArrayList<>();
            for (String codigo : separarCodigos(codigosTexto)) {
                elegidos.add(servicioService.obtenerActivo(codigo));
            }
            if (elegidos.isEmpty()) {
                throw new DatoInvalidoException("Seleccione al menos un servicio");
            }
            TotalesAtencion totales = servicioService.calcularTotales(elegidos);
            for (Servicio s : elegidos) {
                System.out.println("  " + s);
            }
            System.out.println(String.format(Locale.US, "[OK] Total: S/ %.2f | Duración: %d min",
                    totales.getPrecioTotal(), totales.getDuracionTotal()));
        } catch (DatoInvalidoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // ---------------- CITAS ----------------
    private void agendarCita() {
        System.out.println("\n--- Agendar cita ---");
        String cliente = pedir("Código del cliente (ej. CLI-001): ");
        String barbero = pedir("Código del barbero (ej. BAR-001): ");
        String servicio = pedir("Código del servicio (ej. SER-001): ");
        String fechaTexto = pedir("Fecha (dd/MM/aaaa): ");
        String horaTexto = pedir("Hora (HH:mm): ");
        try {
            LocalDate fecha = fechaTexto.isBlank() ? null : Validador.parsearFecha(fechaTexto);
            LocalTime hora = horaTexto.isBlank() ? null : Validador.parsearHora(horaTexto);
            Cita cita = agendaService.agendar(cliente, barbero, servicio, fecha, hora);
            System.out.println("[OK] Cita agendada correctamente: " + cita);
        } catch (DatoInvalidoException | OperacionNoPermitidaException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void verHorarios() {
        System.out.println("\n--- Horarios disponibles ---");
        String barbero = pedir("Código del barbero (ej. BAR-001): ");
        String fechaTexto = pedir("Fecha (dd/MM/aaaa): ");
        String servicio = pedir("Código del servicio (ej. SER-001): ");
        try {
            LocalDate fecha = fechaTexto.isBlank() ? null : Validador.parsearFecha(fechaTexto);
            List<LocalTime> libres = agendaService.horariosDisponibles(barbero, fecha, servicio);
            if (libres.isEmpty()) {
                System.out.println("No hay horarios disponibles en esta fecha");
            } else {
                System.out.println("Horarios libres (hora de inicio):");
                StringBuilder fila = new StringBuilder("  ");
                int contador = 0;
                for (LocalTime h : libres) {
                    fila.append(h).append("  ");
                    contador++;
                    if (contador % 6 == 0) {
                        System.out.println(fila);
                        fila = new StringBuilder("  ");
                    }
                }
                if (contador % 6 != 0) {
                    System.out.println(fila);
                }
            }
        } catch (DatoInvalidoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void marcarCita() {
        System.out.println("\n--- Marcar cita ---");
        String codigo = pedir("Código de la cita (ej. CIT-001): ");
        String opcion = pedir("1 = Atendida, 2 = Inasistencia: ");
        try {
            Cita cita;
            if (opcion.trim().equals("1")) {
                cita = agendaService.marcarAtendida(codigo);
                System.out.println("[OK] Cita marcada como atendida: " + cita);
                System.out.println("     Ya puede registrar su atención (opción 12) y cobrarla (opción 13).");
            } else if (opcion.trim().equals("2")) {
                cita = agendaService.marcarInasistencia(codigo);
                System.out.println("[OK] Cita marcada como inasistencia: " + cita);
            } else {
                System.out.println("[ERROR] Elija 1 o 2");
            }
        } catch (DatoInvalidoException | OperacionNoPermitidaException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // ---------------- ATENCIÓN Y CAJA ----------------
    private void registrarAtencion() {
        System.out.println("\n--- Registrar atención ---");
        String conCita = pedir("¿La atención tiene cita? (s/n): ");
        try {
            Atencion atencion;
            if (conCita.trim().equalsIgnoreCase("s")) {
                String cita = pedir("Código de la cita (ej. CIT-001): ");
                String barbero = pedir("Código del barbero que atendió (Enter = el de la cita): ");
                atencion = atencionService.registrarDesdeCita(cita, barbero);
            } else {
                String cliente = pedir("Código del cliente (ej. CLI-001): ");
                String barbero = pedir("Código del barbero que atendió: ");
                String servicios = pedir("Códigos de los servicios separados por coma: ");
                atencion = atencionService.registrarSinCita(cliente, barbero, separarCodigos(servicios));
            }
            System.out.println("[OK] Atención registrada: " + atencion);
        } catch (DatoInvalidoException | OperacionNoPermitidaException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void registrarPago() {
        System.out.println("\n--- Registrar pago ---");
        String codigo = pedir("Código de la atención (ej. ATE-001): ");
        String metodo = pedir("Método de pago (1 = Efectivo, 2 = Yape, 3 = Tarjeta): ");
        try {
            Atencion atencion = cajaService.pagar(codigo, cajaService.crearMetodo(metodo));
            System.out.println("[OK] Pago registrado: " + atencion);
        } catch (DatoInvalidoException | OperacionNoPermitidaException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private List<String> separarCodigos(String texto) {
        List<String> codigos = new ArrayList<>();
        for (String parte : texto.split(",")) {
            if (!parte.isBlank()) {
                codigos.add(parte.trim());
            }
        }
        return codigos;
    }
}
