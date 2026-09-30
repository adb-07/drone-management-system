@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>&1
if errorlevel 1 (
  echo Maven was not found on PATH.
  echo Open this folder in IntelliJ IDEA and run com.dronefleet.app.Application,
  echo or install Maven and run: mvn clean package
  pause
  exit /b 1
)
mvn clean package
if errorlevel 1 (
  echo Build failed.
  pause
  exit /b 1
)
java -jar "target\drone-management-system-2.0.0-SNAPSHOT.jar"
