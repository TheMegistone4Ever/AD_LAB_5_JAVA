package abc;

public class Bee {
    public enum Status { ONLOOKER, EMPLOYED, SCOUT }
    public static int maxUnluckyItersCount = 10;
    private int[] currentPath;
    private Status currentStatus;
    private int currentPathDistance;
    private int unluckyIterCount;

    public Bee(Status status) {
        this.currentStatus = status;
        this.currentPath = null;
        this.currentPathDistance = 0;
        unluckyIterCount = 0;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public int[] getCurrentPath() {
        return currentPath.clone();
    }

    public int getCurrentPathDistance() {
        return currentPathDistance;
    }

    public void setUnluckyIterCount(int unluckyIterCount) {
        this.unluckyIterCount = unluckyIterCount;
    }

    public void changePath(int[] path, int pathDistance) {
        currentPath = path.clone(); // *******************************************************************************
        currentPathDistance = pathDistance;
        unluckyIterCount = 0;
    }

    public boolean isUnluckyOverLimit() {
        return unluckyIterCount > maxUnluckyItersCount;
    }

    // ожидает
    public void stayIdle() {
        ++unluckyIterCount;
    }

    @Override public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Status = ").append(currentStatus).append("\n");
        s.append(" Memory = ");
        for (int i = 0; i < currentPath.length - 1; ++i)
            s.append(currentPath[i]).append("-");
        s.append(currentPath[currentPath.length - 1]).append("\n");
        s.append(" Distance = ").append(currentPathDistance);
        return s.toString();
    }
}
