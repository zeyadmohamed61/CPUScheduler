# CPU Scheduler Simulator 🖥️

A Java-based CPU scheduling simulator implementing multiple scheduling algorithms with both a command-line interface and an interactive Swing GUI.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Algorithms Implemented](#algorithms-implemented)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [GUI Guide](#gui-guide)
- [Output & Metrics](#output--metrics)

---

## Overview

This project simulates CPU process scheduling using a tick-based engine. Each scheduler advances one time unit per `tick()` call, making it easy to visualize execution step-by-step, run live simulations, or drive the scheduler from a GUI.

Key features:
- Tick-based architecture shared across all schedulers via `BaseScheduler`
- Live simulation mode with pause/resume and dynamic process injection
- Interactive Swing GUI with a color-coded Gantt chart
- Support for dynamic process addition at runtime via `SchedulerRunner`
- Custom exception handling for duplicate PIDs (`IllegalPIDValueException`)

---

## Algorithms Implemented

| Algorithm | Class | Preemptive |
|---|---|---|
| First Come First Served (FCFS) | `FCFSScheduler` | No |
| Shortest Job First (SJF) | `SJFNonPreeptiveScheduler` | No |
| Shortest Job First (SRTF) | `SJFPreeptiveSchedular` | Yes |
| Priority Scheduling | `PrioritySchedulerTick` | Both |
| Round Robin | `RoundRobinScheduler` | Yes (quantum-based) |

> **Priority note:** Lower priority number = higher priority.

---

## Project Structure

```
├── Process.java                  # Process data model
├── BaseScheduler.java            # Abstract base class for all schedulers
├── FCFSScheduler.java            # First Come First Served
├── SJFNonPreeptiveScheduler.java # SJF Non-Preemptive
├── SJFPreeptiveSchedular.java    # SJF Preemptive (SRTF)
├── RoundRobinScheduler.java      # Round Robin with configurable quantum
├── PrioritySchedulerTick.java    # Priority Scheduling (tick-based, both modes)
├── PriorityScheduler.java        # Standalone Priority Scheduler (self-contained)
├── GanttEntry.java               # Gantt chart block model
├── SchedulerRunner.java          # Thread-based live runner with pause support
├── SchedulerGUI.java             # Swing GUI
├── OSPROJECT.java                # CLI test harness (main entry point)
└── IllegalPIDValueException.java # Custom exception for duplicate PIDs
```

---

## Getting Started

### Prerequisites
- Java 8 or later
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code) or the `javac` CLI

### Compile

```bash
javac *.java
```

### Run (CLI)

```bash
java OSPROJECT
```

### Run (GUI)

```bash
java SchedulerGUI
```

---

## Usage

### CLI / Programmatic

```java
List<Process> processes = new ArrayList<>();
processes.add(new Process("P1", 0, 8));   // PID, arrivalTime, burstTime
processes.add(new Process("P2", 2, 4));
processes.add(new Process("P3", 4, 2));

FCFSScheduler scheduler = new FCFSScheduler(processes);

while (!scheduler.isFinished()) {
    scheduler.tick();
}
```

For **Priority Scheduling**, use the constructor with a priority value:

```java
processes.add(new Process("P1", 0, 10, 3)); // PID, arrival, burst, priority
PrioritySchedulerTick scheduler = new PrioritySchedulerTick(processes, true); // true = preemptive
```

For **Round Robin**, specify a time quantum:

```java
RoundRobinScheduler scheduler = new RoundRobinScheduler(processes, 2); // quantum = 2
```

### Live Simulation with Dynamic Process Injection

```java
FCFSScheduler scheduler = new FCFSScheduler(processes);
SchedulerRunner runner = new SchedulerRunner(scheduler);

runner.runLive();                              // starts background thread

Thread.sleep(3000);
runner.addProcess(new Process("P5", 3, 4));   // inject process at runtime

runner.setPaused();                            // pause execution
Thread.sleep(2000);
runner.disablePause();                         // resume
```

---

## GUI Guide

Launch `SchedulerGUI` to open the visual simulator.

**Left Panel — Configuration**
- **Algorithm:** Select one of the five scheduling algorithms.
- **Mode:** Choose *Static* (instant) or *Live Simulation* (1 tick/second).
- **Quantum:** Visible only when Round Robin is selected.
- **Add Process:** Enter PID, Arrival Time, Burst Time, and (if applicable) Priority, then click *Add to List*.

**Controls**
| Button | Action |
|---|---|
| ▶ Start | Run the scheduler with the current process list |
| ↺ Reset All | Clear everything and start fresh |
| ⏸ Pause / ▶ Resume | Pause or resume live simulation |
| + Add Process | Inject a new process during a paused live simulation |

**Right Panel — Output**
- **Gantt Chart:** Color-coded, scrollable timeline of process execution.
- **Remaining Burst Time Table:** Live status of each process (Waiting / Running ► / Done ✓).
- **Statistics:** Average Waiting Time and Average Turnaround Time.

---

## Output & Metrics

Each completed process reports:

| Metric | Formula |
|---|---|
| Completion Time | Time the process finishes execution |
| Turnaround Time | `Completion Time − Arrival Time` |
| Waiting Time | `Turnaround Time − Burst Time` |

Summary averages are printed after all processes complete (both CLI and GUI).

---

## Example Output (FCFS)

```
========================================
   TESTING: FCFS
========================================
Time 0: P1 is running (Remaining: 7)
Time 1: P1 is running (Remaining: 6)
...
Time 7: P1 is running (Remaining: 0)
Time 8: P2 is running (Remaining: 3)
...

Final Statistics:
Process  Arrival  Burst  Finish  Turnaround  Waiting
P1       0        8      8       8           0
P2       2        4      12      10          6
P3       4        2      14      10          8
P4       6        1      15      9           8

Average Waiting Time: 5.50
Average Turnaround Time: 9.25
```
