package barberia.repositorio;

import barberia.modelo.Atencion;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria (colección) del repositorio de atenciones.
 */
public class AtencionRepositorioMemoria implements AtencionRepositorio {

    private final Map<String, Atencion> atenciones = new LinkedHashMap<>();
    private int contador = 0;

    @Override
    public void guardar(Atencion atencion) {
        atenciones.put(atencion.getCodigo(), atencion);
    }

    @Override
    public Optional<Atencion> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(atenciones.get(codigo));
    }

    @Override
    public Optional<Atencion> buscarPorCita(String codigoCita) {
        for (Atencion a : atenciones.values()) {
            if (a.getCita() != null && a.getCita().getCodigo().equals(codigoCita)) {
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Atencion> buscarPorCliente(String codigoCliente) {
        List<Atencion> resultado = new ArrayList<>();
        for (Atencion a : atenciones.values()) {
            if (a.getCliente().getCodigo().equals(codigoCliente)) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    @Override
    public List<Atencion> obtenerTodos() {
        return new ArrayList<>(atenciones.values());
    }

    @Override
    public String siguienteCodigo() {
        contador++;
        return String.format("ATE-%03d", contador);
    }
}
