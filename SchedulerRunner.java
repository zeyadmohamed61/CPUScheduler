public class SchedulerRunner {
    BaseScheduler scheduler;
    Thread schedulerThread;
    volatile boolean paused = false;

    public SchedulerRunner(BaseScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void runBatch() {
        while(!scheduler.isFinished()) {
            scheduler.tick();
            printCurrentState();
        }
    }

    public void runLive() throws InterruptedException {
        schedulerThread = new Thread(() -> {
            boolean finished = false;
            while (!finished) {
                // LOCK the scheduler so addProcess can't interrupt the tick/check
                synchronized (scheduler) {
                    finished = scheduler.isFinished();
                    if (!finished) {
                        scheduler.tick();
                    }
                }

                if (!finished) {
                    printCurrentState();
                    try {
                        Thread.sleep(1000);
                        while (paused) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
        schedulerThread.start();
    }

    public void addProcess(Process p) throws IllegalPIDValueException {
        // LOCK the scheduler before adding a new process
        synchronized (scheduler) {
            scheduler.addProcess(p);
        }
    }

    public void stop() {
        if (schedulerThread != null) {
            schedulerThread.interrupt();
        }
    }

    public void setPaused() {
        paused = true;
    }

    public void disablePause() {
        paused = false;
    }

    public void printCurrentState() {
        if(scheduler.lastRunningProcess == null) {
            System.out.println("CPU Idle");
            return;
        }
        System.out.printf("Current Time: %d | Current Process ID: %s | Arrival Time: %d | Remaining Time: %d\n",
                scheduler.currentTime,
                scheduler.lastRunningProcess.processID,
                scheduler.lastRunningProcess.arrivalTime,
                scheduler.lastRunningProcess.remainingBurstTime);
    }
}