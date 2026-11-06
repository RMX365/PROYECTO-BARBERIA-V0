package barberia.repositorio;

import barberia.modelo.Barbero;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria (colección) del repositorio de barberos.
 */
public class BarberoRepositorioMemoria implements BarberoRepositorio {

    private final Map<String, Barbero> barberos = new LinkedHashMap<>();
    private int contador = 0;

    @Override
    public void guardar(Barbero barbero) {
        barberos.put(barbero.getCodigo(), barbero);
    }

    @Override
    public Optional<Barbero> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(barberos.get(codigo));
    }

    @Override
    public List<Barbero> obtenerTodos() {
        return new ArrayList<>(barberos.values());
    }

    @Override
    public String siguienteCodigo() {
        contador++;
        return String.format("BAR-%03d", contador);
    }
}
