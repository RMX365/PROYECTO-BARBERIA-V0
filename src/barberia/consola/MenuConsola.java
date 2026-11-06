package barberia.consola;

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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Menú de consola para probar los requerimientos implementados.
 */
public class MenuConsola {

    private final Scanner teclado = new Scanner(System.in);
    private final ClienteService clienteService = new ClienteService(new ClienteRepositorioMemoria());
    private final BarberoService barberoService = new BarberoService(new BarberoRepositorioMemoria());

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

    private void registrarBarbero() {
        System.out.println("\n--- Registrar barbero ---");
        String nombres = pedir("Nombres: ");
        String celular = pedir("Celular: ");
        String inicio = pedir("Hora de inicio (HH:mm): ");
        String fin = pedir("Hora de fin (HH:mm): ");
        try {
            LocalTime horaInicio = LocalTime.parse(inicio.trim());
            LocalTime horaFin = LocalTime.parse(fin.trim());
            Barbero b = barberoService.registrar(nombres, celular, horaInicio, horaFin);
            System.out.println("[OK] Barbero registrado correctamente: " + b);
        } catch (DateTimeParseException e) {
            System.out.println("[ERROR] Use el formato HH:mm para las horas (ejemplo 09:00)");
        } catch (DatoInvalidoException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}
