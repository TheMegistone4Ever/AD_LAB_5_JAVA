package abc;

import java.util.Random;

public class UsualBeeColonyAlgorithmImplementation {
    final static int MAX_VERTEX = 300, MIN_W = 5, MAX_W = 150, TOTAL_BEES = 60, SCOUT_BEES = 10,
        MAX_VISITS = 3, MAX_ITERATIONS = 1000, FREQ_TO_REPORT = 20,
            pathStart = 0, pathEnd = MAX_VERTEX - 1;

    public static void main(String[] args) {
        EdgeList graph = loadGraph();
        System.out.println("Loaded graph:\n" + graph);
        AlgorithmImplementation ai = new AlgorithmImplementation(TOTAL_BEES - SCOUT_BEES, SCOUT_BEES, MAX_VISITS,
                graph, pathStart, pathEnd, MAX_ITERATIONS, FREQ_TO_REPORT);
        ai.solve();
    }

    private static EdgeList loadGraph() {
        Random rand = new Random();
        EdgeList result = new EdgeList(MAX_VERTEX, MAX_VERTEX * 15 * 2);
        for (int i = 0; i < MAX_VERTEX * 15; ++i) {
            int from = rand.nextInt(0, MAX_VERTEX);
            int to; do to = rand.nextInt(0, MAX_VERTEX); while (to == from);
            int weight = rand.nextInt(MIN_W, MAX_W);
            // реализация симметричной сети
            result.addEdge(new EdgeList.Edge(from, to, weight));
            result.addEdge(new EdgeList.Edge(to, from, weight));
        }
        return result;
    }
}
