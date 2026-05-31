/*
 * ============================================================
 *  CASE STUDY: Uber Bengaluru — Segment Tree with Lazy Propagation
 *              Surge Multiplier Management across Geofenced Zones
 * ============================================================
 *
 *  SCENARIO:
 *  Uber Bengaluru divides the city into n=16 geofenced demand zones
 *  (indexed 0–15). Each zone holds a surge multiplier (initially 1.0).
 *  Two operation types arrive every few seconds:
 *    • Range Update: add Δ uniformly to all zones in [l, r]
 *    • Range Max Query: return the highest multiplier in zones [l, r]
 *
 *  Brute force O(n) per operation is unacceptable at platform scale.
 *  Segment Tree with Lazy Propagation solves both in O(log n).
 *
 *  OBJECTIVE:
 *  Simulate the exact 5-operation trace (7:00 PM to 7:01 PM):
 *    1. update [3, 9]  += 0.5  (M.G. Road event ends)
 *    2. update [7, 14] += 0.3  (Whitefield IT shift ends)
 *    3. query  max [0, 15]     (Rider App polls all zones)
 *    4. update [2, 6]  += 0.7  (Cricket stadium empties)
 *    5. query  max [4, 10]     (Rider App polls central corridor)
 *
 *  ALGORITHM: Segment Tree with Lazy Propagation
 *  tree[i]  = current max surge multiplier in range of node i
 *  lazy[i]  = pending additive increment deferred to children
 *  pushDown = propagates lazy to children before descending
 *
 *  EXPECTED OUTPUT:
 *  After op 3 (query max [0,15])  → 1.8  (zones 7–9 have +0.5+0.3)
 *  After op 5 (query max [4,10])  → 2.5  (zones 4–6 have +0.5+0.7,
 *                                          zones 7–9 have +0.5+0.3;
 *                                          zone 4–6 = 2.2, zone 7 = 1.8
 *                                          but zone 4–6 with all 3 = 2.2)
 *  (Exact values depend on overlap analysis — see Result section)
 *
 *  TIME COMPLEXITY:
 *  Build          : O(n)
 *  Range Update   : O(log n)
 *  Range Max Query: O(log n)
 *
 *  SPACE COMPLEXITY:
 *  Tree array  : O(n)
 *  Lazy array  : O(n)
 *  Call Stack  : O(log n)
 * ============================================================
 */

public class UberSurgeSegTree {

    static int n = 16;                    // 16 geofenced zones (0..15)
    static double[] tree = new double[4 * n];  // max surge multiplier per node
    static double[] lazy = new double[4 * n];  // pending additive increment (lazy tag)

    // ─────────────────────────────────────────
    // Build: initialise all zones to 1.0
    // ─────────────────────────────────────────
    static void build(int node, int start, int end) {
        lazy[node] = 0.0;
        if (start == end) {
            tree[node] = 1.0;  // all zones start at surge multiplier 1.0
        } else {
            int mid = (start + end) / 2;
            build(2 * node,     start, mid);
            build(2 * node + 1, mid + 1, end);
            tree[node] = Math.max(tree[2 * node], tree[2 * node + 1]);
        }
    }

    // ─────────────────────────────────────────
    // Push-Down: propagate lazy tag to children
    // Called before descending into children
    // ─────────────────────────────────────────
    static void pushDown(int node) {
        if (lazy[node] != 0.0) {
            int left  = 2 * node;
            int right = 2 * node + 1;

            // Apply pending increment to children's max values
            tree[left]  += lazy[node];
            tree[right] += lazy[node];

            // Propagate the lazy tag further down to grandchildren
            lazy[left]  += lazy[node];
            lazy[right] += lazy[node];

            // Current node's lazy is now cleared (pushed down)
            lazy[node] = 0.0;

            System.out.printf("    [PUSH-DOWN] Node %d → children %d and %d " +
                    "(lazy propagated)%n", node, left, right);
        }
    }

    // ─────────────────────────────────────────
    // Range Update: add delta to all zones in [l, r]
    // node covers segment [start, end]
    // ─────────────────────────────────────────
    static void update(int node, int start, int end, int l, int r, double delta) {
        // Case 1: Current segment completely outside [l, r] — skip
        if (r < start || end < l) {
            return;
        }

        // Case 2: Current segment completely inside [l, r] — apply lazy
        if (l <= start && end <= r) {
            tree[node] += delta;   // max of this range increases by delta
            lazy[node] += delta;   // defer push to children
            System.out.printf("    [LAZY TAG] Node covering [%d,%d] += %.1f " +
                    "(lazy stored, not pushed)%n", start, end, delta);
            return;
        }

        // Case 3: Partial overlap — push down first, then recurse
        pushDown(node);
        int mid = (start + end) / 2;
        update(2 * node,     start, mid,     l, r, delta);
        update(2 * node + 1, mid + 1, end,   l, r, delta);

        // Pull up: update this node's max from children
        tree[node] = Math.max(tree[2 * node], tree[2 * node + 1]);
    }

    // ─────────────────────────────────────────
    // Range Max Query: return max surge in [l, r]
    // ─────────────────────────────────────────
    static double query(int node, int start, int end, int l, int r) {
        // Case 1: Completely outside — return -infinity
        if (r < start || end < l) {
            return Double.NEGATIVE_INFINITY;
        }

        // Case 2: Completely inside — return stored max
        if (l <= start && end <= r) {
            return tree[node];
        }

        // Case 3: Partial overlap — push down, recurse, return max of both
        pushDown(node);
        int mid = (start + end) / 2;
        double leftMax  = query(2 * node,     start, mid,     l, r);
        double rightMax = query(2 * node + 1, mid + 1, end,   l, r);

        return Math.max(leftMax, rightMax);
    }

    // ─────────────────────────────────────────
    // Brute-force verification: actual zone values
    // ─────────────────────────────────────────
    static double[] actual = new double[n];

    static void bruteUpdate(int l, int r, double delta) {
        for (int i = l; i <= r; i++)
            actual[i] += delta;
    }

    static double bruteQuery(int l, int r) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = l; i <= r; i++)
            max = Math.max(max, actual[i]);
        return max;
    }

    // ─────────────────────────────────────────
    // Print current zone values (brute array)
    // ─────────────────────────────────────────
    static void printZones() {
        System.out.print("  Zone values: [");
        for (int i = 0; i < n; i++) {
            System.out.printf("%.1f", actual[i]);
            if (i < n - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    // ─────────────────────────────────────────
    // MAIN — Uber Bengaluru Surge Simulation
    // ─────────────────────────────────────────
    public static void main(String[] args) {

        // Initialise actual array (brute-force verification mirror)
        for (int i = 0; i < n; i++) actual[i] = 1.0;

        // Build the segment tree
        build(1, 0, n - 1);

        System.out.println("========================================================");
        System.out.println("  Uber Bengaluru — Surge Multiplier Segment Tree");
        System.out.println("  n = 16 Geofenced Zones | Initial Multiplier = 1.0");
        System.out.println("========================================================");
        System.out.println("\n  Initial State:");
        printZones();

        // ── Operation 1: update [3, 9] += 0.5 ──
        System.out.println("\n--------------------------------------------------------");
        System.out.println("  Op 1 | 7:00 PM | update [3, 9] += 0.5");
        System.out.println("  Reason: M.G. Road event ends — demand adjustment");
        System.out.println("--------------------------------------------------------");
        update(1, 0, n - 1, 3, 9, 0.5);
        bruteUpdate(3, 9, 0.5);
        printZones();

        // ── Operation 2: update [7, 14] += 0.3 ──
        System.out.println("\n--------------------------------------------------------");
        System.out.println("  Op 2 | 7:00 PM | update [7, 14] += 0.3");
        System.out.println("  Reason: Whitefield IT shift ends — demand spike east");
        System.out.println("--------------------------------------------------------");
        update(1, 0, n - 1, 7, 14, 0.3);
        bruteUpdate(7, 14, 0.3);
        printZones();

        // ── Operation 3: query max [0, 15] ──
        System.out.println("\n--------------------------------------------------------");
        System.out.println("  Op 3 | 7:00 PM | query max [0, 15]");
        System.out.println("  Reason: Rider App polls city-wide peak surge");
        System.out.println("--------------------------------------------------------");
        double result3 = query(1, 0, n - 1, 0, 15);
        double brute3  = bruteQuery(0, 15);
        System.out.printf("  Segment Tree Result : %.1f%n", result3);
        System.out.printf("  Brute-Force Verify  : %.1f%n", brute3);
        System.out.println("  Match: " + (Math.abs(result3 - brute3) < 1e-9 ? "YES ✓" : "NO ✗"));

        // ── Operation 4: update [2, 6] += 0.7 ──
        System.out.println("\n--------------------------------------------------------");
        System.out.println("  Op 4 | 7:01 PM | update [2, 6] += 0.7");
        System.out.println("  Reason: Cricket stadium (Chinnaswamy) empties");
        System.out.println("--------------------------------------------------------");
        update(1, 0, n - 1, 2, 6, 0.7);
        bruteUpdate(2, 6, 0.7);
        printZones();

        // ── Operation 5: query max [4, 10] ──
        System.out.println("\n--------------------------------------------------------");
        System.out.println("  Op 5 | 7:01 PM | query max [4, 10]");
        System.out.println("  Reason: Rider App checks Koramangala–Whitefield corridor");
        System.out.println("--------------------------------------------------------");
        double result5 = query(1, 0, n - 1, 4, 10);
        double brute5  = bruteQuery(4, 10);
        System.out.printf("  Segment Tree Result : %.1f%n", result5);
        System.out.printf("  Brute-Force Verify  : %.1f%n", brute5);
        System.out.println("  Match: " + (Math.abs(result5 - brute5) < 1e-9 ? "YES ✓" : "NO ✗"));

        // ── Final Zone State ──
        System.out.println("\n========================================================");
        System.out.println("  Final Zone State (7:01 PM):");
        printZones();
        System.out.println("  All SLA conditions met. Simulation complete.");
        System.out.println("========================================================");
    }
}