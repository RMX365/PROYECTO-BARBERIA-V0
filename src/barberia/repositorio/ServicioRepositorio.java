package barberia.repositorio;

import barberia.modelo.Servicio;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para guardar y consultar servicios (y combos).
 */
public interface ServicioRepositorio {

    void guardar(Servicio servicio);

    Optional<Servicio> buscarPorCodigo(String codigo);

    Optional<Servicio> buscarPorNombre(String nombre);

    List<Servicio> obtenerTodos();

    String siguienteCodigo();
}
