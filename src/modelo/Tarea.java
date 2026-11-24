package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Tarea implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Estado {
        PENDIENTE, EN_CURSO, COMPLETADA
    }

    private String descripcion;
    private LocalDate fechaEntrega;
    private int duracionEstimada;
    private Estado estado;
    private final Materia materia;
    private LocalTime horaProgramada;

    public Tarea(String descripcion, LocalDate fechaEntrega, int duracionEstimada, Materia materia) {
        this(descripcion, fechaEntrega, duracionEstimada, materia, null);
    }

    public Tarea(String descripcion, LocalDate fechaEntrega, int duracionEstimada,
                 Materia materia, LocalTime horaProgramada) {
        this.descripcion = descripcion;
        this.fechaEntrega = fechaEntrega;
        this.duracionEstimada = duracionEstimada;
        this.materia = materia;
        this.horaProgramada = horaProgramada;
        this.estado = Estado.PENDIENTE;
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public int getDuracionEstimada() { return duracionEstimada; }
    public void setDuracionEstimada(int duracionEstimada) { this.duracionEstimada = duracionEstimada; }
    public LocalTime getHoraProgramada() { return horaProgramada; }
    public void setHoraProgramada(LocalTime horaProgramada) { this.horaProgramada = horaProgramada; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public Materia getMateria() { return materia; }
    public boolean estaCompletada() { return estado == Estado.COMPLETADA; }
    public void completar() { estado = Estado.COMPLETADA; }

    @Override
    public String toString() {
        return materia.getNombre() + " - " + descripcion + " (" + duracionEstimada + " min)";
    }
}
