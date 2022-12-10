package abc;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class UsualBeeColonyAlgorithmImplementation implements constants {
    public static void main(String[] args) {
        Graph graph = loadGraph();
        System.out.println("Loaded graph:\n" + graph);
        AlgorithmImplementation ai = new AlgorithmImplementation(TOTAL_BEES - SCOUT_BEES, SCOUT_BEES, MAX_VISITS,
                graph, pathStart, pathEnd, MAX_ITERATIONS, FREQ_TO_REPORT);
        ai.solve();
    }

    private static @NotNull Graph loadGraph() {
        Random rand = new Random();
        Graph result = new Graph(MAX_VERTEX, MAX_VERTEX * 15 << 1);
        for (int i = 0; i < MAX_VERTEX * 15; ++i) {
            int from = rand.nextInt(0, MAX_VERTEX);
            int to; do to = rand.nextInt(0, MAX_VERTEX); while (to == from);
            int weight = rand.nextInt(MIN_W, MAX_W);
            // реалізація симетричної мережі
            result.addEdge(new Graph.Edge(from, to, weight));
            result.addEdge(new Graph.Edge(to, from, weight));
        }
        return result;
    }
}
