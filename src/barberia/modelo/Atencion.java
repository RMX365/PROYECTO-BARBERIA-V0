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
    private double subtotal;
    private double montoDescuento;
    private double total;
    private boolean pagada;
    private Pagable metodoPago;
    private LocalDateTime fechaPago;
    private double montoPagado;

    public Atencion(String codigo, Cliente cliente, Barbero barbero, Cita cita, List<Servicio> servicios, LocalDate fecha) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.barbero = barbero;
        this.cita = cita;
        this.servicios = new ArrayList<>(servicios);
        this.fecha = fecha;
        this.montoDescuento = 0;             // los descuentos se agregan en la semana 13
        calcularTotal();
    }

    /** Calcula el subtotal (suma de precios) y el total (subtotal menos descuento). */
    public double calcularTotal() {
        subtotal = 0;
        for (Servicio s : servicios) {
            subtotal += s.getPrecio();
        }
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

    @Override
    public String toString() {
        StringBuilder nombres = new StringBuilder();
        for (Servicio s : servicios) {
            if (nombres.length() > 0) {
                nombres.append(" + ");
            }
            nombres.append(s.getNombre());
        }
        String estadoPago = pagada
                ? String.format(Locale.US, "Pagada con %s (S/ %.2f)", metodoPago.getMetodo(), montoPagado)
                : "Pendiente de pago";
        return String.format(Locale.US, "%s | %s | %s | Barbero: %s | %s | Total: S/ %.2f | %s",
                codigo, fecha.format(FORMATO_FECHA), cliente.getNombres(), barbero.getNombres(),
                nombres, total, estadoPago);
    }
}
