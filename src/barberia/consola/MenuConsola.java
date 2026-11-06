package barberia.consola;

import barberia.excepciones.CelularDuplicadoException;
import barberia.excepciones.ClienteNoEncontradoException;
import barberia.excepciones.DatoInvalidoException;
import barberia.excepciones.ServicioDuplicadoException;
import barberia.modelo.Barbero;
import barberia.modelo.Cliente;
import barberia.modelo.Servicio;
import barberia.repositorio.BarberoRepositorioMemoria;
import barberia.repositorio.ClienteRepositorioMemoria;
import barberia.repositorio.ServicioRepositorioMemoria;
import barberia.servicio.BarberoService;
import barberia.servicio.ClienteService;
import barberia.servicio.ServicioService;
import barberia.servicio.TotalesAtencion;
import barberia.util.Validador;
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
    private final ClienteService clienteService = new ClienteService(new ClienteRepositorioMemoria());
    private final BarberoService barberoService = new BarberoService(new BarberoRepositorioMemoria());
    private final ServicioService servicioService = new ServicioService(new ServicioRepositorioMemoria());

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
                case 0 -> System.out.println("Hasta pronto.");
                default -> System.out.println("[ERROR] Opción no válida");
            }
        } while (opcion != 0);
    }

    private void mostrarMenu() {
        System.out.println("\n===== BARBERÍA =====");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Editar cliente");
        System.out.println("3. Buscar cliente");
        System.out.println("4. Registrar barbero");
        System.out.println("5. Registrar servicio");
        System.out.println("6. Ver catálogo de servicios");
        System.out.println("7. Crear combo");
        System.out.println("8. Calcular precio y duración de una atención");
        System.out.println("0. Salir");
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
