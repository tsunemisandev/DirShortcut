@echo off
set JAR=build\libs\DirShortcut.jar

if not exist "%JAR%" (
    echo Building DirShortcut...
    call gradlew.bat jar
    if errorlevel 1 (
        echo Build failed.
        pause
        exit /b 1
    )
)

java -jar "%JAR%"
