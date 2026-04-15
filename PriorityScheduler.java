import java.util.*;
 
public class PriorityScheduler {
 
    // ─── Process class ────────────────────────────────────────────────────────
    static class Process {
        String pid;
        int arrivalTime;
        int burstTime;
        int priority;      

        int remainingTime; //preemptive
        int startTime   = -1;
        int finishTime  = -1;
        int waitingTime = 0; //Waiting Time = Turnaround Time - Burst Time
        int turnaroundTime = 0; // Turnaround = Finish Time - Arrival Time
 
        Process(String pid, int arrivalTime, int burstTime, int priority) {
            this.pid           = pid;
            this.arrivalTime   = arrivalTime;
            this.burstTime     = burstTime;
            this.priority      = priority;
            this.remainingTime = burstTime;
        }
    }
 
    // GanttBlock : Pid +how long 
    static class GanttBlock {
        String pid;
        int start, end;
 
        GanttBlock(String pid, int start, int end) {
            this.pid   = pid;
            this.start = start;
            this.end   = end;
        }
    }
 
   
    //  NON-PREEMPTIVE PRIORITY SCHEDULING
    //  Once a process starts, it runs to completion.
    //  At each selection point, the highest-priority ready process is picked.

    public static void nonPreemptivePriority(List<Process> procs) {
        System.out.println("=== NON-PREEMPTIVE PRIORITY SCHEDULING ===\n");
 
        int n = procs.size();
        procs.sort(Comparator.comparingInt(p -> p.arrivalTime)); // sort by arivallTime
 
        List<GanttBlock> gantt    = new ArrayList<>();//Stores the Gantt chart blocks
        List<Process>    done     = new ArrayList<>();//Stores finished processes.
        boolean[]        finished = new boolean[n];//Tracks which processes finished.
        int currentTime   = 0;
        int finishedCount = 0;
 
        while (finishedCount < n) {
 
            // Pick the highest-priority process that has arrived
            Process best   = null; 
            int     bestIdx = -1;
 
            for (int i = 0; i < n; i++) {
                if (!finished[i] && procs.get(i).arrivalTime <= currentTime) {
                    Process p = procs.get(i);
                    if (best == null
                            || p.priority < best.priority
                            || (p.priority == best.priority && p.arrivalTime < best.arrivalTime)) {
                        best    = p;
                        bestIdx = i;
                    }
                }
            }
 
            if (best == null) {
                // CPU idle — jump to next arrival
                int nextArrival = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++)
                    if (!finished[i])
                        nextArrival = Math.min(nextArrival, procs.get(i).arrivalTime);
                gantt.add(new GanttBlock("IDLE", currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }
 
            // Run to completion (non-preemptive)
            if (best.startTime == -1) best.startTime = currentTime;
            gantt.add(new GanttBlock(best.pid, currentTime, currentTime + best.burstTime));
            currentTime         += best.burstTime;
            best.remainingTime   = 0;
            best.finishTime      = currentTime;
            best.turnaroundTime  = best.finishTime - best.arrivalTime;
            best.waitingTime     = best.turnaroundTime - best.burstTime;
 
            finished[bestIdx] = true;
            finishedCount++;
            done.add(best);
        }
 
        printGantt(gantt);
        printResults(done);
    }
 
    
    //  PREEMPTIVE PRIORITY SCHEDULING
    //  At every time unit the highest-priority ready process runs
    //  A newly arrived process with higher priority preempts the current one
 
    public static void preemptivePriority(List<Process> procs) {
        System.out.println("=== PREEMPTIVE PRIORITY SCHEDULING ===\n");
 
        int n = procs.size();
        for (Process p : procs) p.remainingTime = p.burstTime;
 
        List<GanttBlock> gantt        = new ArrayList<>();
        List<Process>    done         = new ArrayList<>();
        int[]            waitAccum    = new int[n];
 
        int currentTime   = 0;
        int finishedCount = 0;
        String lastPid    = "";
 
        while (finishedCount < n) {
 
            // Find highest-priority ready process
            Process running    = null;
            int     runningIdx = -1;
 
            for (int i = 0; i < n; i++) {
                Process p = procs.get(i);
                if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                    if (running == null
                            || p.priority < running.priority
                            || (p.priority == running.priority && p.arrivalTime < running.arrivalTime)) {
                        running    = p;
                        runningIdx = i;
                    }
                }
            }
 
            if (running == null) {
                // CPU idle
                if (!lastPid.equals("IDLE")) gantt.add(new GanttBlock("IDLE", currentTime, currentTime + 1));
                else                          gantt.get(gantt.size() - 1).end++;
                lastPid = "IDLE";
                currentTime++;
                continue;
            }
 
            if (running.startTime == -1) running.startTime = currentTime;
 
            // Extend or start Gantt block
            if (running.pid.equals(lastPid) && !gantt.isEmpty())
                gantt.get(gantt.size() - 1).end++;
            else
                gantt.add(new GanttBlock(running.pid, currentTime, currentTime + 1));
 
            // Accumulate waiting time for all OTHER ready processes
            for (int i = 0; i < n; i++) {
                if (i != runningIdx && procs.get(i).arrivalTime <= currentTime && procs.get(i).remainingTime > 0)
                    waitAccum[i]++;
            }
 
            running.remainingTime--;
            lastPid = running.pid;
            currentTime++;
 
            if (running.remainingTime == 0) {
                running.finishTime     = currentTime;
                running.turnaroundTime = running.finishTime - running.arrivalTime;
                running.waitingTime    = waitAccum[runningIdx];
                finishedCount++;
                done.add(running);
            }
        }
 
        printGantt(gantt);
        printResults(done);
    }
 
 
    //  OUTPUT HELPERS
   
    static void printGantt(List<GanttBlock> gantt) {
        System.out.println("Gantt Chart:");
 
        StringBuilder top  = new StringBuilder();
        StringBuilder mid  = new StringBuilder();
        StringBuilder bot  = new StringBuilder();
        StringBuilder time = new StringBuilder();
 
        for (GanttBlock b : gantt) {
            int width = Math.max(b.end - b.start, 1) * 3;
            int pad   = width - b.pid.length();
            int left  = pad / 2;
            int right = pad - left;
            top.append("+").append("-".repeat(width));
            mid.append("|").append(" ".repeat(left)).append(b.pid).append(" ".repeat(right));
            bot.append("+").append("-".repeat(width));
        }
        top.append("+");
        mid.append("|");
        bot.append("+");
 
        time.append(gantt.get(0).start);
        for (GanttBlock b : gantt) {
            int width = Math.max(b.end - b.start, 1) * 3;
            String lbl = String.valueOf(b.end);
            time.append(" ".repeat(width - lbl.length() + 1)).append(lbl);
        }
 
        System.out.println(top);
        System.out.println(mid);
        System.out.println(bot);
        System.out.println(time);
        System.out.println();
    }
 
    static void printResults(List<Process> done) {
        done.sort(Comparator.comparing(p -> p.pid));
 
        System.out.printf("%-8s %-10s %-10s %-10s %-10s %-14s %-14s%n",
            "PID", "Arrival", "Burst", "Priority", "Start", "Waiting", "Turnaround");
        System.out.println("-".repeat(76));
 
        double totalWT = 0, totalTAT = 0;
        for (Process p : done) {
            System.out.printf("%-8s %-10d %-10d %-10d %-10d %-14d %-14d%n",
                p.pid, p.arrivalTime, p.burstTime, p.priority,
                p.startTime, p.waitingTime, p.turnaroundTime);
            totalWT  += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
 
        System.out.println("-".repeat(76));
        System.out.printf("Average Waiting Time    : %.2f%n", totalWT  / done.size());
        System.out.printf("Average Turnaround Time : %.2f%n", totalTAT / done.size());
        System.out.println();
    }
 
   
    //  MAIN — sample test
   
    public static void main(String[] args) {
        /*
         * PID  Arrival  Burst  Priority
         * P1      0       10      3
         * P2      2        5      1   <- highest priority
         * P3      4        8      4
         * P4      6        2      2
         * P5      8        3      2
         */
        List<Process> base = new ArrayList<>();
        base.add(new Process("P1", 0, 10, 3));
        base.add(new Process("P2", 2,  5, 1));
        base.add(new Process("P3", 4,  8, 4));
        base.add(new Process("P4", 6,  2, 2));
        base.add(new Process("P5", 8,  3, 2));
 
        // deep-copy helper so both algorithms start fresh
        List<Process> copy1 = new ArrayList<>(), copy2 = new ArrayList<>();
        for (Process p : base) {
            copy1.add(new Process(p.pid, p.arrivalTime, p.burstTime, p.priority));
            copy2.add(new Process(p.pid, p.arrivalTime, p.burstTime, p.priority));
        }
 
        nonPreemptivePriority(copy1);
        preemptivePriority(copy2);
    }
}
 