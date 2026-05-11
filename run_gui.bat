@echo off
echo Compiling OOP Battle Arena...
cd src
javac *.java
if %errorlevel% neq 0 (
    echo Compilation FAILED. Check Java is installed: java -version
    pause
    exit /b 1
)
echo Launching GUI...
java BattleGUI
pause
