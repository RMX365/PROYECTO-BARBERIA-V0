package barberia.servicio;

import barberia.excepciones.DatoInvalidoException;
import barberia.excepciones.OperacionNoPermitidaException;
import barberia.modelo.Atencion;
import barberia.modelo.Efectivo;
import barberia.modelo.Pagable;
import barberia.modelo.Tarjeta;
import barberia.modelo.Yape;
import barberia.repositorio.AtencionRepositorio;
import barberia.util.Validador;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Caja: REQ-CAJ-01 (registrar el pago de una atención con su método de pago).
 */
public class CajaService {

    private final AtencionRepositorio atenciones;
    private final Clock reloj;

    public CajaService(AtencionRepositorio atenciones, Clock reloj) {
        this.atenciones = atenciones;
        this.reloj = reloj;
    }

    /** Crea el método de pago a partir de lo que elige el usuario (1/efectivo, 2/yape, 3/tarjeta). */
    public Pagable crearMetodo(String texto) {
        if (texto == null) {
            return null;
        }
        switch (texto.trim().toLowerCase()) {
            case "1":
            case "efectivo":
                return new Efectivo();
            case "2":
            case "yape":
                return new Yape();
            case "3":
            case "tarjeta":
                return new Tarjeta();
            default:
                return null;
        }
    }

    /** REQ-CAJ-01. */
    public Atencion pagar(String codigoAtencion, Pagable metodo)
            throws DatoInvalidoException, OperacionNoPermitidaException {
        Validador.validarObligatorio(codigoAtencion, "atención");
        String codigo = codigoAtencion.trim().toUpperCase();
        Atencion atencion = atenciones.buscarPorCodigo(codigo)
                .orElseThrow(() -> new DatoInvalidoException("No existe la atención " + codigo));

        if (atencion.isPagada()) {
            throw new OperacionNoPermitidaException("La atención ya está pagada");
        }
        if (metodo == null) {
            throw new DatoInvalidoException("Seleccione el método de pago");
        }
        if (!metodo.pagar(atencion.calcularTotal())) {
            throw new OperacionNoPermitidaException("No se pudo procesar el pago");
        }
        atencion.registrarPago(metodo, LocalDateTime.now(reloj));
        atencion.getCliente().registrarVisita();   // REQ-CLI-07: cada atención pagada suma una visita
        atenciones.guardar(atencion);
        return atencion;
    }
}
