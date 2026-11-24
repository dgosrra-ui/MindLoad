# MindLoad

Proyecto Java organizado por paquetes:

- `src/modelo/`: entidades del dominio.
- `src/negocio/`: gestión académica, `GestorHorarios`, validaciones y persistencia.
- `src/negocio/concentracion/`: lógica de ciclos Pomodoro y alertas de saturación.
- `src/interfaz/`: interfaz gráfica Swing de gestión académica y rutina diaria.
- `pruebas/negocio/`: pruebas del gestor académico, horarios y persistencia.
- `pruebas/negocio/concentracion/`: pruebas del módulo de concentración.

La aplicación persiste el usuario, sus materias, tareas y agenda semanal en
`~/.mindload/usuario.ser`. Las clases y los entrenamientos comparten la
superclase `modelo.ActividadProgramada`; los horarios que se traslapan en el
mismo día se rechazan.

## Compilar

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Path src,pruebas -Filter *.java | ForEach-Object { $_.FullName })
```

## Ejecutar pruebas

```powershell
java '-Dfile.encoding=UTF-8' -cp out pruebas.negocio.PruebasValidadorDatos
java '-Dfile.encoding=UTF-8' -cp out pruebas.negocio.PruebaGestorCargaAcademica
java '-Dfile.encoding=UTF-8' -cp out pruebas.negocio.PruebasPersistencia
java '-Dfile.encoding=UTF-8' -cp out pruebas.negocio.concentracion.PruebasGestionSaturacion
```

## Ejecutar la interfaz gráfica

```powershell
java '-Dfile.encoding=UTF-8' -cp out interfaz.VentanaPrincipal
```

Consulta el [manual de usuario](docs/ManualUsuario.md) y el
[diagrama de clases](docs/diagrama-clases.puml).
