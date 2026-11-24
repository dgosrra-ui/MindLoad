package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.Materia;
import modelo.Tarea;
import modelo.Usuario;

public class GestorCargaAcademica {
    private final Usuario usuario;
    private final ValidadorDatos validador;

    public GestorCargaAcademica(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        this.usuario = usuario;
        this.validador = new ValidadorDatos();
    }

    public Materia registrarMateria(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la materia no puede estar vacio.");
        }
        Materia existente = usuario.buscarMateriaPorNombre(nombre);
        if (existente != null) {
            throw new IllegalArgumentException("La materia ya existe.");
        }
        Materia materia = new Materia(nombre.trim());
        usuario.agregarMateria(materia);
        return materia;
    }

    public Tarea registrarTarea(String nombreMateria, String descripcion,
                                LocalDate fechaEntrega, int duracionEstimada) {
        List<String> errores = validador.validarCarga(nombreMateria, descripcion,
                fechaEntrega, String.valueOf(duracionEstimada));
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException("Campos invalidos: " + String.join(", ", errores));
        }
        Materia materia = usuario.buscarMateriaPorNombre(nombreMateria);
        if (materia == null) {
            throw new IllegalArgumentException("La materia indicada no existe. Debe registrarla primero.");
        }
        Tarea tarea = new Tarea(descripcion.trim(), fechaEntrega, duracionEstimada, materia);
        materia.agregarTarea(tarea);
        return tarea;
    }

    public void eliminarTarea(Tarea tarea) {
        if (tarea == null || tarea.getMateria() == null
                || !usuario.getMaterias().contains(tarea.getMateria())
                || !tarea.getMateria().getTareas().contains(tarea)) {
            throw new IllegalArgumentException("La tarea seleccionada no pertenece a este usuario.");
        }
        tarea.getMateria().eliminarTarea(tarea);
    }

    public void eliminarMateria(Materia materia) {
        if (materia == null || !usuario.getMaterias().contains(materia)) {
            throw new IllegalArgumentException("La materia seleccionada no pertenece a este usuario.");
        }
        if (!materia.getTareas().isEmpty()) {
            throw new IllegalStateException("Elimina primero las tareas asociadas a esta materia.");
        }
        usuario.eliminarMateria(materia);
    }

    public int calcularCargaPendiente(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula.");
        }
        int total = 0;
        for (Tarea tarea : consultarTareasPorFecha(fecha)) {
            if (!tarea.estaCompletada()) {
                total += tarea.getDuracionEstimada();
            }
        }
        return total;
    }

    public int calcularCargaPendientePorMateria(LocalDate fecha, Materia materia) {
        if (fecha == null || materia == null) {
            throw new IllegalArgumentException("Fecha y materia son obligatorias.");
        }
        int total = 0;
        for (Tarea tarea : materia.getTareas()) {
            if (!tarea.estaCompletada() && tarea.getFechaEntrega().equals(fecha)) {
                total += tarea.getDuracionEstimada();
            }
        }
        return total;
    }

    public List<Tarea> consultarTareasPorFecha(LocalDate fecha) {
        List<Tarea> resultado = new ArrayList<>();
        if (fecha == null) {
            return resultado;
        }
        for (Materia materia : usuario.getMaterias()) {
            for (Tarea tarea : materia.getTareas()) {
                if (tarea.getFechaEntrega().equals(fecha)) {
                    resultado.add(tarea);
                }
            }
        }
        return resultado;
    }

    public List<Tarea> consultarTareasPorMateria(Materia materia) {
        return materia == null ? new ArrayList<>() : materia.getTareas();
    }

    public int consultarCargaPendiente(LocalDate fecha) {
        return calcularCargaPendiente(fecha);
    }

    public int consultarCargaPendientePorMateria(LocalDate fecha, Materia materia) {
        return calcularCargaPendientePorMateria(fecha, materia);
    }

    public void completarTarea(Tarea tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("Debe seleccionar una tarea.");
        }
        tarea.completar();
    }

    public void modificarDuracion(Tarea tarea, int nuevaDuracion) {
        if (tarea == null) {
            throw new IllegalArgumentException("Debe seleccionar una tarea.");
        }
        if (!validador.esEnteroPositivo(String.valueOf(nuevaDuracion))) {
            throw new IllegalArgumentException("La duracion debe ser un entero mayor que cero.");
        }
        tarea.setDuracionEstimada(nuevaDuracion);
    }

    public Usuario getUsuario() { return usuario; }
}
