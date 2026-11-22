package pruebas;

import modelo.Materia;
import modelo.Tarea;
import modelo.Usuario;
import negocio.GestorCargaAcademica;

import java.time.LocalDate;

public class PruebaGestorCargaAcademica {
    public static void main(String[] args) {
        Usuario usuario = new Usuario("Prueba");
        GestorCargaAcademica gestor = new GestorCargaAcademica(usuario);
        LocalDate fecha = LocalDate.of(2026, 11, 23);

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
    }

    private static void verificar(boolean condicion, String caso) {
        if (condicion) {
            System.out.println("OK - " + caso);
        } else {
            System.out.println("FALLO - " + caso);
        }
    }
}
