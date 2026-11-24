package negocio;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import modelo.ActividadProgramada;
import modelo.Usuario;

public class GestorHorarios {
    private final Usuario usuario;

    public GestorHorarios(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        this.usuario = usuario;
    }

    public void registrarActividad(Usuario usuario, ActividadProgramada actividad) {
        validarUsuario(usuario);
        if (actividad == null) {
            throw new IllegalArgumentException("La actividad programada no puede ser nula.");
        }
        for (ActividadProgramada existente : this.usuario.getActividadesProgramadas()) {
            boolean mismoDia = existente.getDia() == actividad.getDia();
            boolean seTraslapan = actividad.getHoraInicio().isBefore(existente.getHoraFin())
                    && actividad.getHoraFin().isAfter(existente.getHoraInicio());
            if (mismoDia && seTraslapan) {
                throw new IllegalArgumentException("Conflicto de horario con "
                        + existente.getTipo() + " \"" + existente.getNombre() + "\" ("
                        + existente.getHoraInicio() + "-" + existente.getHoraFin() + ").");
            }
        }
        this.usuario.agregarActividadProgramada(actividad);
    }

    public void registrarActividad(ActividadProgramada actividad) {
        registrarActividad(usuario, actividad);
    }

    public List<ActividadProgramada> consultarActividadesPorDia(DayOfWeek dia) {
        if (dia == null) {
            throw new IllegalArgumentException("El día de consulta es obligatorio.");
        }
        List<ActividadProgramada> resultado = new ArrayList<>();
        for (ActividadProgramada actividad : usuario.getActividadesProgramadas()) {
            if (actividad.getDia() == dia) {
                resultado.add(actividad);
            }
        }
        resultado.sort(Comparator.comparing(ActividadProgramada::getHoraInicio));
        return resultado;
    }

    public void eliminarActividad(ActividadProgramada actividad) {
        if (actividad == null) {
            throw new IllegalArgumentException("Debe seleccionar una actividad para eliminar.");
        }
        if (!usuario.eliminarActividadProgramada(actividad)) {
            throw new IllegalArgumentException("La actividad seleccionada no pertenece a este usuario.");
        }
    }

    public List<ActividadProgramada> consultarTodas() {
        List<ActividadProgramada> resultado = usuario.getActividadesProgramadas();
        resultado.sort(Comparator.comparing(ActividadProgramada::getDia)
                .thenComparing(ActividadProgramada::getHoraInicio));
        return resultado;
    }

    private void validarUsuario(Usuario candidato) {
        if (candidato == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        if (candidato != usuario) {
            throw new IllegalArgumentException("El usuario no coincide con el gestionado por este gestor.");
        }
    }
}
