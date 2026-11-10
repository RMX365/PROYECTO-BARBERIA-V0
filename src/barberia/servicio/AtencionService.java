package barberia.servicio;

import barberia.excepciones.DatoInvalidoException;
import barberia.excepciones.OperacionNoPermitidaException;
import barberia.modelo.Atencion;
import barberia.modelo.Barbero;
import barberia.modelo.Cita;
import barberia.modelo.Cliente;
import barberia.modelo.EstadoCita;
import barberia.modelo.Servicio;
import barberia.repositorio.AtencionRepositorio;
import barberia.repositorio.CitaRepositorio;
import barberia.util.Validador;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Registro de atenciones y del barbero que las realizó: REQ-BAR-03.
 */
public class AtencionService {

    private final AtencionRepositorio atenciones;
    private final CitaRepositorio citas;
    private final ClienteService clientes;
    private final BarberoService barberos;
    private final ServicioService servicios;
    private final Clock reloj;

    public AtencionService(AtencionRepositorio atenciones, CitaRepositorio citas, ClienteService clientes,
                           BarberoService barberos, ServicioService servicios, Clock reloj) {
        this.atenciones = atenciones;
        this.citas = citas;
        this.clientes = clientes;
        this.barberos = barberos;
        this.servicios = servicios;
        this.reloj = reloj;
    }

    /** Atención que viene de una cita ya atendida. Si no se indica barbero, queda el de la cita. */
    public Atencion registrarDesdeCita(String codigoCita, String codigoBarberoReal)
            throws DatoInvalidoException, OperacionNoPermitidaException {
        Validador.validarObligatorio(codigoCita, "cita");
        String codigo = codigoCita.trim().toUpperCase();
        Cita cita = citas.buscarPorCodigo(codigo)
                .orElseThrow(() -> new DatoInvalidoException("No existe la cita " + codigo));
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw new OperacionNoPermitidaException("La cita debe estar marcada como atendida");
        }
        if (atenciones.buscarPorCita(cita.getCodigo()).isPresent()) {
            throw new OperacionNoPermitidaException("La cita ya tiene una atención registrada");
        }

        Barbero barbero = cita.getBarbero();
        if (codigoBarberoReal != null && !codigoBarberoReal.isBlank()) {
            barbero = barberos.obtenerActivo(codigoBarberoReal);
        }
        List<Servicio> lista = new ArrayList<>();
        lista.add(cita.getServicio());

        // el precio es el que tenía el servicio cuando se agendó la cita
        Atencion nueva = new Atencion(atenciones.siguienteCodigo(), cita.getCliente(), barbero, cita, lista,
                cita.getFecha(), cita.getPrecio());
        atenciones.guardar(nueva);
        return nueva;
    }

    /** Atención de un cliente que llegó sin cita: es obligatorio indicar qué barbero lo atendió. */
    public Atencion registrarSinCita(String codigoCliente, String codigoBarbero, List<String> codigosServicios)
            throws DatoInvalidoException {
        Validador.validarObligatorio(codigoCliente, "cliente");
        if (codigoBarbero == null || codigoBarbero.isBlank()) {
            throw new DatoInvalidoException("Seleccione el barbero que atendió");
        }
        if (codigosServicios == null || codigosServicios.isEmpty()) {
            throw new DatoInvalidoException("Seleccione al menos un servicio");
        }

        Cliente cliente = clientes.obtenerActivo(codigoCliente);
        Barbero barbero = barberos.obtenerActivo(codigoBarbero);
        List<Servicio> lista = new ArrayList<>();
        double subtotal = 0;
        for (String c : codigosServicios) {
            Servicio servicio = servicios.obtenerActivo(c);
            lista.add(servicio);
            subtotal += servicio.getPrecio();
        }

        Atencion nueva = new Atencion(atenciones.siguienteCodigo(), cliente, barbero, null, lista,
                LocalDate.now(reloj), subtotal);
        atenciones.guardar(nueva);
        return nueva;
    }

    /** Cambia el barbero de una atención, siempre que todavía no esté pagada. */
    public Atencion cambiarBarbero(String codigoAtencion, String codigoBarbero)
            throws DatoInvalidoException, OperacionNoPermitidaException {
        Atencion atencion = obtener(codigoAtencion);
        if (atencion.isPagada()) {
            throw new OperacionNoPermitidaException("No se puede cambiar el barbero de una atención ya pagada");
        }
        Validador.validarObligatorio(codigoBarbero, "barbero");
        atencion.cambiarBarbero(barberos.obtenerActivo(codigoBarbero));
        atenciones.guardar(atencion);
        return atencion;
    }

    public Atencion obtener(String codigoAtencion) throws DatoInvalidoException {
        Validador.validarObligatorio(codigoAtencion, "atención");
        String codigo = codigoAtencion.trim().toUpperCase();
        return atenciones.buscarPorCodigo(codigo)
                .orElseThrow(() -> new DatoInvalidoException("No existe la atención " + codigo));
    }

    /** REQ-CLI-06: atenciones pagadas de un cliente, de la más reciente a la más antigua. */
    public List<Atencion> historialDeCliente(String codigoCliente) throws DatoInvalidoException {
        Cliente cliente = clientes.obtener(codigoCliente);
        List<Atencion> historial = new ArrayList<>();
        for (Atencion a : atenciones.buscarPorCliente(cliente.getCodigo())) {
            if (a.isPagada()) {
                historial.add(a);
            }
        }
        historial.sort(Comparator.comparing(Atencion::getFechaPago)
                .thenComparing(Atencion::getCodigo).reversed());
        return historial;
    }
}
