package mindload.concentracion;

import java.time.LocalDateTime;

/**
 * Mensaje que el sistema genera para el usuario (aviso previo, pausa obligatoria,
 * riesgo de saturación cognitiva, etc.). Es inmutable.
 *
 * @author Integrante 4 - Lógica de negocio y sistema de alertas
 */
public final class Alerta {

    private final NivelAlerta nivel;
    private final String mensaje;
    private final LocalDateTime momento;

    public Alerta(NivelAlerta nivel, String mensaje) {
        if (nivel == null) {
            throw new IllegalArgumentException("El nivel de la alerta es obligatorio.");
        }
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje de la alerta es obligatorio.");
        }
        this.nivel = nivel;
        this.mensaje = mensaje.trim();
        this.momento = LocalDateTime.now();
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    @Override
    public String toString() {
        return "[" + nivel + "] " + mensaje;
    }
}
