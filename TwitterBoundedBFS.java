/*
 * ============================================================
 *  CASE STUDY: X (Twitter) — Bounded BFS Tweet Reach Prediction
 * ============================================================
 *
 *  SCENARIO:
 *  When a tweet is posted, X must predict its 10-minute reach for
 *  ad-pricing and trending-topic detection. The prediction uses
 *  bounded BFS on the directed follower graph: traverse from the
 *  author outward via "is followed by" edges up to depth 3.
 *  Deeper hops have negligible retweet probability and are ignored.
 *
 *  PROBLEM:
 *  Real follower graphs have overlapping audiences — many users are
 *  reachable via multiple independent paths. Naive BFS without a
 *  visited set double-counts these users, inflating the reach estimate.
 *
 *  OBJECTIVE:
 *  1. Build the 9-node directed follower-graph subgraph.
 *  2. Run BFS from source A with depth limit = 3.
 *  3. Use a visited set to deduplicate multi-path reachable nodes.
 *  4. Report unique reach count and traversal order.
 *  5. Identify overlap nodes that would be double-counted naively.
 *  6. Validate against 500 ms SLA budget.
 *
 *  GRAPH EDGES (X → Y means Y follows X; tweet flows from X to Y):
 *  A→B, A→C, B→D, B→E, C→E, C→F, D→G, E→G, E→H, F→H, F→I
 *
 *  EXPECTED OUTPUT:
 *  BFS Order (depth): A(0), B(1), C(1), D(2), E(2), F(2), G(3), H(3), I(3)
 *  Unique Reach     : 8 users (B, C, D, E, F, G, H, I)
 *  Overlap Nodes    : E (via B and C), G (via D and E), H (via E and F)
 *
 *  ALGORITHM: BFS with depth bound and HashSet visited set
 *
 *  TIME COMPLEXITY:
 *  BFS traversal : O(V + E) — each node and edge visited at most once
 *  Visited lookup: O(1) average — HashSet
 *
 *  SPACE COMPLEXITY:
 *  Graph         : O(V + E)
 *  Visited set   : O(V)
 *  BFS Queue     : O(V) worst case
 * ============================================================
 */

import java.util.*;

public class TwitterBoundedBFS {

    // ─────────────────────────────────────────
    // Graph: directed adjacency list
    // Edge X → Y means "X is followed by Y"
    // (tweet posted by X is visible to Y)
    // ─────────────────────────────────────────
    static Map<String, List<String>> graph = new LinkedHashMap<>();

    static void addEdge(String from, String to) {
        graph.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        // Ensure destination node exists in graph even if it has no outgoing edges
        graph.computeIfAbsent(to, k -> new ArrayList<>());
    }

    // ─────────────────────────────────────────
    // BFS Node Entry: stores node + its depth
    // ─────────────────────────────────────────
    static class BFSEntry {
        String node;
        int depth;

        BFSEntry(String node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }

    // ─────────────────────────────────────────
    // Bounded BFS with visited-set deduplication
    // source : starting node (tweet author)
    // depthLimit : maximum BFS depth (= 3 for Twitter)
    // ─────────────────────────────────────────
    static int boundedBFS(String source, int depthLimit) {

        Set<String> visited = new LinkedHashSet<>(); // preserves insertion order for reporting
        Queue<BFSEntry> queue = new LinkedList<>();

        // Track all discovery attempts per node (for overlap audit)
        Map<String, List<String>> discoveryPaths = new LinkedHashMap<>();

        // ── Initialise ──
        visited.add(source);
        queue.add(new BFSEntry(source, 0));

        System.out.println("\n  BFS Traversal Log:");
        System.out.println("  ──────────────────────────────────────────────────");
        System.out.printf("  %-8s %-8s %-12s %-10s%n",
                "Node", "Depth", "Discovered Via", "Action");
        System.out.println("  ──────────────────────────────────────────────────");
        System.out.printf("  %-8s %-8s %-12s %-10s%n",
                source, 0, "SOURCE", "ENQUEUED");

        // ── BFS Main Loop ──
        while (!queue.isEmpty()) {
            BFSEntry curr = queue.poll();
            String node = curr.node;
            int depth = curr.depth;

            // Depth limit: do not explore neighbours beyond limit
            if (depth >= depthLimit) {
                continue;
            }

            List<String> neighbours = graph.getOrDefault(node, new ArrayList<>());

            for (String neighbour : neighbours) {

                // Record this discovery attempt
                discoveryPaths.computeIfAbsent(neighbour, k -> new ArrayList<>()).add(node);

                if (!visited.contains(neighbour)) {
                    // First time seeing this node — enqueue it
                    visited.add(neighbour);
                    queue.add(new BFSEntry(neighbour, depth + 1));

                    System.out.printf("  %-8s %-8s %-12s %-10s%n",
                            neighbour, depth + 1, node, "ENQUEUED");
                } else {
                    // Already visited — skip (deduplication in action)
                    System.out.printf("  %-8s %-8s %-12s %-10s%n",
                            neighbour, depth + 1, node,
                            "SKIPPED (already visited — overlap prevented)");
                }
            }
        }

        System.out.println("  ──────────────────────────────────────────────────");

        // ── Overlap Audit ──
        System.out.println("\n  Overlap Audit (nodes reachable via multiple paths):");
        System.out.println("  ──────────────────────────────────────────────────");
        boolean anyOverlap = false;
        for (Map.Entry<String, List<String>> entry : discoveryPaths.entrySet()) {
            if (entry.getValue().size() > 1) {
                anyOverlap = true;
                System.out.printf("  Node %-4s discovered via: %s%n",
                        entry.getKey(), entry.getValue());
                System.out.println("         → Without visited set: counted "
                        + entry.getValue().size() + "× | With visited set: counted 1×");
            }
        }
        if (!anyOverlap)
            System.out.println("  No overlapping paths detected.");

        // ── Reach Computation ──
        // Subtract 1 to exclude the source node (author) from reach count
        int reach = visited.size() - 1;

        System.out.println("\n  Visited Set (in discovery order): " + visited);
        System.out.println("  Total nodes in visited set : " + visited.size());
        System.out.println("  Unique Reach (excl. author): " + reach + " users");

        return reach;
    }

    // ─────────────────────────────────────────
    // Naive BFS (NO visited set) — for contrast
    // Shows inflated count without deduplication
    // ─────────────────────────────────────────
    static int naiveBFS(String source, int depthLimit) {
        Queue<BFSEntry> queue = new LinkedList<>();
        queue.add(new BFSEntry(source, 0));
        int count = 0; // count everyone dequeued except source

        while (!queue.isEmpty()) {
            BFSEntry curr = queue.poll();
            if (!curr.node.equals(source))
                count++;
            if (curr.depth >= depthLimit)
                continue;

            for (String neighbour : graph.getOrDefault(curr.node, new ArrayList<>())) {
                queue.add(new BFSEntry(neighbour, curr.depth + 1));
                // No visited check — every path counts separately
            }
        }
        return count;
    }

    // ─────────────────────────────────────────
    // MAIN — Twitter Reach Prediction Simulation
    // ─────────────────────────────────────────
    public static void main(String[] args) {

        System.out.println("========================================================");
        System.out.println("  X (Twitter) — Bounded BFS Tweet Reach Predictor");
        System.out.println("  Author: User A | Depth Limit: 3 | SLA: 500 ms p99");
        System.out.println("========================================================");

        // ── Build the follower graph ──
        // Edge A→B means B follows A (tweet from A is visible to B)
        addEdge("A", "B");
        addEdge("A", "C");
        addEdge("B", "D");
        addEdge("B", "E");
        addEdge("C", "E"); // E reachable via both B and C → overlap!
        addEdge("C", "F");
        addEdge("D", "G");
        addEdge("E", "G"); // G reachable via both D and E → overlap!
        addEdge("E", "H");
        addEdge("F", "H"); // H reachable via both E and F → overlap!
        addEdge("F", "I");

        System.out.println("\n  Graph Adjacency List:");
        for (Map.Entry<String, List<String>> entry : graph.entrySet()) {
            if (!entry.getValue().isEmpty())
                System.out.println("  " + entry.getKey() + " → " + entry.getValue());
        }

        // ── Run Bounded BFS with visited set ──
        System.out.println("\n========================================================");
        System.out.println("  Phase 1: Bounded BFS (depth ≤ 3) WITH Visited Set");
        System.out.println("========================================================");

        long startTime = System.nanoTime();
        int correctReach = boundedBFS("A", 3);
        long endTime = System.nanoTime();

        long elapsedMs = (endTime - startTime) / 1_000_000;

        System.out.println("\n  ── Reach Prediction Result ──");
        System.out.println("  Predicted 10-min reach : " + correctReach + " unique users");
        System.out.println("  Execution time         : " + elapsedMs + " ms");
        System.out.println("  SLA (≤ 500 ms)         : "
                + (elapsedMs <= 500 ? "PASSED ✓" : "FAILED ✗"));

        // ── Run Naive BFS (no visited set) for comparison ──
        System.out.println("\n========================================================");
        System.out.println("  Phase 2: Naive BFS (depth ≤ 3) WITHOUT Visited Set");
        System.out.println("  (Contrast — shows inflation from double-counting)");
        System.out.println("========================================================");

        int naiveReach = naiveBFS("A", 3);
        System.out.println("  Naive BFS reach count  : " + naiveReach + " (INFLATED)");
        System.out.println("  Correct BFS reach count: " + correctReach);
        System.out.println("  Overcounting by        : " + (naiveReach - correctReach)
                + " phantom users");

        // ── Downstream service output ──
        System.out.println("\n========================================================");
        System.out.println("  Phase 3: Downstream Service Outputs");
        System.out.println("========================================================");
        System.out.println("  Ad-Pricing Engine     → Predicted Impressions: "
                + correctReach + " × avg CTR");
        System.out.println("  Trending Topic Score  → Reach weight: "
                + correctReach + " nodes × retweet probability");
        System.out.println("  Content Amplification → Depth-3 reachable: "
                + correctReach + " unique accounts");
        System.out.println("\n  Simulation complete. BFS Reach Prediction delivered.");
        System.out.println("========================================================");
    }
}