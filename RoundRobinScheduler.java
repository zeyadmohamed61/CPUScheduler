import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobinScheduler extends BaseScheduler{

    private int quantum;
    private int quantumCounter;
    private int runningStartTime;          // when current slice started

    private Queue<Process> readyQueue;
    private List<Process> completedProcesses;
    private List<GanttEntry> ganttChart;   // NEW: track slices

    public RoundRobinScheduler(List<Process> processes, int quantum) {
        this.quantum              = quantum;
        this.quantumCounter       = 0;
        this.currentTime          = 0;
        this.readyQueue           = new LinkedList<>();
        this.allProcesses = new ArrayList<>(processes);
        this.completedProcesses   = new ArrayList<>();
        this.ganttChart           = new ArrayList<>();
    }

    public boolean isFinished() {
        return allProcesses.isEmpty()
            && readyQueue.isEmpty()
            && runningProcess == null;
    }

   public void tick() {
    lastRunningProcess = null;  // ← reset each tick

    // 1. Admit arrivals
    List<Process> justArrived = new ArrayList<>();
    for (Process p : allProcesses) {
        if (p.arrivalTime <= currentTime) {
            readyQueue.add(p);
            justArrived.add(p);
        }
    }
    allProcesses.removeAll(justArrived);

    // 2. Pick next process if CPU free
    if (runningProcess == null && !readyQueue.isEmpty()) {
        runningProcess   = readyQueue.poll();
        quantumCounter   = 0;
        runningStartTime = currentTime;
    }

    // 3. Execute
    if (runningProcess != null) {
        lastRunningProcess = runningProcess;  // ← capture BEFORE any nulling

        runningProcess.remainingBurstTime--;
        quantumCounter++;

        if (runningProcess.remainingBurstTime == 0) {
            runningProcess.completionTime = currentTime + 1;
            runningProcess.turnaroundTime = runningProcess.completionTime - runningProcess.arrivalTime;
            runningProcess.waitingTime    = runningProcess.turnaroundTime  - runningProcess.burstTime;
            
            ganttChart.add(new GanttEntry(runningProcess.processID, runningStartTime, currentTime + 1));
            completedProcesses.add(runningProcess);
            runningProcess = null;
            quantumCounter = 0;

        } else if (quantumCounter == quantum) {
            // Log the Gantt entry before switching
            ganttChart.add(new GanttEntry(runningProcess.processID, runningStartTime, currentTime + 1));
            
            // THE LOOK-AHEAD FIX (Crucial for simultaneous arrivals)
            List<Process> nextArrivals = new ArrayList<>();
            for (Process p : allProcesses) {
                if (p.arrivalTime == currentTime + 1) { 
                    readyQueue.add(p);
                    nextArrivals.add(p);
                }
            }
            allProcesses.removeAll(nextArrivals);

            // NOW send the running process to the back of the queue
            readyQueue.add(runningProcess);
            runningProcess = null;
            quantumCounter = 0;
        }
    }

    currentTime++;
}

    // ── Gantt chart printer ──────────────────────────────────────────────────
    public void printGanttChart() {
        System.out.println("\nGantt Chart:");

        StringBuilder top      = new StringBuilder();
        StringBuilder timeline = new StringBuilder();

        for (GanttEntry e : ganttChart) {
            String label = " " + e.processID + " ";
            top.append("+").append("-".repeat(label.length()));
            timeline.append("|").append(label);
        }
        top.append("+");
        timeline.append("|");

        // Time markers
        StringBuilder times = new StringBuilder();
        times.append(ganttChart.get(0).startTime);
        for (GanttEntry e : ganttChart) {
            // pad to align under each block boundary
            String end = String.valueOf(e.endTime);
            int blockLen = (" " + e.processID + " ").length() + 1; // +1 for '|'
            int pad = blockLen - String.valueOf(e.startTime).length();
            // we already printed startTime; just pad to next boundary
            times.append(" ".repeat(Math.max(1,
                (" " + e.processID + " ").length() + 1 - end.length())))
                 .append(end);
        }

        System.out.println(top);
        System.out.println(timeline);
        System.out.println(top);
        System.out.println(times);
    }
}