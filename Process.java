public class Process {

    public String processID;
    public int arrivalTime;
    public int burstTime;
    public int priority;

    public int remainingBurstTime;
    public int completionTime;
    public int turnaroundTime;
    public int waitingTime;

    // Constructor without priority (for FCFS, SJF, Round Robin)
    public Process(String processID, int arrivalTime, int burstTime) {
        this.processID = processID;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = 0;
        this.remainingBurstTime = burstTime;
        this.completionTime = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
    }

    // Constructor with priority (for Priority Scheduling)
    public Process(String processID, int arrivalTime, int burstTime, int priority) {
        this(processID, arrivalTime, burstTime);
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Process{ID='" + processID + "', Arrival=" + arrivalTime +
               ", Burst=" + burstTime + ", Remaining=" + remainingBurstTime + '}';
    }
}
