package barberia.servicio;

import barberia.excepciones.DatoInvalidoException;
import barberia.excepciones.ServicioDuplicadoException;
import barberia.modelo.Barba;
import barberia.modelo.Cejas;
import barberia.modelo.Combo;
import barberia.modelo.Corte;
import barberia.modelo.Servicio;
import barberia.modelo.Tinte;
import barberia.repositorio.ServicioRepositorio;
import barberia.util.Validador;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Lógica de negocio de los servicios:
 * REQ-SER-01 (registrar), REQ-SER-03 (catálogo), REQ-SER-05 (combos) y REQ-SER-06 (totales).
 */
public class ServicioService {

    private final ServicioRepositorio repositorio;

    public ServicioService(ServicioRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** REQ-SER-01: registra un servicio simple según su tipo. */
    public Servicio registrar(String tipo, String nombre, double precio, int duracionMin)
            throws DatoInvalidoException, ServicioDuplicadoException {
        String t = (tipo == null) ? "" : tipo.trim().toLowerCase();
        if (!t.equals("corte") && !t.equals("barba") && !t.equals("tinte") && !t.equals("cejas")) {
            throw new DatoInvalidoException("Tipo de servicio inválido (use corte, barba, tinte o cejas)");
        }
        Validador.validarNombreServicio(nombre);
        Validador.validarPrecio(precio);
        Validador.validarDuracion(duracionMin);
        nombre = nombre.trim();
        verificarNombreLibre(nombre);

        String codigo = repositorio.siguienteCodigo();
        Servicio nuevo;
        switch (t) {
            case "corte" -> nuevo = new Corte(codigo, nombre, precio, duracionMin);
            case "barba" -> nuevo = new Barba(codigo, nombre, precio, duracionMin);
            case "tinte" -> nuevo = new Tinte(codigo, nombre, precio, duracionMin);
            default -> nuevo = new Cejas(codigo, nombre, precio, duracionMin);
        }
        repositorio.guardar(nuevo);
        return nuevo;
    }

    /** REQ-SER-03: servicios y combos activos. */
    public List<Servicio> catalogoActivos() {
        List<Servicio> activos = new ArrayList<>();
        for (Servicio s : repositorio.obtenerTodos()) {
            if (s.isActivo()) {
                activos.add(s);
            }
        }
        return activos;
    }

    /** REQ-SER-05: crea un combo con dos o más servicios simples activos y precio propio. */
    public Combo crearCombo(String nombre, double precio, List<String> codigos)
            throws DatoInvalidoException, ServicioDuplicadoException {
        Validador.validarNombreServicio(nombre);
        Validador.validarPrecio(precio);

        Set<String> unicos = new LinkedHashSet<>();
        for (String c : codigos) {
            unicos.add(c.trim().toUpperCase());
        }
        if (unicos.size() < 2) {
            throw new DatoInvalidoException("Un combo debe tener al menos 2 servicios");
        }

        List<Servicio> incluidos = new ArrayList<>();
        for (String codigo : unicos) {
            Servicio s = obtenerActivo(codigo);
            if (s instanceof Combo) {
                throw new DatoInvalidoException("Un combo solo puede contener servicios simples");
            }
            incluidos.add(s);
        }
        nombre = nombre.trim();
        verificarNombreLibre(nombre);

        Combo combo = new Combo(repositorio.siguienteCodigo(), nombre, precio, incluidos);
        repositorio.guardar(combo);
        return combo;
    }

    /** REQ-SER-06: suma el precio y la duración de los servicios elegidos (un combo usa su precio propio). */
    public TotalesAtencion calcularTotales(List<Servicio> servicios) {
        double precio = 0;
        int duracion = 0;
        for (Servicio s : servicios) {
            precio += s.getPrecio();
            duracion += s.getDuracion();
        }
        return new TotalesAtencion(precio, duracion);
    }

    /** Busca un servicio activo por su código. */
    public Servicio obtenerActivo(String codigo) throws DatoInvalidoException {
        String c = codigo.trim().toUpperCase();
        Servicio s = repositorio.buscarPorCodigo(c)
                .orElseThrow(() -> new DatoInvalidoException("No existe el servicio " + c));
        if (!s.isActivo()) {
            throw new DatoInvalidoException("El servicio " + c + " está inactivo");
        }
        return s;
    }

    private void verificarNombreLibre(String nombre) throws ServicioDuplicadoException {
        if (repositorio.buscarPorNombre(nombre).isPresent()) {
            throw new ServicioDuplicadoException("El servicio ya existe");
        }
    }
}
