package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ValidadorDatos {
    public boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public boolean esEnteroPositivo(String texto) {
        if (!esTextoValido(texto)) {
            return false;
        }
        try {
            return Integer.parseInt(texto.trim()) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public boolean esFechaValida(LocalDate fechaEntrega) {
        return fechaEntrega != null && !fechaEntrega.isBefore(LocalDate.now());
    }

    public List<String> validarCarga(String nombreMateria, String descripcion,
                                     LocalDate fechaEntrega, String duracionEstimada) {
        List<String> errores = new ArrayList<>();

        if (!esTextoValido(nombreMateria)) {
            errores.add("nombre de la materia (obligatorio)");
        }
        if (!esTextoValido(descripcion)) {
            errores.add("descripcion de la tarea (obligatoria)");
        }
        if (!esFechaValida(fechaEntrega)) {
            errores.add("fecha de entrega (debe ser hoy o posterior)");
        }
        if (!esEnteroPositivo(duracionEstimada)) {
            errores.add("duracion estimada (entero mayor que cero)");
        }
        return errores;
    }
}
