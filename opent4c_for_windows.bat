@echo off
for /f tokens^=2^ delims^=^" %%i in ('reg query HKEY_CLASSES_ROOT\jarfile\shell\open\command /ve') do set JAVAW_PATH=%%i

if "%JAVAW_PATH%"=="" echo "Vous devez installer java. Vous allez etre redirige vers la page de telechargement."
if "%JAVAW_PATH%"=="" echo "Relancez le programme une fois l'installation terminee."
if "%JAVAW_PATH%"=="" pause
if "%JAVAW_PATH%"=="" start "" "https://www.java.com/fr/download/index.jsp"
if "%JAVAW_PATH%"=="" exit 1

set JAVA_PATH=%JAVAW_PATH:\javaw.exe=%

echo "Lancement de OpenT4C"

"%JAVA_PATH%\java" -jar .\opent4c.jar 1> .\opent4c.log 2> .\error.log

echo "Consultez le fichier opent4c.log pour les informations generales et error.log en cas d'erreur."

pause
exit 0

