package abc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class AlgorithmImplementation implements constants {
    private final Random rand = new Random();
    // ймовірність бездіяльної бджоли відгукнутися на танець розвідника
    private final double persuasionProbability, mistakeProbability;
    // за одну ітерацію всі агенти здійснюють по одній дії
    private final int workerCount, scoutCount, pathStart, pathEnd, maxCycles, reportEvery;
    private int bestDistance;
    private int[] bestPath;
    Graph graph;
    ArrayList<Bee> scouts, onlookers, employed;
    HashMap<int[], Integer> scoutedPaths; // рішення, знайдені розвідниками

    public AlgorithmImplementation(int workerCount, int scoutCount,
                                   Graph graph, int pathStart, int pathEnd, int maxCycles, int reportEvery) {
        this.maxCycles = maxCycles;
        this.reportEvery = reportEvery;
        this.graph = graph;
        this.pathStart = pathStart;
        this.pathEnd = pathEnd;
        this.scoutedPaths = new HashMap<>();
        persuasionProbability = 0.95;
        mistakeProbability = 0.01;
        this.workerCount = workerCount;
        this.scoutCount = scoutCount;
    }

    // створює початкову популяцію: розвідники та фуражири, які очікують у вулику
    private void produceInitialPopulation() {
        employed = new ArrayList<>(workerCount);
        onlookers = new ArrayList<>(workerCount);
        scouts = new ArrayList<>(scoutCount);
        for (int i = 0; i < workerCount; ++i)
            onlookers.add(new Bee(Bee.Status.ONLOOKER));
        for (int i = 0; i < scoutCount; ++i)
            scouts.add(new Bee(Bee.Status.SCOUT));
    }

    public void solve() {
        produceInitialPopulation();
        long start = System.currentTimeMillis();
        while (scoutedPaths.size() <= MAX_AREAS) {
            for (int i = 0; i < maxCycles; ) {
                for (int k = 0; k < reportEvery; ++k) {
                    scoutPhase();
                    onlookerPhase();
                    employedPhase();
                    keepBestPath();
                    System.out.println(scoutedPaths.size());
                }
                System.out.printf("Iteration #%d\n%s\n", i += reportEvery, this);
            }
        }
        System.out.printf("Solution time - %8d seconds\n", (System.currentTimeMillis() - start) / 1000);
        System.out.printf("Best path: %s\n" +
                "Is valid? - %b.", Arrays.toString(bestPath), graph.isValidPath(bestPath));
    }

    // якщо робітникам вдалося знайти краще рішення, запам'ятовуємо його
    private void keepBestPath() {
        for (Bee employ: employed)
            if (bestPath == null || employ.getCurrentPathDistance() < bestDistance) {
                bestDistance = employ.getCurrentPathDistance();
                bestPath = employ.getCurrentPath();
            }
    }

    private void employedPhase() {
        for (Bee employ: employed) processEmployedBee(employ);
        employed.removeIf(employ -> employ.getCurrentStatus() != Bee.Status.EMPLOYED);
    }

    private void processEmployedBee(@NotNull Bee employedBee) {
        int[] neighborSolution = graph.modifyRandomPath(employedBee.getCurrentPath());
        int neighborDistance = graph.measureDistance(neighborSolution);
        boolean isMistaken = rand.nextDouble() < mistakeProbability,
                foundNewSolution = neighborDistance < employedBee.getCurrentPathDistance();
        if (foundNewSolution ^ isMistaken) employedBee.changePath(neighborSolution, neighborDistance);
        else employedBee.stayIdle();
        // бджола-невдаха припиняє спроби поліпшити шлях
        if (employedBee.isUnluckyOverLimit()) {
            employedBee.setCurrentStatus(Bee.Status.ONLOOKER);
            employedBee.setUnluckyIterateCount(0);
            onlookers.add(employedBee);
        }
    }

    private void onlookerPhase() {
        HashMap<Double, int[]> rollingWheel = createScoutedPathsRollingWheel();
        for (Bee onlooker: onlookers)
            processOnlookerBee(onlooker, rollingWheel);
        onlookers.removeIf(onlooker -> onlooker.getCurrentStatus() != Bee.Status.ONLOOKER);
    }

    // будує рулетку рішень розвідників = проєктує кожне рішення в відрізок усередині [0..1]
    // довжини відрізків пропорційні добротності значень цільової функції
    private @NotNull HashMap<Double, int[]> createScoutedPathsRollingWheel() {
        int distanceSum = 0;
        for (int[] path: scoutedPaths.keySet())
            distanceSum += scoutedPaths.get(path);
        HashMap<Double, int[]> res = new HashMap<>();
        double prevProb = 0f;
        for (int[] path: scoutedPaths.keySet()) {
            double prob = 1f - scoutedPaths.get(path) / (double)distanceSum;
            res.put(prevProb + prob, path);
            prevProb += prob;
        }
        return res;
    }

    private void scoutPhase() {
        scoutedPaths.clear();
        for (Bee scout: scouts) processScoutBee(scout);
    }

    private void processScoutBee(Bee bee) {
        if (onlookers.size() == 0) return; // якщо у вулику немає вільних робітників, немає сенсу шукати рішення
        int[] randomSolution = graph.randomPathNullIfNotAvailable(pathStart, pathEnd, null);
        bee.changePath(randomSolution, graph.measureDistance(randomSolution));
        scoutedPaths.put(bee.getCurrentPath(), bee.getCurrentPathDistance()); // бджола танцює до вулика про те, який шлях знайшла
    }

    private void processOnlookerBee(Bee bee, HashMap<Double, int[]> rollingWheel) {
        if (rand.nextDouble() < persuasionProbability) {
            int[] path = getPathFromWheel(rand.nextDouble(), rollingWheel);
            bee.changePath(path, scoutedPaths.get(path));
            bee.setCurrentStatus(Bee.Status.EMPLOYED);
            employed.add(bee);
        }
    }
    // обчислює потрапляння точки в відрізок на рулетці й отримує звідти відповідне рішення
    private int[] getPathFromWheel(double randomDouble, @NotNull HashMap<Double, int[]> rollingWheel) {
        int[] res = null;
        double[] wheelRange = concatTwoDoubleArrays(new Double[]{.0}, rollingWheel.keySet().toArray(new Double[0]));
        Arrays.sort(wheelRange);
        for (int i = 0; i < wheelRange.length - 1; ++i)
            if (randomDouble >= wheelRange[i] && randomDouble < wheelRange[i + 1])
                res = rollingWheel.get(wheelRange[i + 1]);
        if (res == null)
            res = rollingWheel.values().iterator().next();
        return res;
    }

    @Contract(pure = true)
    private double @NotNull [] concatTwoDoubleArrays(Double[] first, Double[] second) {
        int fLen = first == null ? 0 : first.length, sLen = second == null ? 0 : second.length, k = 0;
        double[] concat = new double[fLen + sLen];
        for (int i = 0; i < fLen; ++i) concat[k++] = first[i];
        for (int i = 0; i < sLen; ++i) concat[k++] = second[i];
        return concat;
    }

    @Override public String toString() {
        StringBuilder s = new StringBuilder(onlookers.size() + " onlookers, ");
        s.append(employed.size()).append(" employed, ").append(scouts.size()).append(" scouts, ")
        .append(scouts.size() + onlookers.size() + employed.size()).append(" total\n").append("Best path found: ");
        if (bestPath != null) {
            for (int i = 0; i < bestPath.length - 1; ++i)
                s.append(bestPath[i]).append("-");
            s.append(bestPath[bestPath.length - 1]).append("\n")
            .append("Path distance: ").append(bestDistance).append("\n");
        } else s.append("none");
        return s.toString();
    }
}
