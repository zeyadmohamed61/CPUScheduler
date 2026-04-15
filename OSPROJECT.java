
import java.util.ArrayList;
import java.util.List;



public class OSPROJECT {
   
    public static void main(String[] args) throws IllegalPIDValueException, InterruptedException {
        
        // Define common test data
        // P1: Arrival 0, Burst 8
        // P2: Arrival 1, Burst 4
        // P3: Arrival 2, Burst 2
        // P4: Arrival 3, Burst 1
        System.out.println("\n========================================");
        System.out.println("   TESTING: FCFS");
        System.out.println("========================================");
        runFCFSTest();

        System.out.println("========================================");
        System.out.println("   TESTING: SJF NON-PREEMPTIVE");
        System.out.println("========================================");
        runNonPreemptiveTest();

        System.out.println("\n========================================");
        System.out.println("   TESTING: SJF PREEMPTIVE");
        System.out.println("========================================");
        runPreemptiveTest();
        
        
         System.out.println("\n========================================");
        System.out.println("   TESTING: runRoundRobin");
        System.out.println("========================================");
     runRoundRobinTest(2);

        System.out.println("\n========================================");
        System.out.println("   TESTING: SCHEDULER RUNNER");
        System.out.println("========================================");
        runSchedulerRunnerTest();
    }
    //------------------------------------------------------------
    
    private static void runRoundRobinTest(int quantum) {
    System.out.println("\n========================================");
    System.out.println("   TESTING: ROUND ROBIN (Quantum: " + quantum + ")");
    System.out.println("========================================");

    List<Process> processes = createProcessList();
    RoundRobinScheduler scheduler = new RoundRobinScheduler(processes, quantum);
    List<Process> trackingList = new ArrayList<>(processes);

    while (!scheduler.isFinished()) {
        int time = scheduler.currentTime;
        scheduler.tick();

        // Use lastRunningProcess — valid even when quantum expired or process finished
        if (scheduler.lastRunningProcess != null) {
            System.out.printf("Time %2d: %s running  (Remaining: %d)\n",
                    time,
                    scheduler.lastRunningProcess.processID,
                    scheduler.lastRunningProcess.remainingBurstTime);
        } else {
            System.out.printf("Time %2d: CPU Idle\n", time);
        }
    }

    scheduler.printGanttChart();
    printResults(trackingList);
}
    //--------------------------------------------------------------------------------
    private static void runNonPreemptiveTest() {
        List<Process> processes = createProcessList();
        SJFNonPreeptiveScheduler scheduler = new SJFNonPreeptiveScheduler(processes);

        // Keep a reference to see final results since they are modified
        List<Process> trackingList = new ArrayList<>(processes);

        while (!scheduler.isFinished()) {
            int time = scheduler.currentTime;
            scheduler.tick();

            if (scheduler.runningProcess != null) {
                System.out.printf("Time %d: %s is running (Remaining: %d)\n",
                        time, scheduler.runningProcess.processID, scheduler.runningProcess.remainingBurstTime);
            } else {
                System.out.printf("Time %d: CPU Idle\n", time);
            }
        }
        printResults(trackingList);
    }

    private static void runPreemptiveTest() {
        List<Process> processes = createProcessList();
        SJFPreeptiveSchedular scheduler = new SJFPreeptiveSchedular(processes);
        List<Process> trackingList = new ArrayList<>(processes);

        while (!scheduler.isFinished()) {
            int time = scheduler.currentTime;
            scheduler.tick();

            if (scheduler.runningProcess != null) {
                System.out.printf("Time %d: %s is running (Remaining: %d)\n",
                        time, scheduler.runningProcess.processID, scheduler.runningProcess.remainingBurstTime);
            } else {
                System.out.printf("Time %d: CPU Idle\n", time);
            }
        }
        printResults(trackingList);
    }

    private static void runFCFSTest(){
        List<Process> processes = createProcessList();
        FCFSScheduler scheduler = new FCFSScheduler(processes);
        List<Process> trackingList = new ArrayList<>(processes);
        while (!scheduler.isFinished()) {
            int time = scheduler.currentTime;
            scheduler.tick();

            if (scheduler.runningProcess != null) {
                System.out.printf("Time %d: %s is running (Remaining: %d)\n",
                        time, scheduler.runningProcess.processID, scheduler.runningProcess.remainingBurstTime);
            } else {
                System.out.printf("Time %d: CPU Idle\n", time);
            }
        }
        printResults(trackingList);
    }

    private static List<Process> createProcessList() {
        List<Process> list = new ArrayList<>();
        list.add(new Process("P1", 0, 8));
        list.add(new Process("P2", 2, 4));
        list.add(new Process("P3", 4, 2));
        list.add(new Process("P4", 6, 1));
        return list;
    }

    private static void printResults(List<Process> processes) {
        System.out.println("\nFinal Statistics:");
        System.out.println("Process\tArrival\tBurst\tFinish\tTurnaround\tWaiting");
        double totalWait = 0, totalTAT = 0;
        for (Process p : processes) {
            System.out.println(p.processID + "\t" + p.arrivalTime + "\t" + p.burstTime + "\t" +
                    p.completionTime + "\t" + p.turnaroundTime + "\t\t" + p.waitingTime);
            totalWait += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        System.out.printf("\nAverage Waiting Time: %.2f", (totalWait / processes.size()));
        System.out.printf("\nAverage Turnaround Time: %.2f\n", (totalTAT / processes.size()));
    }

    private static void runSchedulerRunnerTest() throws InterruptedException, IllegalPIDValueException {
        System.out.println("\n========================================");
        System.out.println("   TESTING: SCHEDULER RUNNER (LIVE)");
        System.out.println("========================================");

        List<Process> processes = createProcessList();
        // We use a tracking list to print the final stats later
        List<Process> trackingList = new ArrayList<>(processes);

        // You can swap this with RoundRobinScheduler or SJF to test their live behavior too!
        FCFSScheduler scheduler = new FCFSScheduler(processes);
        SchedulerRunner runner = new SchedulerRunner(scheduler);

        // 1. Start the live background thread
        runner.runLive();

        // 2. Wait 3 seconds, then inject a new process dynamically.
        // If there is a threading bug, this is exactly where it will crash.
        Thread.sleep(3000);
        System.out.println("\n>>> [MAIN THREAD] Adding P5 dynamically at runtime...\n");
        Process p5 = new Process("P5", 3, 4);
        runner.addProcess(p5);
        trackingList.add(p5); // Add to our tracker so printResults knows about it

        // 3. Wait 2 more seconds, then test the PAUSE logic.
        Thread.sleep(2000);
        System.out.println("\n>>> [MAIN THREAD] Pausing runner for 3 seconds...\n");
        runner.setPaused();

        // 4. Wait 3 seconds to visually confirm nothing is printing out (CPU is paused).
        Thread.sleep(3000);
        System.out.println("\n>>> [MAIN THREAD] Resuming runner...\n");
        runner.disablePause();

        // 5. Safely wait for the background thread to finish its work.
        // We lock the scheduler briefly just to check if it's finished without causing a race condition.
        boolean finished = false;
        while (!finished) {
            synchronized(scheduler) {
                finished = scheduler.isFinished();
            }
            if (!finished) {
                Thread.sleep(500);
            }
        }

        System.out.println("\n>>> [MAIN THREAD] Scheduler finished. Printing results:");
        printResults(trackingList);
    }
}
