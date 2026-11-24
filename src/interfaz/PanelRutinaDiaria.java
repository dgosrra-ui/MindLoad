package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import negocio.concentracion.Alerta;
import negocio.concentracion.CicloConcentracion;
import negocio.concentracion.EstadoCiclo;
import negocio.concentracion.GestionSaturacion;
import negocio.concentracion.ListenerSaturacion;
import negocio.concentracion.NivelAlerta;
import modelo.ActividadProgramada;
import modelo.Materia;
import modelo.Tarea;
import negocio.GestorCargaAcademica;
import negocio.ValidadorDatos;

public class PanelRutinaDiaria extends JPanel implements ListenerSaturacion {

    private static final String OPCION_PERSONALIZADA = "Actividad personalizada";
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final GestorCargaAcademica gestor;
    private final GestionSaturacion gestionSaturacion;
    private final ValidadorDatos validador;
    private final Runnable onDatosActualizados;

    private final Timer temporizador;
    private boolean relojPausado = false;
    private boolean actualizandoCombo = false;
    private Tarea tareaEnRutina = null;

    private JComboBox<Object> cmbSelectorTarea;
    private JTextField txtActividad;
    private JTextField txtMinutosTotales;
    private JLabel lblResumenPlan;
    private JComboBox<String> cmbSaturacionReportada;

    private JLabel lblFaseActual;
    private JLabel lblReloj;
    private JProgressBar barraProgreso;
    private JLabel lblDetalleCiclo;
    private JLabel lblResumenJornada;
    private JLabel lblBannerAlerta;

    private JButton btnIniciarCiclo;
    private JButton btnPausarReanudar;
    private JButton btnAvanzarDemo;
    private JButton btnReiniciarJornada;

    private DefaultTableModel modeloCiclos;
    private JTable tablaCiclos;
    private DefaultTableModel modeloAgendaHoy;
    private DefaultListModel<String> modeloAlertas;

    public PanelRutinaDiaria(GestorCargaAcademica gestor,
                             GestionSaturacion gestionSaturacion,
                             Runnable onDatosActualizados) {
        this.gestor = gestor;
        this.gestionSaturacion = gestionSaturacion;
        this.validador = new ValidadorDatos();
        this.onDatosActualizados = onDatosActualizados;

        this.gestionSaturacion.agregarListener(this);
        this.temporizador = new Timer(1000, e -> ejecutarTickSegundo());

        setLayout(new BorderLayout(12, 12));
        setBackground(EstilosUI.FONDO_APP);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearPanelPlanificacion(), BorderLayout.NORTH);
        add(crearPanelCentral(), BorderLayout.CENTER);

        refrescarSelectorTareas();
        actualizarTablaAgendaHoy();
        actualizarTablaCiclos();
        actualizarPanelReloj();
        actualizarEstadoBotones();
    }

    private JPanel crearPanelPlanificacion() {
        JPanel tarjeta = EstilosUI.crearTarjeta("Planificación de la Rutina Diaria (RF-05)");
        tarjeta.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        tarjeta.add(new JLabel("Origen:"), gbc);

        cmbSelectorTarea = new JComboBox<>();
        cmbSelectorTarea.setFont(EstilosUI.FUENTE_NORMAL);
        cmbSelectorTarea.setPreferredSize(new Dimension(230, 26));
        cmbSelectorTarea.addActionListener(e -> alCambiarSelectorTarea());
        gbc.gridx = 1;
        gbc.weightx = 0.3;
        tarjeta.add(cmbSelectorTarea, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        tarjeta.add(new JLabel("Actividad:"), gbc);

        txtActividad = new JTextField();
        txtActividad.setFont(EstilosUI.FUENTE_NORMAL);
        gbc.gridx = 3;
        gbc.weightx = 0.45;
        tarjeta.add(txtActividad, gbc);

        gbc.gridx = 4;
        gbc.weightx = 0;
        tarjeta.add(new JLabel("Minutos:"), gbc);

        txtMinutosTotales = new JTextField("50", 5);
        txtMinutosTotales.setFont(EstilosUI.FUENTE_NORMAL);
        gbc.gridx = 5;
        gbc.weightx = 0.1;
        tarjeta.add(txtMinutosTotales, gbc);

        JButton btnPlanificar = EstilosUI.crearBoton("Generar rutina", EstilosUI.PRIMARIO);
        btnPlanificar.addActionListener(e -> planificarDesdeCampos());
        gbc.gridx = 6;
        gbc.weightx = 0;
        tarjeta.add(btnPlanificar, gbc);

        JButton btnRutinaHoy = EstilosUI.crearBoton("Planificar carga de hoy", EstilosUI.MORADO);
        btnRutinaHoy.addActionListener(e -> planificarCargaDelDia());
        gbc.gridx = 7;
        tarjeta.add(btnRutinaHoy, gbc);

        JLabel lblReporte = new JLabel("¿Cómo te sientes?");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        tarjeta.add(lblReporte, gbc);

        cmbSaturacionReportada = new JComboBox<>(new String[]{
            "Normal", "Me siento saturado", "Saturación alta"
        });
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        tarjeta.add(cmbSaturacionReportada, gbc);

        JButton btnReportarSaturacion = EstilosUI.crearBoton("Reportar saturación", EstilosUI.ADVERTENCIA);
        btnReportarSaturacion.addActionListener(e -> reportarSaturacion());
        gbc.gridx = 3;
        gbc.gridwidth = 2;
        tarjeta.add(btnReportarSaturacion, gbc);

        lblResumenPlan = new JLabel("Sin rutina planificada. Configura una actividad o elige una tarea para fragmentarla en ciclos Pomodoro (25/5/15 min).");
        lblResumenPlan.setFont(EstilosUI.FUENTE_PEQUENA);
        lblResumenPlan.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 8;
        tarjeta.add(lblResumenPlan, gbc);

        return tarjeta;
    }

    private JSplitPane crearPanelCentral() {
        JPanel panelIzquierdo = crearPanelControlYReloj();
        JPanel panelDerecho = crearPanelRutinaYAlertas();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzquierdo, panelDerecho);
        split.setResizeWeight(0.38);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(EstilosUI.FONDO_APP);
        return split;
    }

    private JPanel crearPanelControlYReloj() {
        JPanel tarjeta = EstilosUI.crearTarjeta("Control de Ciclos y Temporizador (RF-06)");
        tarjeta.setLayout(new BorderLayout(10, 10));
        tarjeta.setPreferredSize(new Dimension(370, 0));

        // Indicadores superiores
        JPanel panelSuperior = new JPanel(new BorderLayout(6, 6));
        panelSuperior.setBackground(EstilosUI.FONDO_TARJETA);

        lblFaseActual = EstilosUI.crearInsignia("SIN CICLO ACTIVO",
                new Color(226, 232, 240), EstilosUI.TEXTO_PRINCIPAL);

        lblDetalleCiclo = new JLabel("Genera una rutina e inicia el primer ciclo.", SwingConstants.CENTER);
        lblDetalleCiclo.setFont(EstilosUI.FUENTE_NORMAL);
        lblDetalleCiclo.setForeground(EstilosUI.TEXTO_SECUNDARIO);

        panelSuperior.add(lblFaseActual, BorderLayout.NORTH);
        panelSuperior.add(lblDetalleCiclo, BorderLayout.CENTER);

        // Reloj central y barra de progreso
        JPanel panelReloj = new JPanel(new GridBagLayout());
        panelReloj.setBackground(new Color(248, 250, 252));
        panelReloj.setBorder(BorderFactory.createLineBorder(EstilosUI.BORDE_SUAVE, 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 16, 6, 16);

        lblReloj = new JLabel("00:00", SwingConstants.CENTER);
        lblReloj.setFont(EstilosUI.FUENTE_RELOJ);
        lblReloj.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        gbc.gridy = 0;
        panelReloj.add(lblReloj, gbc);

        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        barraProgreso.setValue(0);
        barraProgreso.setPreferredSize(new Dimension(220, 22));
        barraProgreso.setFont(EstilosUI.FUENTE_PEQUENA);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 20, 10, 20);
        panelReloj.add(barraProgreso, gbc);

        lblResumenJornada = new JLabel("Concentrado hoy: 0 min | Saturación: Normal", SwingConstants.CENTER);
        lblResumenJornada.setFont(EstilosUI.FUENTE_NEGRITA);
        lblResumenJornada.setForeground(EstilosUI.TEXTO_PRINCIPAL);
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 12, 12, 12);
        panelReloj.add(lblResumenJornada, gbc);

        // Botones de control + Banner de última alerta
        JPanel panelInferior = new JPanel(new BorderLayout(8, 8));
        panelInferior.setBackground(EstilosUI.FONDO_TARJETA);

        JPanel panelBotones = new JPanel(new GridBagLayout());
        panelBotones.setBackground(EstilosUI.FONDO_TARJETA);
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.fill = GridBagConstraints.HORIZONTAL;
        gbcB.weightx = 0.5;
        gbcB.insets = new Insets(4, 4, 4, 4);

        btnIniciarCiclo = EstilosUI.crearBoton("Iniciar siguiente ciclo", EstilosUI.EXITO);
        btnIniciarCiclo.addActionListener(e -> iniciarSiguienteCiclo());
        gbcB.gridx = 0;
        gbcB.gridy = 0;
        gbcB.gridwidth = 2;
        panelBotones.add(btnIniciarCiclo, gbcB);

        btnPausarReanudar = EstilosUI.crearBoton("Pausar reloj", EstilosUI.ADVERTENCIA);
        btnPausarReanudar.addActionListener(e -> alternarPausaReloj());
        gbcB.gridx = 0;
        gbcB.gridy = 1;
        gbcB.gridwidth = 1;
        panelBotones.add(btnPausarReanudar, gbcB);

        btnAvanzarDemo = EstilosUI.crearBoton("+1 min (Demo)", EstilosUI.SECUNDARIO);
        btnAvanzarDemo.setToolTipText("Adelanta 60 segundos del ciclo actual para pruebas rápidas");
        btnAvanzarDemo.addActionListener(e -> avanzarUnMinutoDemo());
        gbcB.gridx = 1;
        gbcB.gridy = 1;
        panelBotones.add(btnAvanzarDemo, gbcB);

        btnReiniciarJornada = EstilosUI.crearBoton("Reiniciar acumulado de jornada", EstilosUI.PELIGRO);
        btnReiniciarJornada.addActionListener(e -> reiniciarJornada());
        gbcB.gridx = 0;
        gbcB.gridy = 2;
        gbcB.gridwidth = 2;
        panelBotones.add(btnReiniciarJornada, gbcB);

        lblBannerAlerta = new JLabel("<html><i>Las alertas de concentración, pausas obligatorias y saturación aparecerán aquí.</i></html>");
        lblBannerAlerta.setOpaque(true);
        lblBannerAlerta.setBackground(EstilosUI.colorFondoAlerta(NivelAlerta.INFO));
        lblBannerAlerta.setForeground(EstilosUI.colorTextoAlerta(NivelAlerta.INFO));
        lblBannerAlerta.setFont(EstilosUI.FUENTE_PEQUENA);
        lblBannerAlerta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(EstilosUI.BORDE_SUAVE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        panelInferior.add(panelBotones, BorderLayout.NORTH);
        panelInferior.add(lblBannerAlerta, BorderLayout.SOUTH);

        tarjeta.add(panelSuperior, BorderLayout.NORTH);
        tarjeta.add(panelReloj, BorderLayout.CENTER);
        tarjeta.add(panelInferior, BorderLayout.SOUTH);

        return tarjeta;
    }

    private JPanel crearPanelRutinaYAlertas() {
        JPanel contenedor = new JPanel(new BorderLayout(10, 10));
        contenedor.setBackground(EstilosUI.FONDO_APP);

        JPanel tarjetaAgenda = EstilosUI.crearTarjeta("Agenda de hoy");
        tarjetaAgenda.setLayout(new BorderLayout());
        tarjetaAgenda.setPreferredSize(new Dimension(0, 130));
        modeloAgendaHoy = new DefaultTableModel(new String[]{"Hora", "Actividad", "Tipo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tablaAgendaHoy = new JTable(modeloAgendaHoy);
        EstilosUI.configurarTabla(tablaAgendaHoy);
        tarjetaAgenda.add(new JScrollPane(tablaAgendaHoy), BorderLayout.CENTER);

        // Tabla de rutina diaria (ciclos)
        JPanel tarjetaRutina = EstilosUI.crearTarjeta("Secuencia de Ciclos de la Rutina");
        tarjetaRutina.setLayout(new BorderLayout());

        String[] columnas = {"Ciclo", "Actividad", "Concentración", "Tipo de pausa", "Pausa", "Estado"};
        modeloCiclos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaCiclos = new JTable(modeloCiclos);
        EstilosUI.configurarTabla(tablaCiclos);
        tablaCiclos.getColumnModel().getColumn(0).setPreferredWidth(55);
        tablaCiclos.getColumnModel().getColumn(1).setPreferredWidth(190);
        tablaCiclos.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaCiclos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaCiclos.getColumnModel().getColumn(4).setPreferredWidth(70);
        tablaCiclos.getColumnModel().getColumn(5).setPreferredWidth(105);
        tablaCiclos.getColumnModel().getColumn(5).setCellRenderer(EstilosUI.crearRendererEstado());

        JScrollPane scrollCiclos = new JScrollPane(tablaCiclos);
        scrollCiclos.getViewport().setBackground(Color.WHITE);
        tarjetaRutina.add(scrollCiclos, BorderLayout.CENTER);

        // Historial de alertas
        JPanel tarjetaAlertas = EstilosUI.crearTarjeta("Historial de Alertas y Sugerencias");
        tarjetaAlertas.setLayout(new BorderLayout());
        tarjetaAlertas.setPreferredSize(new Dimension(0, 175));

        modeloAlertas = new DefaultListModel<>();
        JList<String> listaAlertas = new JList<>(modeloAlertas);
        listaAlertas.setFont(EstilosUI.FUENTE_PEQUENA);
        listaAlertas.setBackground(new Color(250, 251, 253));

        JScrollPane scrollAlertas = new JScrollPane(listaAlertas);
        tarjetaAlertas.add(scrollAlertas, BorderLayout.CENTER);

        contenedor.add(tarjetaAgenda, BorderLayout.NORTH);
        contenedor.add(tarjetaRutina, BorderLayout.CENTER);
        contenedor.add(tarjetaAlertas, BorderLayout.SOUTH);

        return contenedor;
    }

    private void actualizarTablaAgendaHoy() {
        modeloAgendaHoy.setRowCount(0);
        List<ActividadProgramada> agenda = gestor.consultarAgenda(DayOfWeek.from(LocalDate.now()));
        for (ActividadProgramada actividad : agenda) {
            modeloAgendaHoy.addRow(new Object[]{
                actividad.getHoraInicio() + " - " + actividad.getHoraFin(),
                actividad.getNombre(),
                actividad.getTipo()
            });
        }
    }

    public void refrescarAgendaHoy() {
        actualizarTablaAgendaHoy();
    }

    private void reportarSaturacion() {
        String seleccion = (String) cmbSaturacionReportada.getSelectedItem();
        NivelAlerta nivel = "Saturación alta".equals(seleccion)
                ? NivelAlerta.CRITICA
                : "Me siento saturado".equals(seleccion) ? NivelAlerta.ADVERTENCIA : NivelAlerta.INFO;
        gestionSaturacion.reportarSaturacion(nivel);
        actualizarPanelReloj();
    }

    public boolean cargarTareaEnRutina(Tarea tarea) {
        if (hayCicloEnCurso()) {
            JOptionPane.showMessageDialog(this,
                    "Hay un ciclo o pausa en curso. Termina el ciclo actual antes de cambiar de rutina.",
                    "Ciclo activo",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        refrescarSelectorTareas();
        tareaEnRutina = tarea;
        actualizandoCombo = true;
        cmbSelectorTarea.setSelectedItem(tarea);
        actualizandoCombo = false;

        txtActividad.setText(tarea.getMateria().getNombre() + " - " + tarea.getDescripcion());
        txtMinutosTotales.setText(String.valueOf(tarea.getDuracionEstimada()));
        planificarDesdeCampos();
        return true;
    }

    public void refrescarSelectorTareas() {
        Object seleccionAnterior = cmbSelectorTarea.getSelectedItem();
        actualizandoCombo = true;
        cmbSelectorTarea.removeAllItems();
        cmbSelectorTarea.addItem(OPCION_PERSONALIZADA);

        for (Materia materia : gestor.getUsuario().getMaterias()) {
            for (Tarea tarea : materia.getTareas()) {
                if (!tarea.estaCompletada()) {
                    cmbSelectorTarea.addItem(tarea);
                }
            }
        }

        if (seleccionAnterior != null) {
            cmbSelectorTarea.setSelectedItem(seleccionAnterior);
        }
        actualizandoCombo = false;
    }

    private void alCambiarSelectorTarea() {
        if (actualizandoCombo) {
            return;
        }
        Object seleccionado = cmbSelectorTarea.getSelectedItem();
        if (seleccionado instanceof Tarea) {
            Tarea tarea = (Tarea) seleccionado;
            tareaEnRutina = tarea;
            txtActividad.setText(tarea.getMateria().getNombre() + " - " + tarea.getDescripcion());
            txtMinutosTotales.setText(String.valueOf(tarea.getDuracionEstimada()));
        } else {
            tareaEnRutina = null;
        }
    }

    private void planificarDesdeCampos() {
        String actividad = txtActividad.getText().trim();
        String minutosTexto = txtMinutosTotales.getText().trim();

        if (!validador.esTextoValido(actividad)) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa el nombre de la actividad o selecciona una tarea.",
                    "Actividad requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validador.esEnteroPositivo(minutosTexto)) {
            JOptionPane.showMessageDialog(this,
                    "Los minutos de estudio deben ser un entero mayor que cero.",
                    "Tiempo inválido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int minutos = Integer.parseInt(minutosTexto);
        try {
            List<CicloConcentracion> ciclos = gestionSaturacion.planificar(actividad, minutos);
            temporizador.stop();
            relojPausado = false;

            Object sel = cmbSelectorTarea.getSelectedItem();
            tareaEnRutina = (sel instanceof Tarea) ? (Tarea) sel : null;

            lblResumenPlan.setText("Plan generado: " + ciclos.size() + " ciclo(s) | "
                    + minutos + " min de estudio | "
                    + gestionSaturacion.getDuracionTotalPlanificadaMin() + " min totales con pausas.");
            actualizarTablaCiclos();
            actualizarPanelReloj();
            actualizarEstadoBotones();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo planificar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void planificarCargaDelDia() {
        int cargaHoy = gestor.consultarCargaPendiente(LocalDate.now());
        if (cargaHoy <= 0) {
            JOptionPane.showMessageDialog(this,
                    "No tienes tareas pendientes con fecha de hoy (" + LocalDate.now() + ").",
                    "Sin carga pendiente hoy",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        actualizandoCombo = true;
        cmbSelectorTarea.setSelectedItem(OPCION_PERSONALIZADA);
        actualizandoCombo = false;
        tareaEnRutina = null;
        txtActividad.setText("Jornada académica (" + LocalDate.now() + ")");
        txtMinutosTotales.setText(String.valueOf(cargaHoy));
        planificarDesdeCampos();
    }

    private void iniciarSiguienteCiclo() {
        try {
            gestionSaturacion.iniciarSiguienteCiclo();
            relojPausado = false;
            temporizador.start();
            actualizarEstadoBotones();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Control de ciclos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void alternarPausaReloj() {
        if (!hayCicloEnCurso()) {
            return;
        }
        if (relojPausado) {
            relojPausado = false;
            temporizador.start();
        } else {
            relojPausado = true;
            temporizador.stop();
        }
        actualizarPanelReloj();
        actualizarEstadoBotones();
    }

    private void avanzarUnMinutoDemo() {
        if (!hayCicloEnCurso()) {
            JOptionPane.showMessageDialog(this,
                    "Inicia primero un ciclo para adelantar el temporizador.",
                    "Sin ciclo activo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        gestionSaturacion.avanzarSegundos(60);
    }

    private void reiniciarJornada() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Deseas reiniciar el contador de minutos concentrados de la jornada actual?",
                "Reiniciar jornada",
                JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            gestionSaturacion.reiniciarJornada();
            actualizarPanelReloj();
            registrarMensajeLocal("Acumulado de jornada reiniciado a 0 min.");
        }
    }

    private void ejecutarTickSegundo() {
        if (!hayCicloEnCurso()) {
            temporizador.stop();
            actualizarEstadoBotones();
            return;
        }
        gestionSaturacion.avanzarSegundos(1);
    }

    private boolean hayCicloEnCurso() {
        CicloConcentracion actual = gestionSaturacion.getCicloActual();
        if (actual == null) {
            return false;
        }
        EstadoCiclo estado = actual.getEstado();
        return estado == EstadoCiclo.EN_CONCENTRACION || estado == EstadoCiclo.EN_PAUSA;
    }

    private void actualizarTablaCiclos() {
        modeloCiclos.setRowCount(0);
        List<CicloConcentracion> ciclos = gestionSaturacion.getCiclos();
        CicloConcentracion actual = gestionSaturacion.getCicloActual();
        int filaActiva = -1;

        for (int i = 0; i < ciclos.size(); i++) {
            CicloConcentracion c = ciclos.get(i);
            modeloCiclos.addRow(new Object[]{
                    "#" + c.getNumero(),
                    c.getActividad(),
                    c.getDuracionConcentracionMin() + " min",
                    c.getTipoPausa().getEtiqueta(),
                    c.getDuracionPausaMin() > 0 ? c.getDuracionPausaMin() + " min" : "-",
                    c.getEstado().getEtiqueta()
            });
            if (c == actual) {
                filaActiva = i;
            }
        }

        if (filaActiva >= 0) {
            tablaCiclos.setRowSelectionInterval(filaActiva, filaActiva);
        }
    }

    private void actualizarPanelReloj() {
        CicloConcentracion actual = gestionSaturacion.getCicloActual();
        int minutosHoy = gestionSaturacion.getMinutosConcentrados();
        NivelAlerta saturacion = gestionSaturacion.getNivelSaturacion();

        String etiquetaSaturacion;
        switch (saturacion) {
            case CRITICA:
                etiquetaSaturacion = "Crítica";
                lblResumenJornada.setForeground(EstilosUI.PELIGRO);
                break;
            case ADVERTENCIA:
                etiquetaSaturacion = "Advertencia";
                lblResumenJornada.setForeground(EstilosUI.ADVERTENCIA);
                break;
            case INFO:
            default:
                etiquetaSaturacion = "Normal";
                lblResumenJornada.setForeground(EstilosUI.TEXTO_PRINCIPAL);
                break;
        }
        lblResumenJornada.setText("Concentrado hoy: " + minutosHoy + " min | Saturación: " + etiquetaSaturacion);

        if (actual == null) {
            lblFaseActual.setText("SIN CICLO ACTIVO");
            lblFaseActual.setBackground(new Color(226, 232, 240));
            lblFaseActual.setForeground(EstilosUI.TEXTO_PRINCIPAL);
            lblReloj.setText("00:00");
            barraProgreso.setValue(0);
            barraProgreso.setString("0%");
            lblDetalleCiclo.setText(gestionSaturacion.getCiclos().isEmpty()
                    ? "Genera una rutina para comenzar."
                    : "Plan listo. Pulsa \"Iniciar siguiente ciclo\".");
            return;
        }

        EstadoCiclo estado = actual.getEstado();
        lblReloj.setText(actual.getTiempoRestanteFormateado());
        int porcentaje = (int) Math.round(actual.getProgreso() * 100);
        barraProgreso.setValue(porcentaje);
        barraProgreso.setString(porcentaje + "%");

        String sufijoPausa = relojPausado ? " (RELOJ PAUSADO)" : "";
        if (estado == EstadoCiclo.EN_CONCENTRACION) {
            lblFaseActual.setText("EN CONCENTRACIÓN" + sufijoPausa);
            lblFaseActual.setBackground(EstilosUI.PRIMARIO);
            lblFaseActual.setForeground(Color.WHITE);
            lblDetalleCiclo.setText("Ciclo " + actual.getNumero() + " de " + gestionSaturacion.getCiclos().size()
                    + " - " + actual.getActividad());
        } else if (estado == EstadoCiclo.EN_PAUSA) {
            lblFaseActual.setText("PAUSA OBLIGATORIA" + sufijoPausa);
            lblFaseActual.setBackground(EstilosUI.ADVERTENCIA);
            lblFaseActual.setForeground(Color.WHITE);
            lblDetalleCiclo.setText("Descanso " + actual.getTipoPausa().getEtiqueta().toLowerCase()
                    + " (" + actual.getDuracionPausaMin() + " min) antes del siguiente ciclo.");
        } else if (gestionSaturacion.isPlanCompletado()) {
            lblFaseActual.setText("PLAN COMPLETADO");
            lblFaseActual.setBackground(EstilosUI.EXITO);
            lblFaseActual.setForeground(Color.WHITE);
            lblDetalleCiclo.setText("Todos los ciclos fueron completados.");
        } else {
            lblFaseActual.setText("LISTO PARA SIGUIENTE CICLO");
            lblFaseActual.setBackground(new Color(220, 252, 231));
            lblFaseActual.setForeground(new Color(22, 101, 52));
            lblDetalleCiclo.setText("Ciclo " + actual.getNumero() + " finalizado. Ya puedes iniciar el siguiente.");
        }
    }

    private void actualizarEstadoBotones() {
        boolean hayPlan = !gestionSaturacion.getCiclos().isEmpty();
        boolean activo = hayCicloEnCurso();
        boolean completado = gestionSaturacion.isPlanCompletado();

        // Se mantiene activo si hay pausa obligatoria para mostrar el mensaje de bloqueo de GestionSaturacion
        btnIniciarCiclo.setEnabled(hayPlan && !completado);
        btnPausarReanudar.setEnabled(activo);
        btnPausarReanudar.setText(relojPausado ? "Reanudar reloj" : "Pausar reloj");
        btnAvanzarDemo.setEnabled(activo);
    }

    private void registrarMensajeLocal(String mensaje) {
        String hora = java.time.LocalTime.now().format(FORMATO_HORA);
        modeloAlertas.add(0, "[" + hora + "] [INFO] " + mensaje);
    }

    // ---------- Implementación de ListenerSaturacion ----------

    @Override
    public void onCambioEstado(CicloConcentracion ciclo, EstadoCiclo anterior, EstadoCiclo nuevo) {
        if (nuevo == EstadoCiclo.EN_CONCENTRACION) {
            if (tareaEnRutina != null && !tareaEnRutina.estaCompletada()) {
                tareaEnRutina.setEstado(Tarea.Estado.EN_CURSO);
                onDatosActualizados.run();
            }
        } else if (nuevo == EstadoCiclo.FINALIZADO) {
            temporizador.stop();
            relojPausado = false;
        }
        actualizarTablaCiclos();
        actualizarPanelReloj();
        actualizarEstadoBotones();
    }

    @Override
    public void onTick(CicloConcentracion ciclo) {
        actualizarPanelReloj();
    }

    @Override
    public void onAlerta(Alerta alerta) {
        String hora = alerta.getMomento().format(FORMATO_HORA);
        modeloAlertas.add(0, "[" + hora + "] " + alerta.toString());

        lblBannerAlerta.setBackground(EstilosUI.colorFondoAlerta(alerta.getNivel()));
        lblBannerAlerta.setForeground(EstilosUI.colorTextoAlerta(alerta.getNivel()));
        lblBannerAlerta.setText("<html><b>[" + alerta.getNivel() + "]</b> " + alerta.getMensaje() + "</html>");

    }

    @Override
    public void onPlanFinalizado() {
        temporizador.stop();
        relojPausado = false;

        if (tareaEnRutina != null && !tareaEnRutina.estaCompletada()) {
            gestor.completarTarea(tareaEnRutina);
            onDatosActualizados.run();
        }

        actualizarTablaCiclos();
        actualizarPanelReloj();
        actualizarEstadoBotones();
        refrescarSelectorTareas();

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                this,
                "¡Rutina finalizada! Todos los ciclos de estudio han sido completados.",
                "Plan completado",
                JOptionPane.INFORMATION_MESSAGE
        ));
    }
}