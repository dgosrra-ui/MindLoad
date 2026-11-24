@echo off
setlocal EnableDelayedExpansion
if not exist out mkdir out
set "SOURCES="
for /r src %%F in (*.java) do set SOURCES=!SOURCES! "%%F"
for /r pruebas %%F in (*.java) do set SOURCES=!SOURCES! "%%F"
javac -encoding UTF-8 -d out !SOURCES!
if errorlevel 1 exit /b 1
call :ejecutar pruebas.negocio.PruebasValidadorDatos
if errorlevel 1 exit /b 1
call :ejecutar pruebas.negocio.PruebaGestorCargaAcademica
if errorlevel 1 exit /b 1
call :ejecutar pruebas.negocio.PruebasGestorHorarios
if errorlevel 1 exit /b 1
call :ejecutar pruebas.negocio.concentracion.PruebasGestionSaturacion
if errorlevel 1 exit /b 1
exit /b 0

:ejecutar
java -Dfile.encoding=UTF-8 -cp out %1
exit /b %errorlevel%
