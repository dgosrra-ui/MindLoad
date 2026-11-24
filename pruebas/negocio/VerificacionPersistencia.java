package pruebas.negocio;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import modelo.Materia;
import modelo.PausaActiva;
import modelo.Usuario;
import negocio.GestorCargaAcademica;
import negocio.GestorHorarios;
import negocio.UsuarioRepositorio;

final class VerificacionPersistencia {
    private VerificacionPersistencia() {
    }

    static void ejecutar() throws Exception {
        Path directorio = Files.createTempDirectory("mindload-test-");
        Path archivo = directorio.resolve("mindload.dat");
        try {
            Usuario original = new Usuario("Estudiante persistente");
            GestorCargaAcademica gestor = new GestorCargaAcademica(original);
            Materia materia = gestor.registrarMateria("Biología");
            gestor.registrarTarea("Biología", "Lectura", LocalDate.now().plusDays(1), 30);
            GestorHorarios gestorHorarios = new GestorHorarios(original);
            gestorHorarios.registrarActividad(new modelo.Clase("Laboratorio", DayOfWeek.FRIDAY,
                    LocalTime.of(14, 0), LocalTime.of(16, 0)));
            gestorHorarios.registrarActividad(new PausaActiva("Descanso visual", DayOfWeek.FRIDAY,
                    LocalTime.of(16, 0), LocalTime.of(16, 15)));

            UsuarioRepositorio repositorio = new UsuarioRepositorio(archivo);
            repositorio.guardar(original);
            Usuario recuperado = repositorio.cargar();

            verificar("Conserva nombre", recuperado.getNombre().equals(original.getNombre()));
            verificar("Conserva materias y tareas",
                    recuperado.buscarMateriaPorNombre(materia.getNombre()).getTareas().size() == 1);
            verificar("Conserva agenda semanal",
                    recuperado.getActividadesProgramadas().size() == 2
                            && recuperado.getActividadesProgramadas().get(0).getNombre().equals("Laboratorio"));
            verificar("Conserva subclases serializables",
                    recuperado.getActividadesProgramadas().get(1) instanceof PausaActiva);
        } finally {
            Files.deleteIfExists(archivo);
            Files.deleteIfExists(directorio);
        }
    }

    private static void verificar(String caso, boolean condicion) {
        if (!condicion) {
            throw new AssertionError(caso);
        }
        System.out.println("OK - " + caso);
    }
}
