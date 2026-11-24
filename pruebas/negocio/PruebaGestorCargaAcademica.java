package pruebas.negocio;

import modelo.Materia;
import modelo.Tarea;
import modelo.Usuario;
import modelo.ActividadProgramada;
import modelo.Clase;
import modelo.Entrenamiento;
import negocio.GestorCargaAcademica;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class PruebaGestorCargaAcademica {
    private static int fallos;

    public static void main(String[] args) {
        Usuario usuario = new Usuario("Prueba");
        GestorCargaAcademica gestor = new GestorCargaAcademica(usuario);
        LocalDate fecha = LocalDate.now().plusDays(1);

        Materia materia = gestor.registrarMateria("Programacion II");
        verificar(usuario.buscarMateriaPorNombre("Programacion II") == materia,
                "Registrar materia correctamente");

        Tarea tarea = gestor.registrarTarea("Programacion II", "Proyecto MindLoad",
                fecha, 90);
        verificar(tarea.getMateria() == materia, "Registrar tarea asociada");

        try {
            gestor.registrarTarea("Materia inexistente", "Tarea", fecha, 30);
            verificar(false, "Rechazar tarea sin materia");
        } catch (IllegalArgumentException ex) {
            verificar(true, "Rechazar tarea sin materia");
        }

        verificar(gestor.calcularCargaPendiente(fecha) == 90,
                "Calcular carga pendiente");
        gestor.modificarDuracion(tarea, 120);
        verificar(gestor.calcularCargaPendiente(fecha) == 120,
                "Modificar duracion y recalcular");
        gestor.completarTarea(tarea);
        verificar(gestor.calcularCargaPendiente(fecha) == 0,
                "Excluir tarea completada");
        verificar(gestor.calcularCargaPendiente(fecha.plusDays(1)) == 0,
                "Consultar fecha sin tareas");
        verificar(gestor.consultarTareasPorMateria(materia).size() == 1,
                "Consultar tareas por materia");

        try {
            gestor.eliminarMateria(materia);
            verificar(false, "Evitar eliminar materia con tareas");
        } catch (IllegalStateException ex) {
            verificar(true, "Evitar eliminar materia con tareas");
        }

        Clase clase = gestor.registrarClase("Programación", DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0));
        verificar(clase instanceof ActividadProgramada, "Clase hereda de ActividadProgramada");
        gestor.registrarEntrenamiento("Natación", DayOfWeek.MONDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0));
        verificar(gestor.consultarAgenda(DayOfWeek.MONDAY).size() == 2,
                "Consultar agenda ordenada del día");
        verificar(gestor.consultarAgenda(DayOfWeek.MONDAY).get(0) == clase,
                "La agenda ordena por hora de inicio");
        try {
            gestor.registrarEntrenamiento("Traslape", DayOfWeek.MONDAY,
                    LocalTime.of(9, 30), LocalTime.of(10, 30));
            verificar(false, "Rechazar conflicto entre clase y entrenamiento");
        } catch (IllegalArgumentException ex) {
            verificar(true, "Rechazar conflicto entre clase y entrenamiento");
        }
        gestor.registrarEntrenamiento("Otro día", DayOfWeek.TUESDAY,
                LocalTime.of(9, 30), LocalTime.of(10, 30));
        verificar(gestor.consultarAgenda(DayOfWeek.TUESDAY).get(0) instanceof Entrenamiento,
                "Permitir horario coincidente en otro día");

        gestor.eliminarTarea(tarea);
        verificar(materia.getTareas().isEmpty(), "Eliminar tarea");
        gestor.eliminarMateria(materia);
        verificar(usuario.buscarMateriaPorNombre("Programacion II") == null,
                "Eliminar materia vacía");

        if (fallos > 0) {
            throw new AssertionError(fallos + " prueba(s) fallaron.");
        }
    }

    private static void verificar(boolean condicion, String caso) {
        if (condicion) {
            System.out.println("OK - " + caso);
        } else {
            System.out.println("FALLO - " + caso);
            fallos++;
        }
    }
}
