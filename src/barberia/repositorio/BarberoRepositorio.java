package barberia.repositorio;

import barberia.modelo.Barbero;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para guardar y consultar barberos.
 */
public interface BarberoRepositorio {

    void guardar(Barbero barbero);

    Optional<Barbero> buscarPorCodigo(String codigo);

    List<Barbero> obtenerTodos();

    String siguienteCodigo();
}
