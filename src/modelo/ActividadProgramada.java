package modelo;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public abstract class ActividadProgramada implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final DayOfWeek dia;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;

    protected ActividadProgramada(String nombre, DayOfWeek dia,
                                  LocalTime horaInicio, LocalTime horaFin) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio.");
        }
        if (dia == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException("El día y las horas son obligatorios.");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        this.nombre = nombre.trim();
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public String getNombre() { return nombre; }
    public DayOfWeek getDia() { return dia; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public abstract String getTipo();

    @Override
    public String toString() {
        return getHoraInicio() + "-" + getHoraFin() + " " + getTipo() + ": " + nombre;
    }
}
