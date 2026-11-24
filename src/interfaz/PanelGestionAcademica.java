package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Materia;
import modelo.Tarea;
import negocio.GestorCargaAcademica;
import negocio.ValidadorDatos;

public class PanelGestionAcademica extends JPanel {

    private final GestorCargaAcademica gestor;
    private final ValidadorDatos validador;
    private final Consumer<Tarea> onEnviarARutina;
    private final Runnable onDatosActualizados;

    private final List<Tarea> tareasMostradas = new ArrayList<>();

    private JTextField txtNombreMateria;
    private JComboBox<Materia> cmbMateriaRegistro;
    private JTextField txtDescripcion;
    private JTextField txtFechaEntrega;
    private JTextField txtDuracion;

    private JComboBox<String> cmbTipoConsulta;
    private JTextField txtFechaConsulta;
    private JComboBox<Materia> cmbMateriaConsulta;
    private JComboBox<String> cmbFiltroEstado;
    private JLabel lblCargaPendiente;

    private DefaultTableModel modeloTabla;
    private JTable tablaTareas;

    public PanelGestionAcademica(GestorCargaAcademica gestor,
                                 Consumer<Tarea> onEnviarARutina,
                                 Runnable onDatosActualizados) {
        this.gestor = gestor;
        this.validador = new ValidadorDatos();
        this.onEnviarARutina = onEnviarARutina;
        this.onDatosActualizados = onDatosActualizados;

        setLayout(new BorderLayout(12, 12));
        setBackground(EstilosUI.FONDO_APP);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearPanelFormularios(), BorderLayout.WEST);
        add(crearPanelConsultaYTabla(), BorderLayout.CENTER);

        actualizarControlesFiltro();
        refrescarVista();
    }

    private JPanel crearPanelFormularios() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(EstilosUI.FONDO_APP);
        contenedor.setPreferredSize(new Dimension(330, 0));

        // Tarjeta 1: Registro de Materia
        JPanel tarjetaMateria = EstilosUI.crearTarjeta("1. Registrar Materia");
        tarjetaMateria.setLayout(new GridBagLayout());
        tarjetaMateria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        GridBagConstraints gbc = crearGbcBase();

        gbc.gridx = 0;
        gbc.gridy = 0;
        tarjetaMateria.add(new JLabel("Nombre de la materia:"), gbc);

        txtNombreMateria = new JTextField();
        txtNombreMateria.setFont(EstilosUI.FUENTE_NORMAL);
        gbc.gridy = 1;
        tarjetaMateria.add(txtNombreMateria, gbc);

        JButton btnRegistrarMateria = EstilosUI.crearBoton("Guardar materia", EstilosUI.PRIMARIO);
        btnRegistrarMateria.addActionListener(e -> registrarMateria());
        gbc.gridy = 2;
        tarjetaMateria.add(btnRegistrarMateria, gbc);

        JButton btnEliminarMateria = EstilosUI.crearBoton("Eliminar materia seleccionada", EstilosUI.PELIGRO);
        btnEliminarMateria.addActionListener(e -> eliminarMateriaSeleccionada());
        gbc.gridy = 3;
        tarjetaMateria.add(btnEliminarMateria, gbc);

        // Tarjeta 2: Registro de Tarea
        JPanel tarjetaTarea = EstilosUI.crearTarjeta("2. Registrar Tarea");
        tarjetaTarea.setLayout(new GridBagLayout());
        GridBagConstraints gbcT = crearGbcBase();

        gbcT.gridy = 0;
        tarjetaTarea.add(new JLabel("Materia:"), gbcT);
        cmbMateriaRegistro = new JComboBox<>();
        cmbMateriaRegistro.setFont(EstilosUI.FUENTE_NORMAL);
        gbcT.gridy = 1;
        tarjetaTarea.add(cmbMateriaRegistro, gbcT);

        gbcT.gridy = 2;
        tarjetaTarea.add(new JLabel("Descripción de la actividad:"), gbcT);
        txtDescripcion = new JTextField();
        txtDescripcion.setFont(EstilosUI.FUENTE_NORMAL);
        gbcT.gridy = 3;
        tarjetaTarea.add(txtDescripcion, gbcT);

        gbcT.gridy = 4;
        tarjetaTarea.add(new JLabel("Fecha de entrega (AAAA-MM-DD):"), gbcT);
        txtFechaEntrega = new JTextField(LocalDate.now().toString());
        txtFechaEntrega.setFont(EstilosUI.FUENTE_NORMAL);
        txtFechaEntrega.setToolTipText("Ejemplo: " + LocalDate.now());
        gbcT.gridy = 5;
        tarjetaTarea.add(txtFechaEntrega, gbcT);

        gbcT.gridy = 6;
        tarjetaTarea.add(new JLabel("Duración estimada (minutos):"), gbcT);
        txtDuracion = new JTextField("50");
        txtDuracion.setFont(EstilosUI.FUENTE_NORMAL);
        gbcT.gridy = 7;
        tarjetaTarea.add(txtDuracion, gbcT);

        JButton btnRegistrarTarea = EstilosUI.crearBoton("Agregar tarea", EstilosUI.EXITO);
        btnRegistrarTarea.addActionListener(e -> registrarTarea());
        gbcT.gridy = 8;
        gbcT.insets = new Insets(10, 4, 4, 4);
        tarjetaTarea.add(btnRegistrarTarea, gbcT);

        JLabel notaValidacion = new JLabel("<html><i style='color:#64748b'>La fecha debe ser hoy o posterior y la duración mayor a 0 min.</i></html>");
        notaValidacion.setFont(EstilosUI.FUENTE_PEQUENA);
        gbcT.gridy = 9;
        gbcT.insets = new Insets(4, 4, 4, 4);
        tarjetaTarea.add(notaValidacion, gbcT);

        contenedor.add(tarjetaMateria);
        contenedor.add(Box.createVerticalStrut(12));
        contenedor.add(tarjetaTarea);
        contenedor.add(Box.createVerticalGlue());

        return contenedor;
    }

    private JPanel crearPanelConsultaYTabla() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(EstilosUI.FONDO_APP);

        // Panel superior: Filtros de consulta
        JPanel tarjetaConsulta = EstilosUI.crearTarjeta("Consulta de Actividades y Carga Académica");
        tarjetaConsulta.setLayout(new BorderLayout(8, 8));

        JPanel filaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaFiltros.setBackground(EstilosUI.FONDO_TARJETA);

        cmbTipoConsulta = new JComboBox<>(new String[]{"Todas las tareas", "Por fecha", "Por materia"});
        cmbTipoConsulta.setFont(EstilosUI.FUENTE_NORMAL);
        cmbTipoConsulta.addActionListener(e -> {
            actualizarControlesFiltro();
            aplicarConsulta();
        });

        txtFechaConsulta = new JTextField(LocalDate.now().toString(), 9);
        txtFechaConsulta.setFont(EstilosUI.FUENTE_NORMAL);

        cmbMateriaConsulta = new JComboBox<>();
        cmbMateriaConsulta.setFont(EstilosUI.FUENTE_NORMAL);
        cmbMateriaConsulta.setPreferredSize(new Dimension(150, 26));
        cmbMateriaConsulta.addActionListener(e -> {
            if ("Por materia".equals(cmbTipoConsulta.getSelectedItem())) {
                aplicarConsulta();
            }
        });

        cmbFiltroEstado = new JComboBox<>(new String[]{"Todos los estados", "Pendiente", "En curso", "Completada"});
        cmbFiltroEstado.setFont(EstilosUI.FUENTE_NORMAL);
        cmbFiltroEstado.addActionListener(e -> aplicarConsulta());

        JButton btnConsultar = EstilosUI.crearBoton("Consultar", EstilosUI.PRIMARIO);
        btnConsultar.addActionListener(e -> aplicarConsulta());

        JButton btnHoy = EstilosUI.crearBoton("Hoy", EstilosUI.SECUNDARIO);
        btnHoy.addActionListener(e -> {
            txtFechaConsulta.setText(LocalDate.now().toString());
            cmbTipoConsulta.setSelectedItem("Por fecha");
            actualizarControlesFiltro();
            aplicarConsulta();
        });

        filaFiltros.add(new JLabel("Ver:"));
        filaFiltros.add(cmbTipoConsulta);
        filaFiltros.add(new JLabel("Fecha:"));
        filaFiltros.add(txtFechaConsulta);
        filaFiltros.add(new JLabel("Materia:"));
        filaFiltros.add(cmbMateriaConsulta);
        filaFiltros.add(new JLabel("Estado:"));
        filaFiltros.add(cmbFiltroEstado);
        filaFiltros.add(btnConsultar);
        filaFiltros.add(btnHoy);

        lblCargaPendiente = new JLabel("Carga pendiente para hoy: 0 min");
        lblCargaPendiente.setFont(EstilosUI.FUENTE_NEGRITA);
        lblCargaPendiente.setForeground(EstilosUI.PRIMARIO);
        lblCargaPendiente.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));

        tarjetaConsulta.add(filaFiltros, BorderLayout.CENTER);
        tarjetaConsulta.add(lblCargaPendiente, BorderLayout.SOUTH);

        // Panel central: Tabla de tareas
        String[] columnas = {"Materia", "Descripción", "Fecha entrega", "Duración (min)", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaTareas = new JTable(modeloTabla);
        EstilosUI.configurarTabla(tablaTareas);
        tablaTareas.getColumnModel().getColumn(0).setPreferredWidth(130);
        tablaTareas.getColumnModel().getColumn(1).setPreferredWidth(250);
        tablaTareas.getColumnModel().getColumn(2).setPreferredWidth(105);
        tablaTareas.getColumnModel().getColumn(3).setPreferredWidth(105);
        tablaTareas.getColumnModel().getColumn(4).setPreferredWidth(110);
        tablaTareas.getColumnModel().getColumn(4).setCellRenderer(EstilosUI.crearRendererEstado());

        JScrollPane scrollTabla = new JScrollPane(tablaTareas);
        scrollTabla.getViewport().setBackground(Color.WHITE);
        scrollTabla.setBorder(BorderFactory.createLineBorder(EstilosUI.BORDE_SUAVE));

        // Panel inferior: Acciones sobre tarea seleccionada
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        panelAcciones.setBackground(EstilosUI.FONDO_APP);

        JButton btnEstudiar = EstilosUI.crearBoton("Planificar en Rutina Diaria", EstilosUI.MORADO);
        btnEstudiar.addActionListener(e -> enviarTareaARutina());

        JButton btnModificar = EstilosUI.crearBoton("Modificar duración", EstilosUI.ADVERTENCIA);
        btnModificar.addActionListener(e -> modificarDuracionSeleccionada());

        JButton btnEditar = EstilosUI.crearBoton("Editar tarea", EstilosUI.SECUNDARIO);
        btnEditar.addActionListener(e -> editarTareaSeleccionada());

        JButton btnCompletar = EstilosUI.crearBoton("Marcar como completada", EstilosUI.EXITO);
        btnCompletar.addActionListener(e -> completarTareaSeleccionada());

        JButton btnEliminar = EstilosUI.crearBoton("Eliminar tarea", EstilosUI.PELIGRO);
        btnEliminar.addActionListener(e -> eliminarTareaSeleccionada());

        panelAcciones.add(btnEstudiar);
        panelAcciones.add(btnModificar);
        panelAcciones.add(btnEditar);
        panelAcciones.add(btnCompletar);
        panelAcciones.add(btnEliminar);

        panel.add(tarjetaConsulta, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        return panel;
    }

    private GridBagConstraints crearGbcBase() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 4, 4, 4);
        return gbc;
    }

    private void actualizarControlesFiltro() {
        String modo = (String) cmbTipoConsulta.getSelectedItem();
        txtFechaConsulta.setEnabled("Por fecha".equals(modo) || "Por materia".equals(modo));
        cmbMateriaConsulta.setEnabled("Por materia".equals(modo));
    }

    private void registrarMateria() {
        try {
            Materia nueva = gestor.registrarMateria(txtNombreMateria.getText());
            txtNombreMateria.setText("");
            refrescarCombosMaterias(nueva);
            aplicarConsulta();
            onDatosActualizados.run();
            JOptionPane.showMessageDialog(this,
                    "Materia \"" + nueva.getNombre() + "\" registrada correctamente.",
                    "Registro exitoso",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo registrar la materia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void registrarTarea() {
        Materia materia = (Materia) cmbMateriaRegistro.getSelectedItem();
        if (materia == null) {
            JOptionPane.showMessageDialog(this,
                    "Primero debes registrar al menos una materia.",
                    "Materia requerida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fechaEntrega;
        try {
            fechaEntrega = LocalDate.parse(txtFechaEntrega.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Fecha inválida. Usa el formato AAAA-MM-DD (ejemplo: " + LocalDate.now() + ").",
                    "Error de formato",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String duracionTexto = txtDuracion.getText().trim();
        if (!validador.esEnteroPositivo(duracionTexto)) {
            JOptionPane.showMessageDialog(this,
                    "La duración estimada debe ser un número entero mayor que cero.",
                    "Duración inválida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int duracion = Integer.parseInt(duracionTexto);
        try {
            gestor.registrarTarea(materia.getNombre(), txtDescripcion.getText(), fechaEntrega, duracion);
            txtDescripcion.setText("");
            txtFechaEntrega.setText(LocalDate.now().toString());
            txtDuracion.setText("50");
            aplicarConsulta();
            onDatosActualizados.run();
            JOptionPane.showMessageDialog(this,
                    "Tarea registrada correctamente en " + materia.getNombre() + ".",
                    "Tarea registrada",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void enviarTareaARutina() {
        Tarea tarea = obtenerTareaSeleccionada();
        if (tarea == null) {
            return;
        }
        if (tarea.estaCompletada()) {
            JOptionPane.showMessageDialog(this,
                    "Esta tarea ya está completada. Selecciona una tarea pendiente o en curso.",
                    "Tarea completada",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        onEnviarARutina.accept(tarea);
    }

    private void modificarDuracionSeleccionada() {
        Tarea tarea = obtenerTareaSeleccionada();
        if (tarea == null) {
            return;
        }
        String entrada = JOptionPane.showInputDialog(
                this,
                "Nueva duración estimada en minutos para:\n" + tarea.getDescripcion(),
                String.valueOf(tarea.getDuracionEstimada())
        );
        if (entrada == null) {
            return;
        }
        if (!validador.esEnteroPositivo(entrada)) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa un número entero mayor que cero.",
                    "Valor inválido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            gestor.modificarDuracion(tarea, Integer.parseInt(entrada.trim()));
            aplicarConsulta();
            onDatosActualizados.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error al modificar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void completarTareaSeleccionada() {
        Tarea tarea = obtenerTareaSeleccionada();
        if (tarea == null) {
            return;
        }
        if (tarea.estaCompletada()) {
            JOptionPane.showMessageDialog(this,
                    "La tarea seleccionada ya figura como completada.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        gestor.completarTarea(tarea);
        aplicarConsulta();
        onDatosActualizados.run();
    }

    private void editarTareaSeleccionada() {
        Tarea tarea = obtenerTareaSeleccionada();
        if (tarea == null) {
            return;
        }
        String descripcion = JOptionPane.showInputDialog(this,
                "Descripción de la tarea:", tarea.getDescripcion());
        if (descripcion == null) {
            return;
        }
        String fechaTexto = JOptionPane.showInputDialog(this,
                "Fecha de entrega (AAAA-MM-DD):", tarea.getFechaEntrega().toString());
        if (fechaTexto == null) {
            return;
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(fechaTexto.trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa una fecha válida en formato AAAA-MM-DD.",
                    "Fecha inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            gestor.modificarTarea(tarea, descripcion, fecha);
            aplicarConsulta();
            onDatosActualizados.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo editar la tarea", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarTareaSeleccionada() {
        Tarea tarea = obtenerTareaSeleccionada();
        if (tarea == null) {
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la tarea \"" + tarea.getDescripcion() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            gestor.eliminarTarea(tarea);
            aplicarConsulta();
            onDatosActualizados.run();
        }
    }

    private void eliminarMateriaSeleccionada() {
        Materia materia = (Materia) cmbMateriaRegistro.getSelectedItem();
        if (materia == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una materia.",
                    "Selección requerida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la materia \"" + materia.getNombre()
                        + "\"? Debe estar vacía de tareas.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            gestor.eliminarMateria(materia);
            refrescarVista();
            onDatosActualizados.run();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo eliminar", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Tarea obtenerTareaSeleccionada() {
        int fila = tablaTareas.getSelectedRow();
        if (fila < 0 || fila >= tareasMostradas.size()) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona primero una tarea de la tabla.",
                    "Selección requerida",
                    JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return tareasMostradas.get(fila);
    }

    public void refrescarVista() {
        Materia seleccionRegistro = (Materia) cmbMateriaRegistro.getSelectedItem();
        refrescarCombosMaterias(seleccionRegistro);
        aplicarConsulta();
    }

    private void refrescarCombosMaterias(Materia seleccionada) {
        Materia seleccionConsulta = (Materia) cmbMateriaConsulta.getSelectedItem();
        cmbMateriaRegistro.removeAllItems();
        cmbMateriaConsulta.removeAllItems();

        for (Materia materia : gestor.getUsuario().getMaterias()) {
            cmbMateriaRegistro.addItem(materia);
            cmbMateriaConsulta.addItem(materia);
        }

        if (seleccionada != null) {
            cmbMateriaRegistro.setSelectedItem(seleccionada);
        }
        if (seleccionConsulta != null) {
            cmbMateriaConsulta.setSelectedItem(seleccionConsulta);
        }
    }

    private void aplicarConsulta() {
        String modo = (String) cmbTipoConsulta.getSelectedItem();
        String filtroEstado = (String) cmbFiltroEstado.getSelectedItem();

        LocalDate fechaConsulta = LocalDate.now();
        if ("Por fecha".equals(modo) || "Por materia".equals(modo)) {
            try {
                fechaConsulta = LocalDate.parse(txtFechaConsulta.getText().trim());
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Fecha de consulta inválida. Usa AAAA-MM-DD (ejemplo: " + LocalDate.now() + ").",
                        "Error de fecha",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        List<Tarea> base = new ArrayList<>();
        if ("Por fecha".equals(modo)) {
            base.addAll(gestor.consultarTareasPorFecha(fechaConsulta));
            int carga = gestor.consultarCargaPendiente(fechaConsulta);
            lblCargaPendiente.setText("Carga pendiente para " + fechaConsulta + ": " + carga + " min");
        } else if ("Por materia".equals(modo)) {
            Materia materia = (Materia) cmbMateriaConsulta.getSelectedItem();
            if (materia != null) {
                base.addAll(gestor.consultarTareasPorMateria(materia));
                int cargaMateria = gestor.consultarCargaPendientePorMateria(fechaConsulta, materia);
                lblCargaPendiente.setText("Carga pendiente en " + materia.getNombre()
                        + " (" + fechaConsulta + "): " + cargaMateria + " min");
            } else {
                lblCargaPendiente.setText("No hay materias registradas todavía.");
            }
        } else {
            for (Materia materia : gestor.getUsuario().getMaterias()) {
                base.addAll(gestor.consultarTareasPorMateria(materia));
            }
            int cargaHoy = gestor.consultarCargaPendiente(LocalDate.now());
            lblCargaPendiente.setText("Mostrando todas las tareas | Carga pendiente para hoy ("
                    + LocalDate.now() + "): " + cargaHoy + " min");
        }

        tareasMostradas.clear();
        modeloTabla.setRowCount(0);

        for (Tarea tarea : base) {
            String etiquetaEstado = EstilosUI.formatearEstadoTarea(tarea.getEstado());
            if (!"Todos los estados".equals(filtroEstado) && !etiquetaEstado.equalsIgnoreCase(filtroEstado)) {
                continue;
            }
            tareasMostradas.add(tarea);
            modeloTabla.addRow(new Object[]{
                    tarea.getMateria().getNombre(),
                    tarea.getDescripcion(),
                    tarea.getFechaEntrega().toString(),
                    tarea.getDuracionEstimada(),
                    etiquetaEstado
            });
        }
    }
}