package pruebas.negocio;

import modelo.Materia;
import modelo.Tarea;
import modelo.Usuario;
import negocio.GestorCargaAcademica;

import java.time.LocalDate;

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
