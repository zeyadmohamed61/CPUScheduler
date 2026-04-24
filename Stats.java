/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author johnh
 */
// Stats.java
import java.util.List;

public class Stats {

    // Print the full stats table + averages to console
    public static void printStats(List<Process> processes) {
        System.out.println("\nFinal Statistics:");
        System.out.println("Process\tArrival\tBurst\tFinish\tTurnaround\tWaiting");

        double totalWait = 0, totalTAT = 0;

        for (Process p : processes) {
            System.out.println(
                p.processID + "\t" +
                p.arrivalTime + "\t" +
                p.burstTime + "\t" +
                p.completionTime + "\t" +
                p.turnaroundTime + "\t\t" +
                p.waitingTime
            );
            totalWait += p.waitingTime;
            totalTAT  += p.turnaroundTime;
        }

        System.out.printf("\nAverage Waiting Time    : %.2f%n", totalWait / processes.size());
        System.out.printf("Average Turnaround Time : %.2f%n", totalTAT  / processes.size());
    }

    // Return average waiting time (used by JavaFX UI later)
    public static double averageWaitingTime(List<Process> processes) {
        double total = 0;
        for (Process p : processes) total += p.waitingTime;
        return total / processes.size();
    }

    // Return average turnaround time (used by JavaFX UI later)
    public static double averageTurnaroundTime(List<Process> processes) {
        double total = 0;
        for (Process p : processes) total += p.turnaroundTime;
        return total / processes.size();
    }
}
