# MindLoad

Aplicación de escritorio para organizar la carga académica, planificar horarios
y trabajar con ciclos de concentración y pausas activas.

## Requisitos

- JDK 8 o superior.
- Windows (PowerShell o `cmd`) o un entorno POSIX con `sh`.
- No requiere dependencias externas.

## Estructura

- `src/modelo/`: entidades y actividades programadas.
- `src/negocio/`: gestión académica, horarios, validación y persistencia.
- `src/negocio/concentracion/`: ciclos Pomodoro y alertas de saturación.
- `src/interfaz/`: interfaz gráfica Swing.
- `pruebas/`: cuatro suites ejecutables sin bibliotecas de prueba externas.
- `docs/`: manual de usuario y diagrama PlantUML.

La aplicación guarda el usuario, sus materias, tareas y agenda semanal en
`~/.mindload/mindload.dat` al cerrar y lo carga al iniciar.

## Compilar

Desde PowerShell, en la raíz del proyecto:

```powershell
New-Item -ItemType Directory -Force out
$fuentes = Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $fuentes
```

Los scripts de pruebas también compilan las fuentes y las pruebas.

## Ejecutar

Después de compilar:

```powershell
java -Dfile.encoding=UTF-8 -cp out interfaz.VentanaPrincipal
```

## Ejecutar las pruebas

En Windows:

```powershell
.\ejecutar_pruebas.bat
```

En Linux, macOS o Git Bash:

```sh
sh ejecutar_pruebas.sh
```

Los scripts compilan el proyecto y ejecutan las suites de validación, gestión
académica (incluida la persistencia), horarios y concentración/saturación. Un
código de salida distinto de cero indica compilación o pruebas fallidas.

Consulta el [manual de usuario](docs/ManualUsuario.md) y el
[diagrama de clases](docs/diagrama-clases.puml).
