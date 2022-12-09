package abc;

import java.util.*;

class AlgorithmImplementation {
    private final Random rand = new Random();

    EdgeList graph;
    int pathStart, pathEnd;

    int maxCycles; // за одну итерацию все агенты совершают по одному действию
    int reportEvery;

    public double PersuasionProbability; // вероятность бездействующей пчелы откликнуться на танец разведчика
    public double MistakeProbability; // вероятность пчелы-работника принять ошибочное решение

    int workerCount, scoutCount;

    ArrayList<Bee> scouts, onlookers, employed;

    HashMap<int[], Integer> scoutedPaths;    // решения, найденные разведчиками

    private int[] bestPath;
    private int bestDistance;

    public AlgorithmImplementation(int workerCount, int scoutCount, int maxNumberVisits,
                                   EdgeList graph, int pathStart, int pathEnd, int maxCycles, int reportEvery) {
        Bee.maxUnluckyItersCount = maxNumberVisits;
        this.maxCycles = maxCycles;
        this.reportEvery = reportEvery;
        this.graph = graph;
        this.pathStart = pathStart;
        this.pathEnd = pathEnd;
        this.scoutedPaths = new HashMap<>();
        PersuasionProbability = 0.95;
        MistakeProbability = 0.01;
        this.workerCount = workerCount;
        this.scoutCount = scoutCount;
    }

    public int getBeesCount() {
        return scouts.size() + onlookers.size() + employed.size();
    }

    @Override public String toString() {
        StringBuilder s = new StringBuilder(onlookers.size() + " onlookers, ");
        s.append(employed.size()).append(" employed, ");
        s.append(scouts.size()).append(" scouts, ");
        s.append(getBeesCount()).append(" total\n");
        s.append("Best path found: ");
        if (bestPath != null) {
            for (int i = 0; i < this.bestPath.length - 1; ++i)
                s.append(bestPath[i]).append("-");
            s.append(this.bestPath[this.bestPath.length - 1]).append("\n");
            s.append("Path distance: ");
            s.append(bestDistance).append("\n");
        }
        else s.append("none");
        return s.toString();
    }

    // создаёт начальную популяцию: разведчики + ожидающие в улье рабочие
    private void ProduceInitialPopulation() {
        employed = new ArrayList<>(workerCount);
        onlookers = new ArrayList<>(workerCount);
        for (int i = 0; i < workerCount; ++i)
            onlookers.add(new Bee(Bee.Status.ONLOOKER));
        scouts = new ArrayList<>(scoutCount);
        for (int i = 0; i < scoutCount; ++i)
            scouts.add(new Bee(Bee.Status.SCOUT));
    }

    public void solve() {
        ProduceInitialPopulation();
        long start = System.currentTimeMillis();
        for (int iteration = 0; iteration < maxCycles;) {
            for (int k = 0; k < reportEvery; ++k) {
                scoutPhase();
                OnlookerPhase();
                employedPhase();
                keepBestPath();
            }
            System.out.printf("Iteration #%d\n%s\n", iteration += reportEvery, this);
        }
        System.out.printf("Estimated time to solve - %8d seconds\n", (System.currentTimeMillis() - start) / 1000);
        System.out.printf("Finded best path: %s\nIs valid? - %b.", Arrays.toString(bestPath), graph.isValidPath(bestPath));
    }

    // если рабочим удалось найти лучшее решение, запоминает его
    private void keepBestPath() {
        for (Bee employ: employed) {
            if (bestPath == null || employ.getCurrentPathDistance() < bestDistance) {
                bestDistance = employ.getCurrentPathDistance();
                bestPath = employ.getCurrentPath();
            }
        }
    }

    private void employedPhase() {
        for (Bee employ: employed) processEmployedBee(employ);
        employed.removeIf(employ -> employ.getCurrentStatus() != Bee.Status.EMPLOYED);
    }

    private void processEmployedBee(Bee employedBee) {
        int[] neighborSolution = graph.modifyRandomPath(employedBee.getCurrentPath());
        int neighborDistance = graph.measureDistance(neighborSolution);

        boolean isMistaken = rand.nextDouble() < MistakeProbability;
        boolean foundNewSolution = neighborDistance < employedBee.getCurrentPathDistance();

        if (foundNewSolution ^ isMistaken) // XOR
            employedBee.changePath(neighborSolution, neighborDistance);
        else employedBee.stayIdle();

        // пчела-неудачник прекращает попытки улучшить путь
        if (employedBee.isUnluckyOverLimit()) {
            employedBee.setCurrentStatus(Bee.Status.ONLOOKER);
            employedBee.setUnluckyIterCount(0);
            onlookers.add(employedBee);
        }
    }

    private void OnlookerPhase() {
        HashMap<Double, int[]> rollingWheel = createScoutedPathsRollingWheel();
        for (Bee onlooker: onlookers)
            processOnlookerBee(onlooker, rollingWheel);
        onlookers.removeIf(onlooker -> onlooker.getCurrentStatus() != Bee.Status.ONLOOKER);
    }

    // строит рулетку решений разведчиков = проецирует каждое решение в отрезок внутри [0..1]
    // длины отрезков пропорциональны добротности значениям целевой функции
    private HashMap<Double, int[]> createScoutedPathsRollingWheel() {
        int distanceSum = 0;
        for (int[] path : scoutedPaths.keySet())
            distanceSum += scoutedPaths.get(path);

        HashMap<Double, int[]> res = new HashMap<>();
        double prevProb = 0.0;

        for (int[] path : scoutedPaths.keySet()) {
            double prob = 1.0 - scoutedPaths.get(path) / (double)distanceSum;
            res.put(prevProb + prob, path);
            prevProb += prob;
        }

        return res;
    }

    private void scoutPhase() {
        scoutedPaths.clear();
        for (Bee scout: scouts)
            processScoutBee(scout);
    }

    private void processScoutBee(Bee bee) {
        // если в улье нет свободных рабочих, нет смысла искать решение
        if (onlookers.size() == 0) return;
        int[] randomSolution = graph.RandomPath(pathStart, pathEnd, null);
        int solutionDistance = graph.measureDistance(randomSolution);
        bee.changePath(randomSolution, solutionDistance);
        // пчела танцует к улью о том, какой путь нашла
        DoWaggleDance(bee);
    }

    private void processOnlookerBee(Bee bee, HashMap<Double, int[]> rollingWheel) {
        boolean isPersuaded = rand.nextDouble() < PersuasionProbability;
        if (isPersuaded) {
            int[] path = getPathFromWheel(rand.nextDouble(), rollingWheel);
            bee.changePath(path, scoutedPaths.get(path));
            bee.setCurrentStatus(Bee.Status.EMPLOYED);
            employed.add(bee);
        }
    }
    // вычисляет попадание точки в отрезок на рулетке и получает оттуда соответствующее решение
    private int[] getPathFromWheel(double randomDouble, HashMap<Double, int[]> rollingWheel) {
        int[] res = null;
        double[] wheelRange = concatTwoDoubleArrays(new Double[]{.0}, rollingWheel.keySet().toArray(new Double[0]));
        Arrays.sort(wheelRange);
        for (int i = 0; i < wheelRange.length - 1; ++i)
            if (randomDouble >= wheelRange[i]
                    && randomDouble < wheelRange[i + 1])
                res = rollingWheel.get(wheelRange[i + 1]);
        if (res == null)
            res = rollingWheel.values().iterator().next();
        return res;
    }

    private void DoWaggleDance(Bee bee) {
        scoutedPaths.put(bee.getCurrentPath(), bee.getCurrentPathDistance());
    }

    private double[] concatTwoDoubleArrays(Double[] first, Double[] second) {
        int fLen = first == null ? 0 : first.length, sLen = second == null ? 0 : second.length, k = 0;
        double[] concat = new double[fLen + sLen];
        for (int i = 0; i < fLen; ++i) concat[k++] = first[i];
        for (int i = 0; i < sLen; ++i) concat[k++] = second[i];
        return concat;
    }
}
