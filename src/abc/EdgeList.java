package abc;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EdgeList {
    public record Edge(int from, int to, int weight) {}

    private final Random rand = new Random();
    private final int verticesCount;
    private final ArrayList<Edge> edges;

    public EdgeList(int verticesCount, int numEdges) {
        this.edges = new ArrayList<>(numEdges);
        this.verticesCount = verticesCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public Edge getEdge(int from, int to) {
        for (Edge e: edges)
            if ((e.from() == from && e.to() == to) || (e.from() == to && e.to() == from))
                return e;
        return null;
    }

    public boolean noEdge(int from, int to) {
        return getEdge(from, to) == null;
    }

    public void addEdge(Edge edge) {
        if (noEdge(edge.from(), edge.to())) edges.add(edge);
    }

    public int getWeight(int from, int to) {
        Edge edge = getEdge(from, to);
        return (getEdge(from, to) == null) ? -1 : edge.weight();
    }

    public int[] getNeighbours(int vertex) {
        HashSet<Integer> neighbours = new HashSet<>();
        for (Edge e : edges) {
            if (e.from() == vertex) neighbours.add(e.to());
            if (e.to() == vertex) neighbours.add(e.from());
        }
        return neighbours.stream().mapToInt(Integer::intValue).toArray();
    }

    public int measureDistance(int[] path) {
        int result = 0;
        for (int i = 0; i < path.length - 1; ++i) {
            int weight = getWeight(path[i], path[i + 1]);
            if (weight < 0) return -1;
            result += weight;
        }
        return result;
    }

    @Override public String toString() {
        StringBuilder s = new StringBuilder(verticesCount + " vertices, "
                + getEdgeCount() + " edges:\n");
        for (Edge e: edges)
            s.append(e.from()).append("-").append(e.to()).append(", weighted ").append(e.weight()).append("\n");
    return s.toString();
}

    public int[] RandomPath(int start, int dest, int[] visitedVertices) {
        HashSet<Integer> visited = (visitedVertices == null)
                ? new HashSet<>()
                : new HashSet<>(IntStream.of(visitedVertices).boxed().collect(Collectors.toSet()));
        visited.add(start);
        ArrayList<Integer> path = new ArrayList<>(List.of(start));
        int currentVertex = path.get(path.size() - 1);
        while (currentVertex != dest) {
            int[] neighs = except(getNeighbours(currentVertex), visited.stream().mapToInt(Integer::intValue).toArray());
            if (neighs.length == 0) {
                if (path.size() == 1) return null;
                path.remove(path.size() - 1);
            }
            else path.add(neighs[rand.nextInt(neighs.length)]); // add next vertex
            currentVertex = path.get(path.size() - 1);
            visited.add(currentVertex);
        }
        return path.stream().mapToInt(Integer::valueOf).toArray();
    }

    public int[] modifyRandomPath(int[] path) {
        int[] unchangedPathPart = null, changedPathPart = null;
        while (changedPathPart == null) {
            int changeIndex = rand.nextInt(1, path.length - 1);
            unchangedPathPart = new int[changeIndex];
            System.arraycopy(path , 0 , unchangedPathPart, 0, changeIndex);
            changedPathPart = RandomPath(path[changeIndex], path[path.length - 1], unchangedPathPart);
        }
        return concatTwoIntArrays(unchangedPathPart, changedPathPart);
    }

    public boolean isValidPath(int[] path) {
        for (int i = 0; i < path.length - 1; ++i)
            if (noEdge(path[i], path[i + 1])) return false;
        return true;
    }

    private int[] concatTwoIntArrays(int[] first, int[] second) {
        int fLen = first == null ? 0 : first.length, sLen = second == null ? 0 : second.length, k = 0;
        int[] concat = new int[fLen + sLen];
        for (int i = 0; i < fLen; ++i) concat[k++] = first[i];
        for (int i = 0; i < sLen; ++i) concat[k++] = second[i];
        return concat;
    }

    private int[] except(int[] first, int[] second) {
        ArrayList<Integer> res = new ArrayList<>();
        for (int c : first)
            if (!List.of(second).contains(c)) res.add(c);
        return res.stream().mapToInt(Integer::valueOf).toArray();
    }
}
