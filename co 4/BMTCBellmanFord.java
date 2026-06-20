// BMTC Bus Route Shortest Path using Bellman-Ford Algorithm
// Case Study: Shortest path from Majestic (MJC) to all BMTC bus hubs

public class BMTCBellmanFord {

    // Edge class to store source, destination, and weight (travel time in minutes)
    static class Edge {
        String src, dest;
        int weight;

        Edge(String src, String dest, int weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }

    // Hub/vertex names
    static String[] hubs = { "MJC", "KEM", "JAY", "KOR", "WHF", "HBR", "MRT" };

    // Number of vertices and edges
    static int V = 7;
    static int E = 11;

    // Bellman-Ford Algorithm
    static void bellmanFord(Edge[] edges, String source) {
        int[] dist = new int[V];
        final int INF = Integer.MAX_VALUE;

        // Step 1: Initialize all distances to infinity, source = 0
        for (int i = 0; i < V; i++) {
            dist[i] = INF;
        }
        dist[getIndex(source)] = 0;

        // Step 2: Relax all edges V-1 times
        for (int i = 1; i <= V - 1; i++) {
            for (Edge edge : edges) {
                int u = getIndex(edge.src);
                int v = getIndex(edge.dest);
                int w = edge.weight;

                if (dist[u] != INF && dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                }
            }
        }

        // Step 3: Negative cycle detection (one extra relaxation pass)
        for (Edge edge : edges) {
            int u = getIndex(edge.src);
            int v = getIndex(edge.dest);
            int w = edge.weight;

            if (dist[u] != INF && dist[u] + w < dist[v]) {
                System.out.println("Negative cycle detected!");
                return;
            }
        }

        // Step 4: Display shortest distances from source hub
        System.out.println("Shortest distance from " + source + ":");
        for (int i = 0; i < V; i++) {
            System.out.println(hubs[i] + " -> " + dist[i]);
        }
    }

    // Helper: Get index of a hub by name
    static int getIndex(String hub) {
        for (int i = 0; i < hubs.length; i++) {
            if (hubs[i].equals(hub)) return i;
        }
        return -1;
    }

    public static void main(String[] args) {

        // 11 directed edges representing BMTC bus routes with travel times (in minutes)
        // One negative edge: WHF -> MRT = -3 (express route / time saved during off-peak)
        Edge[] edges = new Edge[E];

        // Routes from MJC (Majestic)
        edges[0]  = new Edge("MJC", "KEM", 8);   // Majestic -> Kempegowda Bus Stand
        edges[1]  = new Edge("MJC", "JAY", 5);   // Majestic -> Jayanagar
        edges[2]  = new Edge("MJC", "KOR", 12);  // Majestic -> Koramangala

        // Routes from KEM (Kempegowda)
        edges[3]  = new Edge("KEM", "KOR", 4);   // Kempegowda -> Koramangala
        edges[4]  = new Edge("KEM", "HBR", 7);   // Kempegowda -> Hebbal

        // Routes from JAY (Jayanagar)
        edges[5]  = new Edge("JAY", "KOR", 4);   // Jayanagar -> Koramangala
        edges[6]  = new Edge("JAY", "WHF", 10);  // Jayanagar -> Whitefield

        // Routes from KOR (Koramangala)
        edges[7]  = new Edge("KOR", "WHF", 6);   // Koramangala -> Whitefield
        edges[8]  = new Edge("KOR", "MRT", 3);   // Koramangala -> Marathahalli

        // Routes from WHF (Whitefield)
        edges[9]  = new Edge("WHF", "MRT", -3);  // Whitefield -> Marathahalli (negative: express)
        edges[10] = new Edge("WHF", "HBR", 5);   // Whitefield -> Hebbal (surge penalty adjusted)

        // Run Bellman-Ford from source hub: MJC (Majestic)
        bellmanFord(edges, "MJC");
    }
}
