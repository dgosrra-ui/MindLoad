#!/bin/sh
set -eu

mkdir -p out
find src pruebas -name '*.java' -print > out/sources.txt
javac -encoding UTF-8 -d out @out/sources.txt
java -Dfile.encoding=UTF-8 -cp out pruebas.negocio.PruebasValidadorDatos
java -Dfile.encoding=UTF-8 -cp out pruebas.negocio.PruebaGestorCargaAcademica
java -Dfile.encoding=UTF-8 -cp out pruebas.negocio.PruebasGestorHorarios
java -Dfile.encoding=UTF-8 -cp out pruebas.negocio.concentracion.PruebasGestionSaturacion
