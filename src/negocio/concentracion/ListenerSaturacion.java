package negocio.concentracion;

/**
 * Contrato entre la lógica de negocio (Integrante 4) y la interfaz gráfica
 * (Integrante 3). La interfaz implementa esta interfaz y se registra con
 * {@link GestionSaturacion#agregarListener(ListenerSaturacion)}; así la lógica
 * no depende de Swing ni de JavaFX.
 *
 * Los métodos onTick y onPlanFinalizado son opcionales (default).
 *
 * @author Integrante 4 - Lógica de negocio y sistema de alertas
 */
public interface ListenerSaturacion {

    /** Se llama cada vez que un ciclo cambia de estado. */
    void onCambioEstado(CicloConcentracion ciclo, EstadoCiclo anterior, EstadoCiclo nuevo);

    /** Se llama cuando el sistema genera una alerta para el usuario. */
    void onAlerta(Alerta alerta);

    /** Se llama cuando avanza el tiempo del ciclo (para refrescar el contador). */
    default void onTick(CicloConcentracion ciclo) {
    }

    /** Se llama una sola vez cuando se completa el último ciclo del plan. */
    default void onPlanFinalizado() {
    }
}
