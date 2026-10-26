@echo off
setlocal
title ReyaanshsStuff Backup Builder

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Java 21 not found.
    pause
    exit /b 1
)

call gradlew.bat --stop
call gradlew.bat assembleDebug

if errorlevel 1 (
    echo BUILD FAILED
    pause
    exit /b 1
)

echo BUILD SUCCESSFUL
echo APK: %CD%\app\build\outputs\apk\debug\app-debug.apk
explorer "%CD%\app\build\outputs\apk\debug"
pause
