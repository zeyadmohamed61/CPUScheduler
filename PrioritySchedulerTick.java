import java.util.*;

/**
 * Tick-based Priority Scheduler (both Preemptive and Non-Preemptive).
 * Extends BaseScheduler so it plugs into SchedulerRunner and SchedulerGUI
 * exactly like FCFS, SJF, and RoundRobin.
 *
 * Lower priority number = higher priority (as per project spec).
 */
public class PrioritySchedulerTick extends BaseScheduler {

    private final PriorityQueue<Process> readyQueue;
    private final boolean preemptive;

    public PrioritySchedulerTick(List<Process> processes, boolean preemptive) {
        this.allProcesses     = new ArrayList<>(processes);
        this.preemptive       = preemptive;
        this.currentTime      = 0;
        this.runningProcess   = null;
        this.lastRunningProcess = null;

        // Order: lower priority number first, tie-break by arrival time
        this.readyQueue = new PriorityQueue<>(
            Comparator.comparingInt((Process p) -> p.priority)
                      .thenComparingInt(p -> p.arrivalTime)
        );
    }

    @Override
    public void tick() {
        lastRunningProcess = null;

        // 1. Admit all processes that have arrived by currentTime
        Iterator<Process> it = allProcesses.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            if (p.arrivalTime <= currentTime) {
                readyQueue.add(p);
                it.remove();
            }
        }

        // 2. Preemptive: push running process back so we re-evaluate every tick
        if (preemptive && runningProcess != null && runningProcess.remainingBurstTime > 0) {
            readyQueue.add(runningProcess);
            runningProcess = null;
        }

        // 3. Select process if CPU is idle
        if (runningProcess == null && !readyQueue.isEmpty()) {
            runningProcess = readyQueue.poll();
        }

        // 4. Record who ran this tick (before potentially nulling it)
        lastRunningProcess = runningProcess;

        // 5. Execute one unit
        if (runningProcess != null) {
            runningProcess.remainingBurstTime--;
        }

        // 6. Check for completion
        if (runningProcess != null && runningProcess.remainingBurstTime == 0) {
            runningProcess.completionTime  = currentTime + 1;
            runningProcess.turnaroundTime  = runningProcess.completionTime - runningProcess.arrivalTime;
            runningProcess.waitingTime     = runningProcess.turnaroundTime - runningProcess.burstTime;
            runningProcess = null;
        }

        currentTime++;
    }

    @Override
    public boolean isFinished() {
        return allProcesses.isEmpty() && readyQueue.isEmpty() && runningProcess == null;
    }
}
