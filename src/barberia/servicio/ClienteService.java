package barberia.servicio;

import barberia.excepciones.CelularDuplicadoException;
import barberia.excepciones.ClienteNoEncontradoException;
import barberia.excepciones.DatoInvalidoException;
import barberia.modelo.Cliente;
import barberia.repositorio.ClienteRepositorio;
import barberia.util.Validador;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lógica de negocio de los clientes:
 * REQ-CLI-01 (registrar), REQ-CLI-02 (editar), REQ-CLI-03 (buscar) y REQ-CLI-05 (celular único).
 */
public class ClienteService {

    private final ClienteRepositorio repositorio;

    public ClienteService(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /** REQ-CLI-01 y REQ-CLI-05. */
    public Cliente registrar(String nombres, String celular)
            throws DatoInvalidoException, CelularDuplicadoException {
        Validador.validarNombres(nombres);
        Validador.validarCelular(celular);
        nombres = nombres.trim();
        celular = celular.trim();

        Optional<Cliente> existente = repositorio.buscarPorCelular(celular);
        if (existente.isPresent()) {
            Cliente c = existente.get();
            if (c.isActivo()) {
                throw new CelularDuplicadoException(
                        "Ya existe un cliente con ese celular: " + c.getNombres(), c);
            }
            throw new CelularDuplicadoException(
                    "Ya existe un cliente inactivo con ese celular (" + c.getCodigo()
                    + "). Reactívelo en lugar de crear uno nuevo", c);
        }

        Cliente nuevo = new Cliente(repositorio.siguienteCodigo(), nombres, celular);
        repositorio.guardar(nuevo);
        return nuevo;
    }

    /** REQ-CLI-02. */
    public Cliente editar(String codigo, String nombres, String celular)
            throws DatoInvalidoException, CelularDuplicadoException, ClienteNoEncontradoException {
        if (codigo == null || codigo.isBlank()) {
            throw new ClienteNoEncontradoException("Seleccione un cliente");
        }
        Cliente cliente = repositorio.buscarPorCodigo(codigo.trim())
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe un cliente con el código " + codigo));

        Validador.validarNombres(nombres);
        Validador.validarCelular(celular);
        nombres = nombres.trim();
        celular = celular.trim();

        Optional<Cliente> otro = repositorio.buscarPorCelular(celular);
        if (otro.isPresent() && !otro.get().getCodigo().equals(cliente.getCodigo())) {
            throw new CelularDuplicadoException("El celular ya está registrado", otro.get());
        }

        cliente.setNombres(nombres);
        cliente.setCelular(celular);
        repositorio.guardar(cliente);
        return cliente;
    }

    /** REQ-CLI-03: busca por parte del nombre (sin distinguir mayúsculas) o por celular. */
    public List<Cliente> buscar(String texto) throws DatoInvalidoException {
        if (texto == null || texto.isBlank()) {
            throw new DatoInvalidoException("Ingrese un nombre o celular para buscar");
        }
        String t = texto.trim().toLowerCase();
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : repositorio.obtenerTodos()) {
            if (c.getNombres().toLowerCase().contains(t) || c.getCelular().contains(t)) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
