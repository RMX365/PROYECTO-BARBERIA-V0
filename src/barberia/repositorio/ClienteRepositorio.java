package barberia.repositorio;

import barberia.modelo.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Contrato para guardar y consultar clientes.
 * Hoy se implementa en memoria; después se podrá implementar con JDBC sin cambiar el resto del sistema.
 */
public interface ClienteRepositorio {

    void guardar(Cliente cliente);

    Optional<Cliente> buscarPorCodigo(String codigo);

    Optional<Cliente> buscarPorCelular(String celular);

    List<Cliente> obtenerTodos();

    String siguienteCodigo();
}
