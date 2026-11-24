package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import negocio.concentracion.GestionSaturacion;
import modelo.Materia;
import modelo.Tarea;
import modelo.Usuario;
import negocio.GestorCargaAcademica;
import negocio.GestorHorarios;
import negocio.UsuarioRepositorio;

public class VentanaPrincipal extends JFrame {

    private final Usuario usuario;
    private final GestorCargaAcademica gestor;
    private final GestionSaturacion gestionSaturacion;
    private final GestorHorarios gestorHorarios;
    private final UsuarioRepositorio repositorio;

    private JLabel lblEstudiante;
    private JLabel lblPendientes;
    private JLabel lblEnCurso;
    private JLabel lblCompletadas;

    private JTabbedPane pestanas;
    private PanelGestionAcademica panelAcademico;
    private PanelRutinaDiaria panelRutina;
    private PanelHorarios panelHorarios;

    public VentanaPrincipal() {
        super("MindLoad - Gestión de Carga Académica y Rutina Diaria");

        this.repositorio = new UsuarioRepositorio(Paths.get(
                System.getProperty("user.home"), ".mindload", "mindload.dat"));
        this.usuario = repositorio.cargar();
        this.gestor = new GestorCargaAcademica(usuario);
        this.gestionSaturacion = new GestionSaturacion();
        this.gestorHorarios = new GestorHorarios(usuario);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                if (guardarDatos()) {
                    dispose();
                }
            }
        });
        setSize(1120, 740);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(EstilosUI.FONDO_APP);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearContenidoPrincipal(), BorderLayout.CENTER);

        actualizarResumenGlobal();
    }

    private JPanel crearEncabezado() {
        JPanel barra = new JPanel(new BorderLayout(12, 8));
        barra.setBackground(EstilosUI.ENCABEZADO_OSCURO);
        barra.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Izquierda: título y estudiante
        JPanel panelTitulo = new JPanel(new BorderLayout(4, 2));
        panelTitulo.setOpaque(false);

        JLabel lblTitulo = new JLabel("MindLoad");
        lblTitulo.setFont(EstilosUI.FUENTE_TITULO);
        lblTitulo.setForeground(Color.WHITE);

        lblEstudiante = new JLabel("Estudiante: " + usuario.getNombre() + "  |  Hoy: " + LocalDate.now());
        lblEstudiante.setFont(EstilosUI.FUENTE_PEQUENA);
        lblEstudiante.setForeground(new Color(203, 213, 225));

        panelTitulo.add(lblTitulo, BorderLayout.NORTH);
        panelTitulo.add(lblEstudiante, BorderLayout.SOUTH);

        // Derecha: contadores de estado y botones rápidos
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        panelDerecho.setOpaque(false);

        lblPendientes = EstilosUI.crearInsignia("Pendientes: 0",
                new Color(241, 245, 249), new Color(51, 65, 85));
        lblEnCurso = EstilosUI.crearInsignia("En curso: 0",
                new Color(219, 234, 254), new Color(30, 64, 175));
        lblCompletadas = EstilosUI.crearInsignia("Completadas: 0",
                new Color(220, 252, 231), new Color(22, 101, 52));

        JButton btnCambiarUsuario = EstilosUI.crearBoton("Estudiante", EstilosUI.SECUNDARIO);
        btnCambiarUsuario.addActionListener(e -> cambiarNombreEstudiante());

        JButton btnDatosEjemplo = EstilosUI.crearBoton("Cargar ejemplo", EstilosUI.PRIMARIO);
        btnDatosEjemplo.setToolTipText("Agrega materias y tareas de prueba para evaluar la aplicación");
        btnDatosEjemplo.addActionListener(e -> cargarDatosEjemplo());

        panelDerecho.add(lblPendientes);
        panelDerecho.add(lblEnCurso);
        panelDerecho.add(lblCompletadas);
        panelDerecho.add(btnCambiarUsuario);
        panelDerecho.add(btnDatosEjemplo);

        barra.add(panelTitulo, BorderLayout.WEST);
        barra.add(panelDerecho, BorderLayout.EAST);

        return barra;
    }

    private JTabbedPane crearContenidoPrincipal() {
        pestanas = new JTabbedPane();
        pestanas.setFont(EstilosUI.FUENTE_SUBTITULO);

        panelRutina = new PanelRutinaDiaria(
                gestor,
                gestorHorarios,
                gestionSaturacion,
                this::sincronizarVistas
        );

        panelAcademico = new PanelGestionAcademica(
                gestor,
                this::abrirTareaEnRutina,
                this::sincronizarVistas
        );
        panelHorarios = new PanelHorarios(gestorHorarios, this::sincronizarVistas);

        pestanas.addTab("Gestión Académica y Consultas", panelAcademico);
        pestanas.addTab("Horarios", panelHorarios);
        pestanas.addTab("Rutina Diaria y Ciclos (RF-05 / RF-06)", panelRutina);

        return pestanas;
    }

    private void abrirTareaEnRutina(Tarea tarea) {
        if (panelRutina.cargarTareaEnRutina(tarea)) {
            pestanas.setSelectedComponent(panelRutina);
        }
    }

    private void sincronizarVistas() {
        panelAcademico.refrescarVista();
        panelRutina.refrescarSelectorTareas();
        panelRutina.refrescarAgendaHoy();
        panelHorarios.refrescarVista();
        actualizarResumenGlobal();
        guardarDatos();
    }

    private boolean guardarDatos() {
        try {
            repositorio.guardar(usuario);
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Los cambios siguen disponibles en esta sesión, pero no se pudieron guardar:\n"
                            + ex.getMessage(),
                    "Error de persistencia", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void actualizarResumenGlobal() {
        int pendientes = 0;
        int enCurso = 0;
        int completadas = 0;

        for (Materia materia : usuario.getMaterias()) {
            for (Tarea tarea : materia.getTareas()) {
                if (tarea.getEstado() == Tarea.Estado.COMPLETADA) {
                    completadas++;
                } else if (tarea.getEstado() == Tarea.Estado.EN_CURSO) {
                    enCurso++;
                } else {
                    pendientes++;
                }
            }
        }

        lblPendientes.setText("Pendientes: " + pendientes);
        lblEnCurso.setText("En curso: " + enCurso);
        lblCompletadas.setText("Completadas: " + completadas);
    }

    private void cambiarNombreEstudiante() {
        String nuevoNombre = JOptionPane.showInputDialog(
                this,
                "Ingresa el nombre del estudiante:",
                usuario.getNombre()
        );
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
            usuario.setNombre(nuevoNombre.trim());
            lblEstudiante.setText("Estudiante: " + usuario.getNombre() + "  |  Hoy: " + LocalDate.now());
            guardarDatos();
        }
    }

    private void cargarDatosEjemplo() {
        if (!usuario.getMaterias().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ya existen materias registradas. Los datos de ejemplo solo se cargan cuando la lista está vacía.",
                    "Datos existentes",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            LocalDate hoy = LocalDate.now();
            gestor.registrarMateria("Programación II");
            gestor.registrarMateria("Cálculo II");
            gestor.registrarMateria("Física");

            gestor.registrarTarea("Programación II", "Conectar interfaz gráfica de MindLoad", hoy, 55);
            gestor.registrarTarea("Cálculo II", "Resolver ejercicios de integrales", hoy, 30);
            gestor.registrarTarea("Física", "Informe de laboratorio de cinemática", hoy.plusDays(1), 45);

            sincronizarVistas();
            JOptionPane.showMessageDialog(this,
                    "Datos de ejemplo cargados. Selecciona una tarea y pulsa \"Planificar en Rutina Diaria\".",
                    "Ejemplo listo",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            try {
                VentanaPrincipal ventana = new VentanaPrincipal();
                ventana.setVisible(true);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(),
                        "No se pudieron cargar los datos", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}