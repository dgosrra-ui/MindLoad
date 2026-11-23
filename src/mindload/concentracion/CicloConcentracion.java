package mindload.concentracion;

/**
 * Representa un bloque de estudio (concentración) seguido de una pausa activa.
 * Controla su propio estado y el tiempo restante de la fase actual.
 *
 * Transiciones válidas:
 * PENDIENTE -> EN_CONCENTRACION -> EN_PAUSA -> FINALIZADO
 * PENDIENTE -> EN_CONCENTRACION -> FINALIZADO (último ciclo, sin pausa)
 *
 * @author Integrante 4 - Lógica de negocio y sistema de alertas
 */
public class CicloConcentracion {

    private final int numero;
    private final String actividad;
    private final int duracionConcentracionMin;
    private final TipoPausa tipoPausa;
    private final int duracionPausaMin;

    private EstadoCiclo estado;
    private int segundosFase;       // duración total de la fase actual
    private int segundosRestantes;  // tiempo que falta de la fase actual

    public CicloConcentracion(int numero, String actividad, int duracionConcentracionMin,
                              TipoPausa tipoPausa, int duracionPausaMin) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de ciclo debe ser mayor que cero.");
        }
        if (actividad == null || actividad.trim().isEmpty()) {
            throw new IllegalArgumentException("La actividad del ciclo es obligatoria.");
        }
        if (duracionConcentracionMin <= 0) {
            throw new IllegalArgumentException("La duración de concentración debe ser mayor que cero.");
        }
        if (tipoPausa == null) {
            throw new IllegalArgumentException("El tipo de pausa es obligatorio.");
        }
        if (tipoPausa == TipoPausa.NINGUNA && duracionPausaMin != 0) {
            throw new IllegalArgumentException("Un ciclo sin pausa debe tener duración de pausa 0.");
        }
        if (tipoPausa != TipoPausa.NINGUNA && duracionPausaMin <= 0) {
            throw new IllegalArgumentException("La duración de la pausa debe ser mayor que cero.");
        }
        this.numero = numero;
        this.actividad = actividad.trim();
        this.duracionConcentracionMin = duracionConcentracionMin;
        this.tipoPausa = tipoPausa;
        this.duracionPausaMin = duracionPausaMin;
        this.estado = EstadoCiclo.PENDIENTE;
        this.segundosFase = 0;
        this.segundosRestantes = 0;
    }

    // ---------- Transiciones de estado ----------

    /** PENDIENTE -> EN_CONCENTRACION. */
    public void iniciarConcentracion() {
        exigirEstado(EstadoCiclo.PENDIENTE, "iniciar la concentración");
        estado = EstadoCiclo.EN_CONCENTRACION;
        segundosFase = duracionConcentracionMin * 60;
        segundosRestantes = segundosFase;
    }

    /**
     * EN_CONCENTRACION -> EN_PAUSA, o -> FINALIZADO si el ciclo no tiene pausa.
     */
    public void terminarConcentracion() {
        exigirEstado(EstadoCiclo.EN_CONCENTRACION, "terminar la concentración");
        if (tipoPausa == TipoPausa.NINGUNA) {
            estado = EstadoCiclo.FINALIZADO;
            segundosFase = 0;
            segundosRestantes = 0;
        } else {
            estado = EstadoCiclo.EN_PAUSA;
            segundosFase = duracionPausaMin * 60;
            segundosRestantes = segundosFase;
        }
    }

    /** EN_PAUSA -> FINALIZADO. */
    public void terminarPausa() {
        exigirEstado(EstadoCiclo.EN_PAUSA, "terminar la pausa");
        estado = EstadoCiclo.FINALIZADO;
        segundosFase = 0;
        segundosRestantes = 0;
    }

    /**
     * Descuenta segundos de la fase actual (sin bajar de cero).
     * No cambia de estado: eso lo decide GestionSaturacion.
     */
    public void restarSegundos(int segundos) {
        if (segundos < 0) {
            throw new IllegalArgumentException("Los segundos a restar no pueden ser negativos.");
        }
        if (estado != EstadoCiclo.EN_CONCENTRACION && estado != EstadoCiclo.EN_PAUSA) {
            throw new IllegalStateException("El ciclo " + numero + " no tiene una fase en curso.");
        }
        segundosRestantes = Math.max(0, segundosRestantes - segundos);
    }

    private void exigirEstado(EstadoCiclo esperado, String accion) {
        if (estado != esperado) {
            throw new IllegalStateException("No se puede " + accion + " del ciclo " + numero
                    + ": estado actual " + estado + ", se esperaba " + esperado + ".");
        }
    }

    // ---------- Consultas (para la interfaz) ----------

    public int getNumero() {
        return numero;
    }

    public String getActividad() {
        return actividad;
    }

    public int getDuracionConcentracionMin() {
        return duracionConcentracionMin;
    }

    public TipoPausa getTipoPausa() {
        return tipoPausa;
    }

    public int getDuracionPausaMin() {
        return duracionPausaMin;
    }

    public EstadoCiclo getEstado() {
        return estado;
    }

    public int getSegundosFase() {
        return segundosFase;
    }

    public int getSegundosRestantes() {
        return segundosRestantes;
    }

    /** Tiempo restante de la fase actual con formato mm:ss para el contador. */
    public String getTiempoRestanteFormateado() {
        return String.format("%02d:%02d", segundosRestantes / 60, segundosRestantes % 60);
    }

    /** Avance de la fase actual entre 0.0 y 1.0 (útil para una barra de progreso). */
    public double getProgreso() {
        if (estado == EstadoCiclo.FINALIZADO) {
            return 1.0;
        }
        if (segundosFase == 0) {
            return 0.0;
        }
        return (segundosFase - segundosRestantes) / (double) segundosFase;
    }

    @Override
    public String toString() {
        return "Ciclo " + numero + " [" + actividad + "] " + duracionConcentracionMin + " min + "
                + tipoPausa.getEtiqueta() + (duracionPausaMin > 0 ? " (" + duracionPausaMin + " min)" : "")
                + " - " + estado.getEtiqueta();
    }
}
