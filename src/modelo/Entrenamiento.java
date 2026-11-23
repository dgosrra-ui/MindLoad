package modelo;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class Entrenamiento extends ActividadProgramada {
    private static final long serialVersionUID = 1L;

    public Entrenamiento(String nombre, DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
        super(nombre, dia, horaInicio, horaFin);
    }

    @Override
    public String getTipo() {
        return "Entrenamiento";
    }
}
