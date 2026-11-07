package barberia.repositorio;

import barberia.modelo.Cita;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación en memoria (colección) del repositorio de citas.
 */
public class CitaRepositorioMemoria implements CitaRepositorio {

    private final Map<String, Cita> citas = new LinkedHashMap<>();
    private int contador = 0;

    @Override
    public void guardar(Cita cita) {
        citas.put(cita.getCodigo(), cita);
    }

    @Override
    public Optional<Cita> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(citas.get(codigo));
    }

    @Override
    public List<Cita> buscarPorBarberoYFecha(String codigoBarbero, LocalDate fecha) {
        List<Cita> resultado = new ArrayList<>();
        for (Cita c : citas.values()) {
            if (c.getBarbero().getCodigo().equals(codigoBarbero) && c.getFecha().equals(fecha)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public List<Cita> obtenerTodos() {
        return new ArrayList<>(citas.values());
    }

    @Override
    public String siguienteCodigo() {
        contador++;
        return String.format("CIT-%03d", contador);
    }
}
