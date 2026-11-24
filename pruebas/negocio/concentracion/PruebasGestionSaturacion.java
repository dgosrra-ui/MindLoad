package pruebas.negocio.concentracion;

import negocio.concentracion.Alerta;
import negocio.concentracion.CicloConcentracion;
import negocio.concentracion.EstadoCiclo;
import negocio.concentracion.GestionSaturacion;
import negocio.concentracion.ListenerSaturacion;
import negocio.concentracion.NivelAlerta;
import negocio.concentracion.TipoPausa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas funcionales del módulo del Integrante 4 (sin librerías externas).
 * Ejecutar: java -cp out pruebas.negocio.concentracion.PruebasGestionSaturacion
 */
public class PruebasGestionSaturacion {

    private static int ok = 0;
    private static int fallos = 0;

    /** Listener que registra todo lo que la lógica notifica. */
    static class Registro implements ListenerSaturacion {
        final List<Alerta> alertas = new ArrayList<>();
        final List<String> transiciones = new ArrayList<>();
        int planesFinalizados = 0;
        int ticks = 0;

        @Override
        public void onCambioEstado(CicloConcentracion c, EstadoCiclo ant, EstadoCiclo nuevo) {
            transiciones.add(ant + "->" + nuevo);
        }

        @Override
        public void onAlerta(Alerta a) {
            alertas.add(a);
        }

        @Override
        public void onTick(CicloConcentracion c) {
            ticks++;
        }

        @Override
        public void onPlanFinalizado() {
            planesFinalizados++;
        }

        int contar(NivelAlerta nivel, String fragmento) {
            int n = 0;
            for (Alerta a : alertas) {
                if ((nivel == null || a.getNivel() == nivel)
                        && a.getMensaje().toLowerCase().contains(fragmento.toLowerCase())) {
                    n++;
                }
            }
            return n;
        }
    }

    private static void check(String nombre, boolean condicion) {
        if (condicion) {
            ok++;
            System.out.println("[OK]    " + nombre);
        } else {
            fallos++;
            System.out.println("[FALLO] " + nombre);
        }
    }

    private static boolean lanza(Class<? extends Throwable> tipo, Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (Throwable t) {
            return tipo.isInstance(t);
        }
    }

    public static void main(String[] args) {
        // ---------- RF-07: fragmentación ----------
        GestionSaturacion g = new GestionSaturacion();
        List<CicloConcentracion> c = g.planificar("Cálculo", 100);
        check("P4-01 100 min -> 4 ciclos de 25 min",
                c.size() == 4 && c.get(0).getDuracionConcentracionMin() == 25 && c.get(3).getDuracionConcentracionMin() == 25);
        check("P4-01b pausas: corta, corta, corta y último sin pausa",
                c.get(0).getTipoPausa() == TipoPausa.CORTA && c.get(2).getTipoPausa() == TipoPausa.CORTA
                        && c.get(3).getTipoPausa() == TipoPausa.NINGUNA);
        check("P4-01c ciclos consecutivos tienen hora planificada",
                c.get(1).getFechaHoraPlanificada().equals(
                        c.get(0).getFechaHoraPlanificada().plusMinutes(30)));
        LocalDateTime inicioFijo = LocalDateTime.of(2026, 11, 23, 10, 0);
        c = g.planificar("Cálculo", 25, inicioFijo);
        check("P4-01d la rutina respeta el día y hora seleccionados",
                c.get(0).getFechaHoraPlanificada().equals(inicioFijo));

        c = g.planificar("Cálculo", 200);
        check("P4-02 200 min -> 8 ciclos, pausa larga tras el 4, último sin pausa",
                c.size() == 8 && c.get(3).getTipoPausa() == TipoPausa.LARGA && c.get(3).getDuracionPausaMin() == 15
                        && c.get(6).getTipoPausa() == TipoPausa.CORTA && c.get(7).getTipoPausa() == TipoPausa.NINGUNA);

        c = g.planificar("Cálculo", 110);
        check("P4-03 110 min -> 5 ciclos, el último de 10 min",
                c.size() == 5 && c.get(4).getDuracionConcentracionMin() == 10);

        c = g.planificar("Cálculo", 102);
        check("P4-04 102 min -> 4 ciclos, resto de 2 min se suma al último (27 min)",
                c.size() == 4 && c.get(3).getDuracionConcentracionMin() == 27);

        c = g.planificar("Cálculo", 20);
        check("P4-05 20 min -> un solo ciclo de 20 min sin pausa",
                c.size() == 1 && c.get(0).getDuracionConcentracionMin() == 20 && c.get(0).getTipoPausa() == TipoPausa.NINGUNA);

        c = g.planificar("Cálculo", 25);
        check("P4-06 25 min exactos -> un ciclo de 25 min", c.size() == 1 && c.get(0).getDuracionConcentracionMin() == 25);

        // ---------- Validaciones y estados inválidos ----------
        check("P4-07 minutos = 0 lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> new GestionSaturacion().planificar("Física", 0)));
        check("P4-07b minutos negativos lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> new GestionSaturacion().planificar("Física", -30)));
        check("P4-07c actividad vacía lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> new GestionSaturacion().planificar("   ", 50)));
        check("P4-07d actividad null lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> new GestionSaturacion().planificar(null, 50)));
        check("P4-08 iniciar ciclo sin plan lanza IllegalStateException",
                lanza(IllegalStateException.class, () -> new GestionSaturacion().iniciarSiguienteCiclo()));
        check("P4-17 configuración inválida (duración 0) lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> new GestionSaturacion(0, 5, 15, 4, 150, 240)));
        check("P4-17b configuración inválida (crítico <= advertencia) lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> new GestionSaturacion(25, 5, 15, 4, 200, 200)));
        check("P4-20b avanzarSegundos negativo lanza IllegalArgumentException",
                lanza(IllegalArgumentException.class, () -> new GestionSaturacion().avanzarSegundos(-1)));

        // ---------- Flujo de estados y pausa obligatoria ----------
        GestionSaturacion f = new GestionSaturacion();
        Registro r = new Registro();
        f.agregarListener(r);
        f.planificar("Programación", 60); // 25 + 25 + 10
        check("P4-09a antes de iniciar se puede iniciar el ciclo 1", f.puedeIniciarCiclo());
        f.iniciarSiguienteCiclo();
        check("P4-09b ciclo 1 en concentración", f.getCicloActual().getEstado() == EstadoCiclo.EN_CONCENTRACION);
        check("P4-09c no se puede iniciar otro ciclo con uno en curso", !f.puedeIniciarCiclo());
        f.avanzarSegundos(25 * 60);
        check("P4-09d al terminar la concentración pasa a pausa",
                f.getCicloActual().getEstado() == EstadoCiclo.EN_PAUSA && f.isPausaObligatoriaPendiente());
        check("P4-09e pausa pendiente impide iniciar otro ciclo", !f.puedeIniciarCiclo());
        check("P4-09f intentar iniciar con pausa pendiente lanza IllegalStateException",
                lanza(IllegalStateException.class, f::iniciarSiguienteCiclo));
        check("P4-09g se emitió alerta ADVERTENCIA de pausa obligatoria",
                r.contar(NivelAlerta.ADVERTENCIA, "pausa obligatoria") >= 2);
        f.avanzarSegundos(5 * 60);
        check("P4-09h al terminar la pausa el ciclo queda finalizado y se puede continuar",
                f.getCiclos().get(0).getEstado() == EstadoCiclo.FINALIZADO && f.puedeIniciarCiclo()
                        && !f.isPausaObligatoriaPendiente());
        check("P4-10 secuencia de estados notificada a la interfaz",
                r.transiciones.get(0).equals("PENDIENTE->EN_CONCENTRACION")
                        && r.transiciones.get(1).equals("EN_CONCENTRACION->EN_PAUSA")
                        && r.transiciones.get(2).equals("EN_PAUSA->FINALIZADO"));
        check("P4-10b la interfaz recibió ticks de tiempo", r.ticks > 0);

        // ---------- Avance de tiempo grande ----------
        GestionSaturacion t = new GestionSaturacion();
        t.planificar("Química", 60);
        t.iniciarSiguienteCiclo();
        t.avanzarSegundos(40 * 60);
        check("P4-11 tick grande: ciclo 1 finalizado y el ciclo 2 no inicia solo",
                t.getCiclos().get(0).getEstado() == EstadoCiclo.FINALIZADO
                        && t.getCiclos().get(1).getEstado() == EstadoCiclo.PENDIENTE);

        // ---------- Aviso previo ----------
        GestionSaturacion a = new GestionSaturacion();
        Registro ra = new Registro();
        a.agregarListener(ra);
        a.planificar("Estadística", 25);
        a.iniciarSiguienteCiclo();
        for (int i = 0; i < 25 * 60; i++) {
            a.avanzarSegundos(1);
        }
        check("P4-12 aviso previo emitido una sola vez", ra.contar(NivelAlerta.INFO, "quedan aproximadamente") == 1);
        check("P4-12b plan de 1 ciclo se completa y notifica una vez", a.isPlanCompletado() && ra.planesFinalizados == 1);

        // ---------- Jornada completa y saturación ----------
        GestionSaturacion j = new GestionSaturacion();
        Registro rj = new Registro();
        j.agregarListener(rj);
        j.planificar("Examen final", 300);
        check("P4-13a 300 min -> 12 ciclos", j.getCiclos().size() == 12);
        check("P4-13b duración total planificada = 300 + 75 min de pausas", j.getDuracionTotalPlanificadaMin() == 375);
        while (j.puedeIniciarCiclo()) {
            CicloConcentracion ciclo = j.iniciarSiguienteCiclo();
            j.avanzarSegundos((ciclo.getDuracionConcentracionMin() + ciclo.getDuracionPausaMin()) * 60);
        }
        check("P4-13c 300 min concentrados y plan completado", j.getMinutosConcentrados() == 300 && j.isPlanCompletado());
        check("P4-13d alerta de advertencia por saturación emitida una vez",
                rj.contar(NivelAlerta.ADVERTENCIA, "riesgo de saturación cognitiva") == 1);
        check("P4-13e alerta crítica por saturación emitida una vez",
                rj.contar(NivelAlerta.CRITICA, "saturación cognitiva alta") == 1);
        check("P4-13f nivel de saturación de la jornada es CRITICA", j.getNivelSaturacion() == NivelAlerta.CRITICA);
        check("P4-13g se notificó el fin del plan una sola vez", rj.planesFinalizados == 1);
        check("P4-19 la pausa larga se anuncia como pausa larga de 15 min",
                rj.contar(NivelAlerta.ADVERTENCIA, "pausa larga) de 15 min") == 2);
        check("P4-20 no se puede iniciar ciclo con el plan completado",
                lanza(IllegalStateException.class, j::iniciarSiguienteCiclo));

        // ---------- Configuración personalizada ----------
        GestionSaturacion p = new GestionSaturacion(50, 10, 30, 2, 120, 200);
        List<CicloConcentracion> cp = p.planificar("Tesis", 200);
        check("P4-14 configuración 50/10/30 cada 2: 4 ciclos y pausas corta, larga, corta, ninguna",
                cp.size() == 4 && cp.get(0).getDuracionPausaMin() == 10 && cp.get(1).getTipoPausa() == TipoPausa.LARGA
                        && cp.get(1).getDuracionPausaMin() == 30 && cp.get(2).getTipoPausa() == TipoPausa.CORTA
                        && cp.get(3).getTipoPausa() == TipoPausa.NINGUNA);

        // ---------- Otras reglas ----------
        GestionSaturacion e = new GestionSaturacion();
        e.planificar("Inglés", 60);
        e.iniciarSiguienteCiclo();
        check("P4-15 no se puede replanificar con un ciclo en curso",
                lanza(IllegalStateException.class, () -> e.planificar("Otra", 30)));

        GestionSaturacion k = new GestionSaturacion();
        k.planificar("Lectura", 25);
        k.iniciarSiguienteCiclo();
        k.avanzarSegundos(10 * 60);
        check("P4-16a minutos concentrados = 10", k.getMinutosConcentrados() == 10);
        k.reiniciarJornada();
        check("P4-16b reiniciarJornada deja el acumulado en 0", k.getMinutosConcentrados() == 0);

        GestionSaturacion reporte = new GestionSaturacion();
        Registro rr = new Registro();
        reporte.agregarListener(rr);
        reporte.reportarSaturacion(NivelAlerta.ADVERTENCIA);
        check("P4-21 reporte del estudiante actualiza el nivel de saturación",
                reporte.getNivelSaturacion() == NivelAlerta.ADVERTENCIA);
        check("P4-22 el reporte de saturación genera una alerta visible al listener",
                rr.contar(NivelAlerta.ADVERTENCIA, "estudiante reporta") == 1);
        reporte.reiniciarJornada();
        check("P4-23 reiniciar la jornada limpia el reporte manual",
                reporte.getNivelSaturacion() == NivelAlerta.INFO);

        GestionSaturacion saturacionAlta = new GestionSaturacion();
        Registro registroAlta = new Registro();
        saturacionAlta.agregarListener(registroAlta);
        saturacionAlta.planificar("Lectura", 25);
        saturacionAlta.iniciarSiguienteCiclo();
        saturacionAlta.avanzarSegundos(10 * 60);
        saturacionAlta.reportarSaturacion(NivelAlerta.CRITICA);
        check("P4-24 saturación alta interrumpe el ciclo y activa pausa",
                saturacionAlta.getCicloActual().getEstado() == EstadoCiclo.EN_PAUSA);
        check("P4-25 conserva los 15 minutos no ejecutados como pendientes",
                saturacionAlta.getMinutosPendientes() == 15);
        check("P4-26 emite el mensaje exacto de pausa activa",
                registroAlta.contar(NivelAlerta.ADVERTENCIA,
                        "Nivel de saturación alto. Es momento de tu pausa activa") == 1);
        check("P4-27 registra solo el tiempo realmente concentrado",
                saturacionAlta.getMinutosConcentrados() == 10);

        GestionSaturacion saturacionBaja = new GestionSaturacion();
        saturacionBaja.planificar("Ejercicios", 25);
        saturacionBaja.iniciarSiguienteCiclo();
        saturacionBaja.avanzarSegundos(5 * 60);
        saturacionBaja.reportarSaturacion(NivelAlerta.INFO);
        check("P4-28 saturación baja no interrumpe la concentración",
                saturacionBaja.getCicloActual().getEstado() == EstadoCiclo.EN_CONCENTRACION);
        check("P4-29 minutos pendientes incluyen el resto del bloque activo",
                saturacionBaja.getMinutosPendientes() == 20);

        GestionSaturacion h = new GestionSaturacion();
        h.planificar("Álgebra", 25);
        CicloConcentracion cf = h.iniciarSiguienteCiclo();
        check("P4-18a contador inicial 25:00", cf.getTiempoRestanteFormateado().equals("25:00"));
        h.avanzarSegundos(90);
        check("P4-18b tras 90 s el contador marca 23:30", cf.getTiempoRestanteFormateado().equals("23:30"));
        check("P4-18c progreso entre 0 y 1", cf.getProgreso() > 0.0 && cf.getProgreso() < 1.0);

        System.out.println();
        System.out.println("Resultado: " + ok + " pruebas correctas, " + fallos + " fallidas.");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
