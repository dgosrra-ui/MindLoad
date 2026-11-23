package pruebas.negocio;

import java.time.LocalDate;
import java.util.List;
import negocio.ValidadorDatos;

public class PruebasValidadorDatos {
    private static final ValidadorDatos V = new ValidadorDatos();
    private static final LocalDate HOY = LocalDate.now();
    private static int ejecutadas = 0;
    private static int aprobadas = 0;

    private static void verificar(String id, String descripcion, boolean condicion) {
        ejecutadas++;
        System.out.println((condicion ? "[OK] " : "[FALLO] ") + id + " - " + descripcion);
        if (condicion) aprobadas++;
    }

    private static List<String> errores(String materia, String tarea, LocalDate fecha, String duracion) {
        return V.validarCarga(materia, tarea, fecha, duracion);
    }

    public static void main(String[] args) {
        verificar("P01", "Datos correctos y entrega hoy", errores("Fisica", "Taller", HOY, "45").isEmpty());
        verificar("P02", "Entrega futura", errores("Calculo", "Ejercicios", HOY.plusDays(3), "60").isEmpty());
        verificar("P03", "Materia vacia", !errores("", "Taller", HOY, "45").isEmpty());
        verificar("P04", "Materia solo espacios", !errores("   ", "Taller", HOY, "45").isEmpty());
        verificar("P05", "Materia nula", !errores(null, "Taller", HOY, "45").isEmpty());
        verificar("P06", "Descripcion vacia", !errores("Fisica", "", HOY, "45").isEmpty());
        verificar("P07", "Descripcion nula", !errores("Fisica", null, HOY, "45").isEmpty());
        verificar("P08", "Fecha nula", !errores("Fisica", "Taller", null, "45").isEmpty());
        verificar("P09", "Fecha anterior a hoy", !errores("Fisica", "Taller", HOY.minusDays(1), "45").isEmpty());
        verificar("P10", "Duracion cero", !errores("Fisica", "Taller", HOY, "0").isEmpty());
        verificar("P11", "Duracion negativa", !errores("Fisica", "Taller", HOY, "-10").isEmpty());
        verificar("P12", "Duracion con letras", !errores("Fisica", "Taller", HOY, "dos").isEmpty());
        verificar("P13", "Duracion decimal", !errores("Fisica", "Taller", HOY, "2.5").isEmpty());
        verificar("P14", "Duracion nula", !errores("Fisica", "Taller", HOY, null).isEmpty());
        verificar("P15", "Duracion fuera del rango int", !errores("Fisica", "Taller", HOY, "999999999999999").isEmpty());
        verificar("P16", "Duracion con espacios exteriores", errores("Fisica", "Taller", HOY, " 30 ").isEmpty());
        verificar("P17", "Varios campos invalidos", !errores("", "", null, "0").isEmpty());
        verificar("P18", "Cuatro campos invalidos generan cuatro errores", errores("", "", null, "0").size() == 4);
        verificar("P19", "Entero positivo 90", V.esEnteroPositivo("90"));
        verificar("P20", "Entero no numerico", !V.esEnteroPositivo("abc"));
        verificar("P21", "Descripcion compuesta solo por espacios", !errores("Fisica", "   ", HOY, "45").isEmpty());
        verificar("P22", "Duracion con texto vacio", !errores("Fisica", "Taller", HOY, "").isEmpty());
        verificar("P23", "Duracion solo espacios", !errores("Fisica", "Taller", HOY, "  ").isEmpty());
        verificar("P24", "Valor maximo de int permitido", V.esEnteroPositivo(String.valueOf(Integer.MAX_VALUE)));
        verificar("P25", "Valor minimo de int rechazado", !V.esEnteroPositivo(String.valueOf(Integer.MIN_VALUE)));
        verificar("P26", "Numero entero desbordado rechazado", !V.esEnteroPositivo("2147483648"));
        verificar("P27", "Descripcion con tildes valida", errores("Matematicas", "Resolver ecuaciones y graficas", HOY, "25").isEmpty());
        verificar("P28", "Fecha valida para hoy", V.esFechaValida(HOY));
        verificar("P29", "Fecha futura valida", V.esFechaValida(HOY.plusDays(365)));
        verificar("P30", "Fecha nula rechazada", !V.esFechaValida(null));
        verificar("P31", "Mensaje de error identifica materia", errores("", "Taller", HOY, "45").get(0).contains("materia"));
        verificar("P32", "Mensaje de error identifica duracion", errores("Fisica", "Taller", HOY, "0").get(0).contains("duracion"));

        System.out.println("\nRESULTADO UNITARIAS: " + aprobadas + "/" + ejecutadas + " pruebas aprobadas.");
        if (aprobadas != ejecutadas) throw new AssertionError("Fallo al menos una prueba de RF-03.");
    }
}
