package modelo;

import java.util.ArrayList;
import java.util.List;

public class Materia {
    private String nombre;
    private final List<Tarea> tareas;

    public Materia() {
        this("");
    }

    public Materia(String nombre) {
        this.nombre = nombre;
        this.tareas = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Tarea> getTareas() {
        return new ArrayList<>(tareas);
    }

    public void agregarTarea(Tarea tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("La tarea no puede ser nula.");
        }
        if (!tareas.contains(tarea)) {
            tareas.add(tarea);
        }
    }

    public void eliminarTarea(Tarea tarea) {
        tareas.remove(tarea);
    }

    @Override
    public String toString() { return nombre; }
}
