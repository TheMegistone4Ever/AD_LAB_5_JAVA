package abc;

public class Bee implements constants {
    public enum Status { ONLOOKER, EMPLOYED, SCOUT }
    private int[] currentPath;
    private Status currentStatus;
    private int currentPathDistance, unluckyIterateCount;

    public Bee(Status currentStatus) {
        this.currentStatus = currentStatus;
        currentPath = null;
        currentPathDistance = 0;
        unluckyIterateCount = 0;
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

    public void setUnluckyIterateCount(int unluckyIterateCount) {
        this.unluckyIterateCount = unluckyIterateCount;
    }

    public void changePath(int[] path, int pathDistance) {
        currentPath = path;
        currentPathDistance = pathDistance;
        unluckyIterateCount = 0;
    }

    public boolean isUnluckyOverLimit() {
        return unluckyIterateCount > UNL_LIM;
    }

    public void stayIdle() {
        ++unluckyIterateCount;
    }
}
