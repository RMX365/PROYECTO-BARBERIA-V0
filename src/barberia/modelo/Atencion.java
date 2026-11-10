package barberia.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Atención realizada a un cliente: qué servicios recibió, quién lo atendió y cómo pagó.
 * Puede venir de una cita o ser una atención sin cita.
 */
public class Atencion {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final String codigo;
    private final Cliente cliente;
    private Barbero barbero;
    private final Cita cita;                 // null si la atención no tuvo cita
    private final List<Servicio> servicios;
    private final LocalDate fecha;
    private final double subtotal;           // se fija al registrar la atención
    private double montoDescuento;
    private double total;
    private boolean pagada;
    private Pagable metodoPago;
    private LocalDateTime fechaPago;
    private double montoPagado;

    public Atencion(String codigo, Cliente cliente, Barbero barbero, Cita cita, List<Servicio> servicios,
                    LocalDate fecha, double subtotal) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.barbero = barbero;
        this.cita = cita;
        this.servicios = new ArrayList<>(servicios);
        this.fecha = fecha;
        this.subtotal = subtotal;
        this.montoDescuento = 0;             // los descuentos se agregan en la semana 13
        calcularTotal();
    }

    /** El total es el subtotal menos el descuento. */
    public double calcularTotal() {
        total = subtotal - montoDescuento;
        return total;
    }

    public void cambiarBarbero(Barbero nuevo) {
        this.barbero = nuevo;
    }

    public void registrarPago(Pagable metodo, LocalDateTime momento) {
        this.metodoPago = metodo;
        this.fechaPago = momento;
        this.montoPagado = total;
        this.pagada = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Barbero getBarbero() {
        return barbero;
    }

    public Cita getCita() {
        return cita;
    }

    public List<Servicio> getServicios() {
        return new ArrayList<>(servicios);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTotal() {
        return total;
    }

    public boolean isPagada() {
        return pagada;
    }

    public Pagable getMetodoPago() {
        return metodoPago;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    private String nombresServicios() {
        StringBuilder nombres = new StringBuilder();
        for (Servicio s : servicios) {
            if (nombres.length() > 0) {
                nombres.append(" + ");
            }
            nombres.append(s.getNombre());
        }
        return nombres.toString();
    }

    /** Línea para el historial del cliente: fecha, servicios, barbero y monto. */
    public String resumenHistorial() {
        return String.format(Locale.US, "%s | %s | Barbero: %s | S/ %.2f",
                fecha.format(FORMATO_FECHA), nombresServicios(), barbero.getNombres(), montoPagado);
    }

    @Override
    public String toString() {
        String estadoPago = pagada
                ? String.format(Locale.US, "Pagada con %s (S/ %.2f)", metodoPago.getMetodo(), montoPagado)
                : "Pendiente de pago";
        return String.format(Locale.US, "%s | %s | %s | Barbero: %s | %s | Total: S/ %.2f | %s",
                codigo, fecha.format(FORMATO_FECHA), cliente.getNombres(), barbero.getNombres(),
                nombresServicios(), total, estadoPago);
    }
}
