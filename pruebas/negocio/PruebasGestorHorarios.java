package pruebas.negocio;

import java.time.DayOfWeek;
import java.time.LocalTime;
import modelo.ActividadProgramada;
import modelo.Clase;
import modelo.Entrenamiento;
import modelo.PausaActiva;
import modelo.Usuario;
import negocio.GestorHorarios;

public class PruebasGestorHorarios {
    private static int ejecutadas;
    private static int aprobadas;

    public static void main(String[] args) {
        Usuario usuario = new Usuario("Prueba");
        GestorHorarios gestor = new GestorHorarios(usuario);
        Clase clase = new Clase("Matemáticas", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0));
        gestor.registrarActividad(usuario, clase);
        verificar("Registrar actividad válida", usuario.getActividadesProgramadas().size() == 1);

        verificarExcepcion("Detectar traslape parcial", IllegalArgumentException.class,
                () -> gestor.registrarActividad(new Entrenamiento("Gimnasio", DayOfWeek.MONDAY,
                        LocalTime.of(10, 30), LocalTime.of(11, 30))));
        verificar("Mensaje identifica actividad conflictiva",
                mensajeConflicto(gestor, new PausaActiva("Descanso", DayOfWeek.MONDAY,
                        LocalTime.of(10, 30), LocalTime.of(10, 45))).contains("Matemáticas"));
        verificarExcepcion("Detectar actividad contenida", IllegalArgumentException.class,
                () -> gestor.registrarActividad(new PausaActiva("Pausa", DayOfWeek.MONDAY,
                        LocalTime.of(9, 30), LocalTime.of(10, 0))));

        gestor.registrarActividad(new Entrenamiento("Gimnasio", DayOfWeek.MONDAY,
                LocalTime.of(11, 0), LocalTime.of(12, 0)));
        verificar("Permitir actividades consecutivas", gestor.consultarActividadesPorDia(
                DayOfWeek.MONDAY).size() == 2);

        verificarExcepcion("Rechazar hora de fin igual al inicio", IllegalArgumentException.class,
                () -> new Clase("Inválida", DayOfWeek.TUESDAY,
                        LocalTime.of(9, 0), LocalTime.of(9, 0)));
        verificarExcepcion("Rechazar nombre vacío", IllegalArgumentException.class,
                () -> new Clase("  ", DayOfWeek.TUESDAY,
                        LocalTime.of(9, 0), LocalTime.of(10, 0)));
        verificarExcepcion("Rechazar día nulo", IllegalArgumentException.class,
                () -> new PausaActiva("Descanso", null,
                        LocalTime.of(9, 0), LocalTime.of(10, 0)));
        verificarExcepcion("Rechazar hora nula", IllegalArgumentException.class,
                () -> new PausaActiva("Descanso", DayOfWeek.TUESDAY,
                        null, LocalTime.of(10, 0)));

        gestor.registrarActividad(new PausaActiva("Pausa", DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 15)));
        verificar("Permitir mismo horario en día distinto",
                gestor.consultarActividadesPorDia(DayOfWeek.TUESDAY).size() == 1);

        Usuario ordenUsuario = new Usuario("Orden");
        GestorHorarios orden = new GestorHorarios(ordenUsuario);
        ActividadProgramada tarde = new Clase("Tarde", DayOfWeek.WEDNESDAY,
                LocalTime.of(14, 0), LocalTime.of(15, 0));
        ActividadProgramada manana = new PausaActiva("Mañana", DayOfWeek.WEDNESDAY,
                LocalTime.of(8, 0), LocalTime.of(8, 15));
        orden.registrarActividad(tarde);
        orden.registrarActividad(manana);
        verificar("Ordenar actividades por hora",
                orden.consultarActividadesPorDia(DayOfWeek.WEDNESDAY).get(0) == manana);

        orden.eliminarActividad(tarde);
        verificar("Eliminar actividad del usuario",
                orden.consultarTodas().size() == 1 && orden.consultarTodas().get(0) == manana);
        verificarExcepcion("Rechazar usuario nulo al registrar", IllegalArgumentException.class,
                () -> orden.registrarActividad(null, manana));
        verificarExcepcion("Rechazar día nulo al consultar", IllegalArgumentException.class,
                () -> orden.consultarActividadesPorDia(null));

        System.out.println("Resultado RF-04: " + aprobadas + "/" + ejecutadas + " pruebas aprobadas.");
        if (aprobadas != ejecutadas) {
            System.exit(1);
        }
    }

    private static String mensajeConflicto(GestorHorarios gestor, ActividadProgramada actividad) {
        try {
            gestor.registrarActividad(actividad);
            return "";
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    private static void verificarExcepcion(String nombre, Class<? extends Throwable> tipo, Runnable accion) {
        try {
            accion.run();
            verificar(nombre, false);
        } catch (Throwable ex) {
            verificar(nombre, tipo.isInstance(ex));
        }
    }

    private static void verificar(String nombre, boolean condicion) {
        ejecutadas++;
        if (condicion) {
            aprobadas++;
            System.out.println("[OK] " + nombre);
        } else {
            System.out.println("[FALLO] " + nombre);
        }
    }
}
