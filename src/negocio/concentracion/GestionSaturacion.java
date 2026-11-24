package negocio.concentracion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lógica de concentración y sistema de alertas de MindLoad.
 *
 * Responsabilidades:
 * - RF-07: fragmentar el tiempo de estudio en ciclos de concentración con sus pausas.
 * - RF-08: generar alertas por riesgo de saturación cognitiva.
 * - Gestionar los cambios de estado (concentración, pausa, finalización).
 * - Impedir que se inicie otro ciclo mientras exista una pausa obligatoria pendiente.
 *
 * La clase no usa temporizadores propios: la interfaz llama a
 * {@link #avanzarSegundos(int)} (por ejemplo, cada segundo desde un Timer),
 * lo que mantiene la lógica independiente de la GUI y fácil de probar.
 *
 * @author Integrante 4 - Lógica de negocio y sistema de alertas
 */
public class GestionSaturacion {

    // Valores por defecto (técnica Pomodoro) y umbrales de jornada
    public static final int CONCENTRACION_DEFECTO_MIN = 25;
    public static final int PAUSA_CORTA_DEFECTO_MIN = 5;
    public static final int PAUSA_LARGA_DEFECTO_MIN = 15;
    public static final int CICLOS_PARA_PAUSA_LARGA_DEFECTO = 4;
    public static final int LIMITE_ADVERTENCIA_DEFECTO_MIN = 150;
    public static final int LIMITE_CRITICO_DEFECTO_MIN = 240;

    /** Si sobra menos que esto al fragmentar, se suma al último bloque. */
    private static final int MINUTOS_MINIMOS_ULTIMO_BLOQUE = 5;

    private static final String[] PAUSAS_ACTIVAS = {
        "levántate y estira cuello y hombros",
        "camina unos minutos lejos de la pantalla",
        "toma agua",
        "mira un punto lejano durante 20 segundos para descansar la vista",
        "haz 5 respiraciones profundas"
    };

    private final int concentracionMin;
    private final int pausaCortaMin;
    private final int pausaLargaMin;
    private final int ciclosParaPausaLarga;
    private final int limiteAdvertenciaMin;
    private final int limiteCriticoMin;

    private final List<CicloConcentracion> ciclos = new ArrayList<>();
    private final List<ListenerSaturacion> listeners = new ArrayList<>();

    private int indiceActual = -1;
    private long segundosConcentrados = 0;   // acumulado de la jornada
    private boolean avisoPrevioEmitido = false;
    private boolean advertenciaJornadaEmitida = false;
    private boolean criticaJornadaEmitida = false;
    private int contadorSugerencias = 0;
    private NivelAlerta saturacionReportada = NivelAlerta.INFO;

    /** Configuración por defecto: Pomodoro 25/5, pausa larga de 15 cada 4 ciclos. */
    public GestionSaturacion() {
        this(CONCENTRACION_DEFECTO_MIN, PAUSA_CORTA_DEFECTO_MIN, PAUSA_LARGA_DEFECTO_MIN,
                CICLOS_PARA_PAUSA_LARGA_DEFECTO, LIMITE_ADVERTENCIA_DEFECTO_MIN, LIMITE_CRITICO_DEFECTO_MIN);
    }

    public GestionSaturacion(int concentracionMin, int pausaCortaMin, int pausaLargaMin,
                             int ciclosParaPausaLarga, int limiteAdvertenciaMin, int limiteCriticoMin) {
        if (concentracionMin <= 0 || pausaCortaMin <= 0 || pausaLargaMin <= 0 || ciclosParaPausaLarga <= 0) {
            throw new IllegalArgumentException("Las duraciones y la cantidad de ciclos deben ser mayores que cero.");
        }
        if (limiteAdvertenciaMin <= 0 || limiteCriticoMin <= limiteAdvertenciaMin) {
            throw new IllegalArgumentException("El límite crítico debe ser mayor que el de advertencia, y ambos positivos.");
        }
        this.concentracionMin = concentracionMin;
        this.pausaCortaMin = pausaCortaMin;
        this.pausaLargaMin = pausaLargaMin;
        this.ciclosParaPausaLarga = ciclosParaPausaLarga;
        this.limiteAdvertenciaMin = limiteAdvertenciaMin;
        this.limiteCriticoMin = limiteCriticoMin;
    }

    // ---------- Conexión con la interfaz ----------

    public void agregarListener(ListenerSaturacion listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removerListener(ListenerSaturacion listener) {
        listeners.remove(listener);
    }

    // ---------- RF-07: fragmentación en ciclos ----------

    /**
     * Fragmenta el tiempo total de estudio en ciclos de concentración.
     * - Bloques de concentracionMin; el resto forma un último bloque más corto,
     *   salvo que sobren menos de 5 minutos, que se suman al último bloque.
     * - Pausa corta tras cada ciclo y pausa larga tras cada N-ésimo ciclo.
     * - El último ciclo no lleva pausa.
     *
     * @return lista no modificable con los ciclos creados
     */
    public List<CicloConcentracion> planificar(String actividad, int minutosTotales) {
        if (actividad == null || actividad.trim().isEmpty()) {
            throw new IllegalArgumentException("La actividad no puede estar vacía.");
        }
        if (minutosTotales <= 0) {
            throw new IllegalArgumentException("El tiempo de estudio debe ser mayor que cero.");
        }
        if (hayCicloActivo()) {
            throw new IllegalStateException("No se puede replanificar mientras hay un ciclo en curso.");
        }

        List<Integer> duraciones = new ArrayList<>();
        int completos = minutosTotales / concentracionMin;
        int resto = minutosTotales % concentracionMin;
        if (completos == 0) {
            duraciones.add(minutosTotales);
        } else {
            for (int i = 0; i < completos; i++) {
                duraciones.add(concentracionMin);
            }
            if (resto > 0) {
                if (resto < MINUTOS_MINIMOS_ULTIMO_BLOQUE) {
                    int ultimo = duraciones.size() - 1;
                    duraciones.set(ultimo, duraciones.get(ultimo) + resto);
                } else {
                    duraciones.add(resto);
                }
            }
        }

        ciclos.clear();
        indiceActual = -1;
        int total = duraciones.size();
        for (int i = 0; i < total; i++) {
            TipoPausa tipo;
            int pausaMin;
            if (i == total - 1) {
                tipo = TipoPausa.NINGUNA;
                pausaMin = 0;
            } else if ((i + 1) % ciclosParaPausaLarga == 0) {
                tipo = TipoPausa.LARGA;
                pausaMin = pausaLargaMin;
            } else {
                tipo = TipoPausa.CORTA;
                pausaMin = pausaCortaMin;
            }
            ciclos.add(new CicloConcentracion(i + 1, actividad, duraciones.get(i), tipo, pausaMin));
        }
        return getCiclos();
    }

    /** Duración total del plan: bloques de concentración más todas las pausas. */
    public int getDuracionTotalPlanificadaMin() {
        int total = 0;
        for (CicloConcentracion c : ciclos) {
            total += c.getDuracionConcentracionMin() + c.getDuracionPausaMin();
        }
        return total;
    }

    // ---------- Control del flujo de ciclos ----------

    /**
     * Indica si se puede iniciar el siguiente ciclo. Es falso si no hay plan,
     * si hay un ciclo en curso, si hay una pausa obligatoria pendiente o si el
     * plan ya terminó.
     */
    public boolean puedeIniciarCiclo() {
        return !ciclos.isEmpty() && !hayCicloActivo() && indiceActual + 1 < ciclos.size();
    }

    /**
     * Inicia el siguiente ciclo del plan.
     *
     * @throws IllegalStateException si no hay plan, hay una pausa obligatoria
     *         pendiente, hay un ciclo en curso o el plan ya terminó
     */
    public CicloConcentracion iniciarSiguienteCiclo() {
        if (ciclos.isEmpty()) {
            throw new IllegalStateException("No hay un plan de estudio. Use planificar() primero.");
        }
        if (hayCicloActivo()) {
            String mensaje = isPausaObligatoriaPendiente()
                    ? "Hay una pausa obligatoria pendiente: complétala antes de iniciar otro ciclo."
                    : "Ya hay un ciclo de concentración en curso.";
            emitirAlerta(NivelAlerta.ADVERTENCIA, mensaje);
            throw new IllegalStateException(mensaje);
        }
        if (indiceActual + 1 >= ciclos.size()) {
            throw new IllegalStateException("Todos los ciclos del plan ya fueron completados.");
        }

        indiceActual++;
        CicloConcentracion ciclo = ciclos.get(indiceActual);
        avisoPrevioEmitido = false;

        EstadoCiclo anterior = ciclo.getEstado();
        ciclo.iniciarConcentracion();
        notificarCambio(ciclo, anterior, ciclo.getEstado());
        emitirAlerta(NivelAlerta.INFO, "Ciclo " + ciclo.getNumero() + " de " + ciclos.size()
                + " iniciado: " + ciclo.getDuracionConcentracionMin() + " min de concentración.");
        return ciclo;
    }

    /**
     * Hace avanzar el reloj. La interfaz lo llama periódicamente (p. ej. 1 segundo).
     * Al agotarse la concentración pasa solo a la pausa obligatoria, y al
     * agotarse la pausa finaliza el ciclo; el siguiente ciclo NO se inicia solo.
     */
    public void avanzarSegundos(int segundos) {
        if (segundos < 0) {
            throw new IllegalArgumentException("Los segundos no pueden ser negativos.");
        }
        int restante = segundos;
        while (restante > 0 && hayCicloActivo()) {
            CicloConcentracion ciclo = ciclos.get(indiceActual);
            int consumir = Math.min(restante, ciclo.getSegundosRestantes());
            ciclo.restarSegundos(consumir);
            restante -= consumir;

            if (ciclo.getEstado() == EstadoCiclo.EN_CONCENTRACION) {
                segundosConcentrados += consumir;
                verificarLimitesJornada();
                verificarAvisoPrevio(ciclo);
            }
            for (ListenerSaturacion l : new ArrayList<>(listeners)) {
                l.onTick(ciclo);
            }
            if (ciclo.getSegundosRestantes() == 0) {
                cambiarDeFase(ciclo);
            }
        }
    }

    private void cambiarDeFase(CicloConcentracion ciclo) {
        EstadoCiclo anterior = ciclo.getEstado();
        if (anterior == EstadoCiclo.EN_CONCENTRACION) {
            ciclo.terminarConcentracion();
            notificarCambio(ciclo, anterior, ciclo.getEstado());
            if (ciclo.getTipoPausa() == TipoPausa.NINGUNA) {
                finalizarPlan();
            } else {
                emitirAlerta(NivelAlerta.ADVERTENCIA, "Fin del ciclo " + ciclo.getNumero()
                        + ". Pausa obligatoria (" + ciclo.getTipoPausa().getEtiqueta().toLowerCase()
                        + ") de " + ciclo.getDuracionPausaMin() + " min. Sugerencia: " + sugerirPausaActiva() + ".");
            }
        } else if (anterior == EstadoCiclo.EN_PAUSA) {
            ciclo.terminarPausa();
            notificarCambio(ciclo, anterior, ciclo.getEstado());
            emitirAlerta(NivelAlerta.INFO, "Pausa terminada. Ya puedes iniciar el ciclo "
                    + (ciclo.getNumero() + 1) + ".");
        }
    }

    private void finalizarPlan() {
        emitirAlerta(NivelAlerta.INFO, "Plan de estudio completado. ¡Buen trabajo!");
        for (ListenerSaturacion l : new ArrayList<>(listeners)) {
            l.onPlanFinalizado();
        }
    }

    // ---------- RF-08: alertas por saturación cognitiva ----------

    /** Aviso cuando queda el 20 % del bloque de concentración (una vez por ciclo). */
    private void verificarAvisoPrevio(CicloConcentracion ciclo) {
        int umbral = (int) (ciclo.getSegundosFase() * 0.2);
        int restantes = ciclo.getSegundosRestantes();
        if (!avisoPrevioEmitido && restantes > 0 && restantes <= umbral) {
            avisoPrevioEmitido = true;
            emitirAlerta(NivelAlerta.INFO, "Quedan aproximadamente " + ((restantes + 59) / 60)
                    + " min del ciclo " + ciclo.getNumero() + ". Prepárate para la pausa.");
        }
    }

    /** Alertas por minutos de concentración acumulados en la jornada (una vez cada nivel). */
    private void verificarLimitesJornada() {
        int minutos = getMinutosConcentrados();
        if (!advertenciaJornadaEmitida && minutos >= limiteAdvertenciaMin) {
            advertenciaJornadaEmitida = true;
            emitirAlerta(NivelAlerta.ADVERTENCIA, "Llevas " + minutos
                    + " min de concentración hoy: riesgo de saturación cognitiva. Considera descansar más.");
        }
        if (!criticaJornadaEmitida && minutos >= limiteCriticoMin) {
            criticaJornadaEmitida = true;
            emitirAlerta(NivelAlerta.CRITICA, "Llevas " + minutos
                    + " min de concentración hoy: saturación cognitiva alta. Se recomienda detener el estudio.");
        }
    }

    /** Nivel de saturación de la jornada según los minutos concentrados. */
    public NivelAlerta getNivelSaturacion() {
        int minutos = getMinutosConcentrados();
        if (minutos >= limiteCriticoMin) {
            return NivelAlerta.CRITICA;
        }
        if (minutos >= limiteAdvertenciaMin || saturacionReportada == NivelAlerta.ADVERTENCIA) {
            return NivelAlerta.ADVERTENCIA;
        }
        return saturacionReportada;
    }

    public void reportarSaturacion(NivelAlerta nivel) {
        if (nivel == null) {
            throw new IllegalArgumentException("El nivel de saturación reportado es obligatorio.");
        }
        saturacionReportada = nivel;
        String mensaje;
        switch (nivel) {
            case CRITICA:
                mensaje = "El estudiante reporta saturación alta. Considera detener el estudio y descansar.";
                break;
            case ADVERTENCIA:
                mensaje = "El estudiante reporta sentirse saturado. Considera tomar una pausa.";
                break;
            default:
                mensaje = "El estudiante reporta un nivel de saturación normal.";
                break;
        }
        emitirAlerta(nivel, mensaje);
    }

    /** Sugerencia de pausa activa (rota entre varias opciones). */
    public String sugerirPausaActiva() {
        String s = PAUSAS_ACTIVAS[contadorSugerencias % PAUSAS_ACTIVAS.length];
        contadorSugerencias++;
        return s;
    }

    // ---------- Consultas ----------

    public CicloConcentracion getCicloActual() {
        return indiceActual >= 0 ? ciclos.get(indiceActual) : null;
    }

    public List<CicloConcentracion> getCiclos() {
        return Collections.unmodifiableList(ciclos);
    }

    public int getMinutosConcentrados() {
        return (int) (segundosConcentrados / 60);
    }

    public boolean isPausaObligatoriaPendiente() {
        return indiceActual >= 0 && ciclos.get(indiceActual).getEstado() == EstadoCiclo.EN_PAUSA;
    }

    public boolean isPlanCompletado() {
        return !ciclos.isEmpty() && ciclos.get(ciclos.size() - 1).getEstado() == EstadoCiclo.FINALIZADO;
    }

    /** Reinicia el acumulado de la jornada (por ejemplo, al empezar un nuevo día). */
    public void reiniciarJornada() {
        segundosConcentrados = 0;
        advertenciaJornadaEmitida = false;
        criticaJornadaEmitida = false;
        saturacionReportada = NivelAlerta.INFO;
    }

    // ---------- Utilidades internas ----------

    private boolean hayCicloActivo() {
        if (indiceActual < 0) {
            return false;
        }
        EstadoCiclo e = ciclos.get(indiceActual).getEstado();
        return e == EstadoCiclo.EN_CONCENTRACION || e == EstadoCiclo.EN_PAUSA;
    }

    private void emitirAlerta(NivelAlerta nivel, String mensaje) {
        Alerta alerta = new Alerta(nivel, mensaje);
        for (ListenerSaturacion l : new ArrayList<>(listeners)) {
            l.onAlerta(alerta);
        }
    }

    private void notificarCambio(CicloConcentracion ciclo, EstadoCiclo anterior, EstadoCiclo nuevo) {
        for (ListenerSaturacion l : new ArrayList<>(listeners)) {
            l.onCambioEstado(ciclo, anterior, nuevo);
        }
    }
}
