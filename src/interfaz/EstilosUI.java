package interfaz;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import negocio.concentracion.EstadoCiclo;
import negocio.concentracion.NivelAlerta;
import modelo.Tarea;

public final class EstilosUI {

    public static final Color FONDO_APP = new Color(244, 246, 250);
    public static final Color FONDO_TARJETA = Color.WHITE;
    public static final Color ENCABEZADO_OSCURO = new Color(30, 41, 59);
    public static final Color BORDE_SUAVE = new Color(214, 220, 230);

    public static final Color PRIMARIO = new Color(37, 99, 235);
    public static final Color SECUNDARIO = new Color(71, 85, 105);
    public static final Color EXITO = new Color(22, 163, 74);
    public static final Color ADVERTENCIA = new Color(217, 119, 6);
    public static final Color PELIGRO = new Color(220, 38, 38);
    public static final Color MORADO = new Color(124, 58, 237);

    public static final Color TEXTO_PRINCIPAL = new Color(30, 41, 59);
    public static final Color TEXTO_SECUNDARIO = new Color(100, 116, 139);

    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FUENTE_NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FUENTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FUENTE_RELOJ = new Font("Monospaced", Font.BOLD, 48);

    private EstilosUI() {
    }

    public static JButton crearBoton(String texto, Color colorFondo) {
        JButton boton = new JButton(texto);
        boton.setUI(new BasicButtonUI());
        boton.setBackground(colorFondo);
        boton.setForeground(Color.WHITE);
        boton.setFont(FUENTE_NEGRITA);
        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    public static JPanel crearTarjeta(String titulo) {
        JPanel panel = new JPanel();
        panel.setBackground(FONDO_TARJETA);
        TitledBorder bordeTitulo = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDE_SUAVE, 1, true),
                " " + titulo + " ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FUENTE_SUBTITULO,
                TEXTO_PRINCIPAL
        );
        panel.setBorder(BorderFactory.createCompoundBorder(
                bordeTitulo,
                BorderFactory.createEmptyBorder(8, 10, 10, 10)
        ));
        return panel;
    }

    public static JLabel crearInsignia(String texto, Color fondo, Color textoColor) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(fondo);
        label.setForeground(textoColor);
        label.setFont(FUENTE_NEGRITA);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }

    public static void configurarTabla(JTable tabla) {
        tabla.setRowHeight(28);
        tabla.setFont(FUENTE_NORMAL);
        tabla.getTableHeader().setFont(FUENTE_NEGRITA);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(BORDE_SUAVE);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));
    }

    public static String formatearEstadoTarea(Tarea.Estado estado) {
        if (estado == null) {
            return "Pendiente";
        }
        switch (estado) {
            case EN_CURSO:
                return "En curso";
            case COMPLETADA:
                return "Completada";
            case PENDIENTE:
            default:
                return "Pendiente";
        }
    }

    public static Color colorEstadoCiclo(EstadoCiclo estado) {
        if (estado == null) {
            return TEXTO_SECUNDARIO;
        }
        switch (estado) {
            case EN_CONCENTRACION:
                return PRIMARIO;
            case EN_PAUSA:
                return ADVERTENCIA;
            case FINALIZADO:
                return EXITO;
            case PENDIENTE:
            default:
                return TEXTO_SECUNDARIO;
        }
    }

    public static Color colorFondoAlerta(NivelAlerta nivel) {
        if (nivel == null) {
            return new Color(239, 246, 255);
        }
        switch (nivel) {
            case CRITICA:
                return new Color(254, 226, 226);
            case ADVERTENCIA:
                return new Color(254, 243, 199);
            case INFO:
            default:
                return new Color(239, 246, 255);
        }
    }

    public static Color colorTextoAlerta(NivelAlerta nivel) {
        if (nivel == null) {
            return PRIMARIO;
        }
        switch (nivel) {
            case CRITICA:
                return new Color(153, 27, 27);
            case ADVERTENCIA:
                return new Color(146, 64, 14);
            case INFO:
            default:
                return new Color(30, 64, 175);
        }
    }

    public static DefaultTableCellRenderer crearRendererEstado() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel celda = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                celda.setHorizontalAlignment(SwingConstants.CENTER);
                celda.setFont(FUENTE_NEGRITA);

                if (isSelected) {
                    return celda;
                }

                String texto = value == null ? "" : value.toString();
                if ("En curso".equalsIgnoreCase(texto)) {
                    celda.setBackground(new Color(219, 234, 254));
                    celda.setForeground(new Color(30, 64, 175));
                } else if ("En pausa".equalsIgnoreCase(texto)) {
                    celda.setBackground(new Color(254, 243, 199));
                    celda.setForeground(new Color(146, 64, 14));
                } else if ("Completada".equalsIgnoreCase(texto) || "Completado".equalsIgnoreCase(texto)) {
                    celda.setBackground(new Color(220, 252, 231));
                    celda.setForeground(new Color(22, 101, 52));
                } else {
                    celda.setBackground(new Color(241, 245, 249));
                    celda.setForeground(new Color(71, 85, 105));
                }
                return celda;
            }
        };
    }
}