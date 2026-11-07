package barberia.repositorio;

import barberia.modelo.Cita;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para guardar y consultar citas.
 */
public interface CitaRepositorio {

    void guardar(Cita cita);

    Optional<Cita> buscarPorCodigo(String codigo);

    List<Cita> buscarPorBarberoYFecha(String codigoBarbero, LocalDate fecha);

    List<Cita> obtenerTodos();

    String siguienteCodigo();
}
