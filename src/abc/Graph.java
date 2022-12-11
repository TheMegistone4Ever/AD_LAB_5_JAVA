package abc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Graph {
    public record Edge(int from, int to, int weight) {}
    private final Random rand = new Random(2022);

    private final int verticesCount;
    private final ArrayList<Edge> edges;

    public Graph(int verticesCount, int numEdges) {
        this.edges = new ArrayList<>(numEdges);
        this.verticesCount = verticesCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public Edge getEdgeOrNullIfNotFound(int from, int to) {
        for (Edge e: edges)
            if ((e.from() == from && e.to() == to) || (e.from() == to && e.to() == from))
                return e;
        return null;
    }

    public boolean noEdge(int from, int to) {
        return getEdgeOrNullIfNotFound(from, to) == null;
    }

    public void addEdge(@NotNull Edge edge) {
        if (noEdge(edge.from(), edge.to())) edges.add(edge);
    }

    public int getWeight(int from, int to) {
        Edge edge = getEdgeOrNullIfNotFound(from, to);
        return (getEdgeOrNullIfNotFound(from, to) == null) ? -1 : edge.weight();
    }

    public int[] getNeighbours(int vertex) {
        HashSet<Integer> neighbours = new HashSet<>();
        for (Edge e : edges) {
            if (e.from() == vertex) neighbours.add(e.to());
            if (e.to() == vertex) neighbours.add(e.from());
        }
        return neighbours.stream().mapToInt(Integer::intValue).toArray();
    }

    public int measureDistance(int @NotNull [] path) {
        int result = 0;
        for (int i = 0; i < path.length - 1; ++i) {
            int weight = getWeight(path[i], path[i + 1]);
            if (weight < 0) return -1;
            result += weight;
        }
        return result;
    }

    public int[] randomPathNullIfNotAvailable(int start, int dest, int[] visitedVertices) {
        HashSet<Integer> visited = (visitedVertices == null)
                ? new HashSet<>()
                : new HashSet<>(IntStream.of(visitedVertices).boxed().collect(Collectors.toSet()));
        visited.add(start);
        ArrayList<Integer> path = new ArrayList<>(List.of(start));
        int currentVertex = path.get(path.size() - 1);
        while (currentVertex != dest) {
            int[] neighs = except(getNeighbours(currentVertex), visited.stream().mapToInt(Integer::intValue).toArray());
            if (neighs.length == 0)
                if (path.size() == 1) return null;
                else path.remove(path.size() - 1);
            else path.add(neighs[rand.nextInt(neighs.length)]);
            currentVertex = path.get(path.size() - 1);
            visited.add(currentVertex);
        }
        return path.stream().mapToInt(Integer::valueOf).toArray();
    }

    public int[] modifyRandomPath(int @NotNull [] path) {
        int[] unchangedPathPart = null, changedPathPart = null;
        if (path.length > 2) while (changedPathPart == null) {
            int changeIndex = rand.nextInt(1, path.length - 1);
            System.arraycopy(path , 0 , unchangedPathPart = new int[changeIndex], 0, changeIndex);
            changedPathPart = randomPathNullIfNotAvailable(path[changeIndex], path[path.length - 1], unchangedPathPart);
        }
        return concatTwoIntArrays(unchangedPathPart, changedPathPart);
    }

    public boolean isValidPath(int @NotNull [] path) {
        for (int i = 0; i < path.length - 1; ++i)
            if (noEdge(path[i], path[i + 1])) return false;
        return true;
    }

    @Contract(pure = true)
    private int @NotNull [] concatTwoIntArrays(int[] first, int[] second) {
        int fLen = first == null ? 0 : first.length, sLen = second == null ? 0 : second.length, k = -1;
        int[] concat = new int[fLen + sLen];
        for (int i = 0; i < fLen; ++i) concat[++k] = first[i];
        for (int i = 0; i < sLen; ++i) concat[++k] = second[i];
        return concat;
    }

    private int[] except(int @NotNull [] first, int[] second) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int c : first) if (Arrays.stream(second).noneMatch(n -> n == c)) res.add(c);
        return res.stream().mapToInt(Integer::valueOf).toArray();
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(verticesCount + " vertices, " + getEdgeCount() + " edges:\n");
        int k = 0;
        for (Edge e: edges) {
            if (++k % 12 == 0) sb.append('\n');
            sb.append(e.from()).append('-').append(e.to()).append(":w").append(e.weight()).append(", ");
        }
        return sb.replace(sb.length() - 2, sb.length(), ";").toString();
    }
}
