@echo off
echo Compiling CPU Scheduler...
mkdir out 2>nul
javac -d out *.java
if %errorlevel% neq 0 (
    echo Compilation FAILED.
    pause
    exit /b 1
)
echo.
echo Creating executable JAR...
echo Main-Class: SchedulerGUI > manifest.txt
jar cfm CPUScheduler.jar manifest.txt -C out .
del manifest.txt
echo.
echo Done! Run with:  java -jar CPUScheduler.jar
pause
