package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private final List<Materia> materias;
    private final List<ActividadProgramada> actividadesProgramadas;

    public Usuario() {
        this("");
    }

    public Usuario(String nombre) {
        this.nombre = nombre;
        this.materias = new ArrayList<>();
        this.actividadesProgramadas = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Materia> getMaterias() {
        return new ArrayList<>(materias);
    }

    public void agregarMateria(Materia materia) {
        if (materia == null) {
            throw new IllegalArgumentException("La materia no puede ser nula.");
        }
        if (!materias.contains(materia)) {
            materias.add(materia);
        }
    }

    public boolean eliminarMateria(Materia materia) {
        return materias.remove(materia);
    }

    public void agregarActividadProgramada(ActividadProgramada actividad) {
        if (actividad == null) {
            throw new IllegalArgumentException("La actividad programada no puede ser nula.");
        }
        if (!actividadesProgramadas.contains(actividad)) {
            actividadesProgramadas.add(actividad);
        }
    }

    public boolean eliminarActividadProgramada(ActividadProgramada actividad) {
        return actividadesProgramadas.remove(actividad);
    }

    public List<ActividadProgramada> getActividadesProgramadas() {
        return new ArrayList<>(actividadesProgramadas);
    }

    public Materia buscarMateriaPorNombre(String nombreMateria) {
        if (nombreMateria == null) {
            return null;
        }
        for (Materia materia : materias) {
            if (materia.getNombre().equalsIgnoreCase(nombreMateria.trim())) {
                return materia;
            }
        }
        return null;
    }
}
