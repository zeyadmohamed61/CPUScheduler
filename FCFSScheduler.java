
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class FCFSScheduler extends BaseScheduler {

    public PriorityQueue<Process> readyQueue;

    public FCFSScheduler(List<Process> processes) {
        this.allProcesses = new ArrayList<>(processes);
        this.runningProcess = null;
        this.currentTime = 0;
        this.readyQueue = new PriorityQueue<>(
                Comparator.comparingInt((Process p) -> p.arrivalTime)
        );
    }

    public void tick() {
        lastRunningProcess = null; // Reset at the start of each tick

        java.util.Iterator<Process> iterator = allProcesses.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            if (p.arrivalTime == currentTime) {
                readyQueue.add(p);
                iterator.remove();
            }
        }

        if (runningProcess == null && !readyQueue.isEmpty()) {
            runningProcess = readyQueue.poll();
        }

        // CAPTURE the running process BEFORE modifying or nulling it
        lastRunningProcess = runningProcess;

        if (runningProcess != null) {
            runningProcess.remainingBurstTime--;
        }

        if (runningProcess != null && runningProcess.remainingBurstTime == 0) {
            runningProcess.completionTime = currentTime + 1;
            runningProcess.turnaroundTime = runningProcess.completionTime - runningProcess.arrivalTime;
            runningProcess.waitingTime = runningProcess.turnaroundTime - runningProcess.burstTime;
            runningProcess = null;
        }

        currentTime++;
    }

    public boolean isFinished() {
        return allProcesses.isEmpty() && readyQueue.isEmpty() && runningProcess == null;
    }
}