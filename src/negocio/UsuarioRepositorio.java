package negocio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import modelo.Usuario;

public class UsuarioRepositorio {
    private final Path archivo;

    public UsuarioRepositorio(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("La ruta de persistencia es obligatoria.");
        }
        this.archivo = archivo;
    }

    public Usuario cargar() {
        if (!Files.exists(archivo)) {
            return new Usuario("Estudiante");
        }
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(archivo))) {
            Object datos = entrada.readObject();
            if (!(datos instanceof Usuario)) {
                throw new IOException("El archivo no contiene datos de un usuario MindLoad.");
            }
            return (Usuario) datos;
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalStateException("No se pudieron cargar los datos desde " + archivo + ".", ex);
        }
    }

    public void guardar(Usuario usuario) throws IOException {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        Path directorio = archivo.toAbsolutePath().getParent();
        Files.createDirectories(directorio);
        Path temporal = Files.createTempFile(directorio, "mindload-", ".tmp");
        try {
            try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(temporal))) {
                salida.writeObject(usuario);
            }
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
