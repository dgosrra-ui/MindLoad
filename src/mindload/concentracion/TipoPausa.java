package mindload.concentracion;

/**
 * Tipo de pausa activa que sigue a un ciclo de concentración.
 * El último ciclo de un plan no tiene pausa (NINGUNA).
 *
 * @author Integrante 4 - Lógica de negocio y sistema de alertas
 */
public enum TipoPausa {
    NINGUNA("Sin pausa"),
    CORTA("Pausa corta"),
    LARGA("Pausa larga");

    private final String etiqueta;

    TipoPausa(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
