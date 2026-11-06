package barberia.repositorio;

import barberia.modelo.Servicio;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria (colección) del repositorio de servicios.
 */
public class ServicioRepositorioMemoria implements ServicioRepositorio {

    private final Map<String, Servicio> servicios = new LinkedHashMap<>();
    private int contador = 0;

    @Override
    public void guardar(Servicio servicio) {
        servicios.put(servicio.getCodigo(), servicio);
    }

    @Override
    public Optional<Servicio> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(servicios.get(codigo));
    }

    @Override
    public Optional<Servicio> buscarPorNombre(String nombre) {
        for (Servicio s : servicios.values()) {
            if (s.getNombre().equalsIgnoreCase(nombre)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Servicio> obtenerTodos() {
        return new ArrayList<>(servicios.values());
    }

    @Override
    public String siguienteCodigo() {
        contador++;
        return String.format("SER-%03d", contador);
    }
}
