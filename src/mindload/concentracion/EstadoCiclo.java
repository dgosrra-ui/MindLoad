package mindload.concentracion;

/**
 * Estados por los que pasa un ciclo de concentración.
 * Se corresponden con los estados que muestra la interfaz (RF-05/RF-06):
 * pendiente, en curso (concentración o pausa) y completado.
 *
 * @author Integrante 4 - Lógica de negocio y sistema de alertas
 */
public enum EstadoCiclo {
    PENDIENTE("Pendiente"),
    EN_CONCENTRACION("En curso"),
    EN_PAUSA("En pausa"),
    FINALIZADO("Completado");

    private final String etiqueta;

    EstadoCiclo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto listo para mostrar en la interfaz. */
    public String getEtiqueta() {
        return etiqueta;
    }
}
