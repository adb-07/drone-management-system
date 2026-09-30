@echo off
setlocal
cd /d "%~dp0"
where mvn >nul 2>&1
if errorlevel 1 (
  echo Maven was not found on PATH.
  echo Install Maven or import pom.xml in IntelliJ IDEA.
  pause
  exit /b 1
)
mvn clean test package
pause
