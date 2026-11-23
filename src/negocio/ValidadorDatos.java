package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ValidadorDatos {
    public List<String> validarCarga(String nombreMateria, String descripcion,
                                      LocalDate fechaEntrega, String duracionEstimada) {
        List<String> errores = new ArrayList<>();

        if (nombreMateria == null || nombreMateria.trim().isEmpty()) {
            errores.add("materia");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            errores.add("descripcion");
        }
        if (fechaEntrega == null) {
            errores.add("fecha de entrega");
        }
        if (!esEnteroPositivo(duracionEstimada)) {
            errores.add("duracion");
        }

        return errores;
    }

    public boolean esEnteroPositivo(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(valor.trim()) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
