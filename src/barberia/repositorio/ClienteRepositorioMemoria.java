package barberia.repositorio;

import barberia.modelo.Cliente;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria (colección) del repositorio de clientes.
 */
public class ClienteRepositorioMemoria implements ClienteRepositorio {

    private final Map<String, Cliente> clientes = new LinkedHashMap<>();
    private int contador = 0;

    @Override
    public void guardar(Cliente cliente) {
        clientes.put(cliente.getCodigo(), cliente);
    }

    @Override
    public Optional<Cliente> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(clientes.get(codigo));
    }

    @Override
    public Optional<Cliente> buscarPorCelular(String celular) {
        for (Cliente c : clientes.values()) {
            if (c.getCelular().equals(celular)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Cliente> obtenerTodos() {
        return new ArrayList<>(clientes.values());
    }

    @Override
    public String siguienteCodigo() {
        contador++;
        return String.format("CLI-%03d", contador);
    }
}
