#!/bin/bash
echo "Compiling CPU Scheduler..."
mkdir -p out
javac -d out *.java || { echo "Compilation FAILED."; exit 1; }
echo "Creating executable JAR..."
echo "Main-Class: SchedulerGUI" > manifest.txt
jar cfm CPUScheduler.jar manifest.txt -C out .
rm manifest.txt
echo "Done! Run with:  java -jar CPUScheduler.jar"
