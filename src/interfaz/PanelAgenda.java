package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.ActividadProgramada;
import negocio.GestorCargaAcademica;

public class PanelAgenda extends JPanel {
    private static final String[] DIAS = {
        "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    private final GestorCargaAcademica gestor;
    private final Runnable onDatosActualizados;
    private final List<ActividadProgramada> actividadesMostradas = new ArrayList<>();

    private JComboBox<String> cmbTipo;
    private JComboBox<String> cmbDia;
    private JTextField txtNombre;
    private JTextField txtInicio;
    private JTextField txtFin;
    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelAgenda(GestorCargaAcademica gestor, Runnable onDatosActualizados) {
        this.gestor = gestor;
        this.onDatosActualizados = onDatosActualizados;
        setLayout(new BorderLayout(10, 10));
        setBackground(EstilosUI.FONDO_APP);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(crearFormulario(), BorderLayout.NORTH);
        modelo = new DefaultTableModel(
                new String[]{"Día", "Inicio", "Fin", "Actividad", "Tipo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        EstilosUI.configurarTabla(tabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setBackground(EstilosUI.FONDO_APP);
        javax.swing.JButton btnEliminar = EstilosUI.crearBoton("Eliminar actividad", EstilosUI.PELIGRO);
        btnEliminar.addActionListener(e -> eliminarSeleccionada());
        acciones.add(btnEliminar);
        add(acciones, BorderLayout.SOUTH);
        refrescarVista();
    }

    private JPanel crearFormulario() {
        JPanel formulario = EstilosUI.crearTarjeta("Registrar clase o entrenamiento");
        formulario.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        cmbTipo = new JComboBox<>(new String[]{"Clase", "Entrenamiento"});
        cmbDia = new JComboBox<>(DIAS);
        txtNombre = new JTextField(16);
        txtInicio = new JTextField("09:00", 5);
        txtFin = new JTextField("10:00", 5);
        javax.swing.JButton btnGuardar = EstilosUI.crearBoton("Agregar a la agenda", EstilosUI.PRIMARIO);
        btnGuardar.addActionListener(e -> registrarActividad());

        formulario.add(new JLabel("Tipo:"));
        formulario.add(cmbTipo);
        formulario.add(new JLabel("Día:"));
        formulario.add(cmbDia);
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("Inicio (HH:mm):"));
        formulario.add(txtInicio);
        formulario.add(new JLabel("Fin (HH:mm):"));
        formulario.add(txtFin);
        formulario.add(btnGuardar);
        return formulario;
    }

    private void registrarActividad() {
        try {
            DayOfWeek dia = DayOfWeek.of(cmbDia.getSelectedIndex() + 1);
            LocalTime inicio = LocalTime.parse(txtInicio.getText().trim());
            LocalTime fin = LocalTime.parse(txtFin.getText().trim());
            if ("Clase".equals(cmbTipo.getSelectedItem())) {
                gestor.registrarClase(txtNombre.getText(), dia, inicio, fin);
            } else {
                gestor.registrarEntrenamiento(txtNombre.getText(), dia, inicio, fin);
            }
            txtNombre.setText("");
            refrescarVista();
            onDatosActualizados.run();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa las horas en formato HH:mm.",
                    "Hora inválida", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo agregar la actividad", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= actividadesMostradas.size()) {
            JOptionPane.showMessageDialog(this, "Selecciona una actividad de la agenda.",
                    "Selección requerida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar esta actividad de la agenda?", "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            gestor.eliminarActividadProgramada(actividadesMostradas.get(fila));
            refrescarVista();
            onDatosActualizados.run();
        }
    }

    public void refrescarVista() {
        actividadesMostradas.clear();
        actividadesMostradas.addAll(gestor.getUsuario().getActividadesProgramadas());
        modelo.setRowCount(0);
        for (ActividadProgramada actividad : actividadesMostradas) {
            modelo.addRow(new Object[]{
                DIAS[actividad.getDia().getValue() - 1],
                actividad.getHoraInicio(),
                actividad.getHoraFin(),
                actividad.getNombre(),
                actividad.getTipo()
            });
        }
    }
}
