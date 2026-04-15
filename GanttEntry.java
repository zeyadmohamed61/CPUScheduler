/*public class GanttEntry {
    public String pid;
    public int startTime;
    public int endTime;
}*/


public class GanttEntry {
    public String processID;
    public int startTime;
    public int endTime;

    public GanttEntry(String processID, int startTime, int endTime) {
        this.processID = processID;
        this.startTime = startTime;
        this.endTime   = endTime;
    }

    @Override
    public String toString() {
        return "| " + processID + " (" + startTime + "-" + endTime + ") ";
    }
}