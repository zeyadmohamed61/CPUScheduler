import java.util.List;

public abstract class BaseScheduler {
    public int currentTime;
    public Process runningProcess;
    public Process lastRunningProcess;
    public List<Process> allProcesses;

    public abstract boolean isFinished();
    public abstract void tick();

    public void addProcess (Process P) throws IllegalPIDValueException
    {
        // Checking for the PID.
        for(int i = 0; i< allProcesses.size(); i++)
        {
            if(P.processID.equals(allProcesses.get(i).processID))
            {
                throw new IllegalPIDValueException();
            }
        }
        allProcesses.add(P);
    }
}
