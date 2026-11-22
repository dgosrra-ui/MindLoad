package modelo;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String nombre;
    private final List<Materia> materias;

    public Usuario() {
        this("");
    }

    public Usuario(String nombre) {
        this.nombre = nombre;
        this.materias = new ArrayList<>();
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
