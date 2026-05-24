@echo off
REM Ejecuta el juego con el código compilado actual.
cd /d "%~dp0"
"C:\Program Files\Apache\Maven\apache-maven-3.9.15\bin\mvn.cmd" clean compile exec:java
pause
