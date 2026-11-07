package barberia.repositorio;

import barberia.modelo.Atencion;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para guardar y consultar atenciones.
 */
public interface AtencionRepositorio {

    void guardar(Atencion atencion);

    Optional<Atencion> buscarPorCodigo(String codigo);

    Optional<Atencion> buscarPorCita(String codigoCita);

    List<Atencion> obtenerTodos();

    String siguienteCodigo();
}
