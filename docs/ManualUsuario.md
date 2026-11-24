# Manual de usuario de MindLoad

## Inicio y almacenamiento

Ejecuta `interfaz.VentanaPrincipal` después de compilar el proyecto. El perfil,
las materias, las tareas y la agenda se guardan automáticamente en
`~/.mindload/mindload.dat` y al cerrar; si el guardado falla, la aplicación
muestra un aviso y permite reintentar el cierre.

## Gestión académica

En **Gestión Académica y Consultas** puedes crear materias y tareas, consultar
por fecha o materia, cambiar la duración, marcar una tarea como completada y
eliminarla. Para eliminar una materia, elimina primero sus tareas y selecciónala
en el formulario de materias.

## Horarios y conflictos de horario

En **Horarios**, selecciona si registrarás una clase, un entrenamiento o una
pausa activa; indica el día, nombre y horas de inicio y fin en formato `HH:mm`.
La aplicación rechaza intervalos inválidos y horarios que se traslapen en el
mismo día; una actividad que comienza justo al terminar otra sí es válida.
Selecciona una fila y usa **Eliminar actividad** para retirarla.

La sección **Agenda de hoy** de Rutina Diaria presenta las clases y los
entrenamientos del día con sus horas. La agenda usa repetición semanal.

## Rutina y saturación

La rutina fragmenta una actividad o tarea en ciclos de concentración y pausas.
También permite planificar la carga académica con entrega para hoy. En la misma
pantalla, informa tu nivel de saturación usando el control **¿Cómo te sientes?**.
Un reporte alto durante un ciclo activa una pausa obligatoria y conserva el
tiempo restante como pendiente. Las advertencias de pausa muestran un diálogo
para que el estudiante confirme que recibió la indicación.
