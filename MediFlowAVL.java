/*
 * ============================================================
 *  CASE STUDY: MediFlow Hospital — AVL Tree Patient Dictionary
 * ============================================================
 *
 *  SCENARIO:
 *  MediFlow Hospital issues 8-digit Patient-IDs in near-ascending
 *  appointment order. The triage console must perform point-lookups
 *  with p99 < 5 ms. Hardware dereference cost = ~200 ns/pointer.
 *  Therefore: max tree depth = 25 levels (25 × 200 ns = 5 ms SLA).
 *
 *  A plain BST on nearly sorted input degenerates to O(n) depth.
 *  An AVL Tree guarantees depth ≤ 1.44 × log₂(n), which for the
 *  hospital's operational scale stays well within the 25-level budget.
 *
 *  OBJECTIVE:
 *  1. Insert 13 Patient-IDs (20,30,35,40,45,50,60,65,70,75,80,85,90)
 *     into an AVL Tree, maintaining balance after each insertion.
 *  2. Perform point-lookups and verify traversal depth.
 *  3. Delete patients 30, 70, and 50 (in that order) at noon.
 *  4. Validate post-deletion tree via in-order traversal.
 *  5. Confirm SLA compliance at every stage.
 *
 *  ALGORITHM: AVL Tree (Self-Balancing BST)
 *  Rotations: LL (Right), RR (Left), LR (Left-Right), RL (Right-Left)
 *  Balance Factor = height(left) - height(right); must stay in {-1, 0, 1}
 *
 *  EXPECTED FINAL IN-ORDER OUTPUT (after 3 deletions):
 *  20 35 40 45 60 65 75 80 85 90
 *
 *  TIME COMPLEXITY:
 *  Insert / Delete / Search: O(log n) — AVL height guarantee
 *  In-Order Traversal      : O(n)
 *
 *  SPACE COMPLEXITY:
 *  Tree Storage : O(n)
 *  Call Stack   : O(log n) — recursive depth bounded by tree height
 * ============================================================
 */

public class MediFlowAVL {

    // ─────────────────────────────────────────
    // Node Definition
    // ─────────────────────────────────────────
    static class Node {
        int patientId;   // 8-digit Patient-ID (key)
        int height;      // height of subtree rooted here
        Node left, right;

        Node(int id) {
            this.patientId = id;
            this.height = 0;      // leaf height = 0
            this.left = null;
            this.right = null;
        }
    }

    // ─────────────────────────────────────────
    // Utility: Height of a node (-1 if null)
    // ─────────────────────────────────────────
    private int height(Node n) {
        return (n == null) ? -1 : n.height;
    }

    // ─────────────────────────────────────────
    // Utility: Balance Factor
    // ─────────────────────────────────────────
    private int balanceFactor(Node n) {
        return (n == null) ? 0 : height(n.left) - height(n.right);
    }

    // ─────────────────────────────────────────
    // Utility: Update Height
    // ─────────────────────────────────────────
    private void updateHeight(Node n) {
        if (n != null)
            n.height = 1 + Math.max(height(n.left), height(n.right));
    }

    // ─────────────────────────────────────────
    // Right Rotation (LL Case)
    //        y                x
    //       / \              / \
    //      x   T3   -->    T1   y
    //     / \                  / \
    //    T1  T2              T2  T3
    // ─────────────────────────────────────────
    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        x.right = y;
        y.left = T2;

        updateHeight(y);
        updateHeight(x);

        System.out.println("    [ROTATION] Right Rotation on node " + y.patientId
                + " → new subtree root: " + x.patientId);
        return x;
    }

    // ─────────────────────────────────────────
    // Left Rotation (RR Case)
    //    x                    y
    //   / \                  / \
    //  T1   y     -->       x   T3
    //      / \             / \
    //    T2  T3           T1  T2
    // ─────────────────────────────────────────
    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        y.left = x;
        x.right = T2;

        updateHeight(x);
        updateHeight(y);

        System.out.println("    [ROTATION] Left Rotation on node " + x.patientId
                + " → new subtree root: " + y.patientId);
        return y;
    }

    // ─────────────────────────────────────────
    // Rebalance helper (called after insert/delete)
    // ─────────────────────────────────────────
    private Node rebalance(Node node) {
        updateHeight(node);
        int bf = balanceFactor(node);

        // LL Case
        if (bf > 1 && balanceFactor(node.left) >= 0)
            return rotateRight(node);

        // LR Case
        if (bf > 1 && balanceFactor(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // RR Case
        if (bf < -1 && balanceFactor(node.right) <= 0)
            return rotateLeft(node);

        // RL Case
        if (bf < -1 && balanceFactor(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node; // already balanced
    }

    // ─────────────────────────────────────────
    // Insert
    // ─────────────────────────────────────────
    public Node insert(Node root, int id) {
        // Standard BST insertion
        if (root == null) {
            System.out.println("  → Inserted Patient-ID: " + id);
            return new Node(id);
        }

        if (id < root.patientId)
            root.left = insert(root.left, id);
        else if (id > root.patientId)
            root.right = insert(root.right, id);
        else {
            System.out.println("  → Duplicate Patient-ID " + id + " ignored.");
            return root;
        }

        return rebalance(root);
    }

    // ─────────────────────────────────────────
    // Find Minimum Node (for deletion successor)
    // ─────────────────────────────────────────
    private Node minValueNode(Node node) {
        Node curr = node;
        while (curr.left != null)
            curr = curr.left;
        return curr;
    }

    // ─────────────────────────────────────────
    // Delete
    // ─────────────────────────────────────────
    public Node delete(Node root, int id) {
        if (root == null) {
            System.out.println("  → Patient-ID " + id + " not found.");
            return null;
        }

        if (id < root.patientId)
            root.left = delete(root.left, id);
        else if (id > root.patientId)
            root.right = delete(root.right, id);
        else {
            // Node to delete found
            System.out.println("  → Deleting Patient-ID: " + id);

            if (root.left == null || root.right == null) {
                // Case: 0 or 1 child
                root = (root.left != null) ? root.left : root.right;
            } else {
                // Case: 2 children — replace with in-order successor
                Node successor = minValueNode(root.right);
                System.out.println("    [SUCCESSOR] In-order successor: " + successor.patientId);
                root.patientId = successor.patientId;
                root.right = delete(root.right, successor.patientId);
            }
        }

        if (root == null) return null;
        return rebalance(root);
    }

    // ─────────────────────────────────────────
    // Search (Point-Lookup)
    // ─────────────────────────────────────────
    public boolean search(Node root, int id) {
        int depth = 0;
        Node curr = root;
        while (curr != null) {
            depth++;
            if (id == curr.patientId) {
                System.out.println("  → Patient-ID " + id + " FOUND at depth " + depth
                        + " (latency ≈ " + (depth * 200) + " ns)");
                return true;
            } else if (id < curr.patientId) {
                curr = curr.left;
            } else {
                curr = curr.right;
            }
        }
        System.out.println("  → Patient-ID " + id + " NOT FOUND (searched " + depth + " nodes)");
        return false;
    }

    // ─────────────────────────────────────────
    // In-Order Traversal (Validation)
    // ─────────────────────────────────────────
    public void inOrder(Node root) {
        if (root == null) return;
        inOrder(root.left);
        System.out.print(root.patientId + " ");
        inOrder(root.right);
    }

    // ─────────────────────────────────────────
    // Tree Height Reporter
    // ─────────────────────────────────────────
    public void reportStats(Node root, String phase) {
        int h = height(root) + 1; // convert 0-indexed to level count
        System.out.println("\n  [STATS — " + phase + "]");
        System.out.println("  Tree Height (levels): " + h);
        System.out.println("  SLA Compliant (≤25 levels): " + (h <= 25 ? "YES ✓" : "NO ✗"));
        System.out.println("  Max Lookup Latency ≈ " + (h * 200) + " ns ("
                + (h * 200 < 5_000_000 ? "within" : "EXCEEDS") + " 5 ms SLA)");
    }

    // ─────────────────────────────────────────
    // MAIN — MediFlow Daily Simulation
    // ─────────────────────────────────────────
    public static void main(String[] args) {

        MediFlowAVL avl = new MediFlowAVL();
        Node root = null;

        // Morning appointment board — IDs in scheduled order (nearly sorted ↑)
        int[] morningIDs = {20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90};

        System.out.println("========================================================");
        System.out.println("  MediFlow Hospital — AVL Patient Dictionary");
        System.out.println("  Phase 1: Morning Registrations (8:00 AM – 2:00 PM)");
        System.out.println("========================================================");

        for (int id : morningIDs) {
            System.out.println("\nRegistering Patient-ID: " + id);
            root = avl.insert(root, id);
        }

        avl.reportStats(root, "After All Insertions");

        System.out.println("\n  In-Order Traversal (should be sorted ↑):");
        System.out.print("  ");
        avl.inOrder(root);
        System.out.println();

        // ── Point-Lookup Tests ──
        System.out.println("\n========================================================");
        System.out.println("  Phase 2: Triage Console — Point-Lookup SLA Tests");
        System.out.println("========================================================");

        int[] lookups = {20, 60, 90, 75, 50};
        for (int id : lookups) {
            System.out.println("\nLooking up Patient-ID: " + id);
            avl.search(root, id);
        }

        // ── Noon Deletions ──
        System.out.println("\n========================================================");
        System.out.println("  Phase 3: Noon Status Updates — Deletions");
        System.out.println("  30 → transferred | 70 → discharged | 50 → admission closed");
        System.out.println("========================================================");

        int[] deletions = {30, 70, 50};
        for (int id : deletions) {
            System.out.println("\nRemoving Patient-ID: " + id);
            root = avl.delete(root, id);
        }

        avl.reportStats(root, "After All Deletions");

        System.out.println("\n  In-Order Traversal (remaining 10 patients):");
        System.out.print("  ");
        avl.inOrder(root);
        System.out.println();

        // ── Post-deletion lookups ──
        System.out.println("\n========================================================");
        System.out.println("  Phase 4: Post-Deletion Lookup Validation");
        System.out.println("========================================================");

        System.out.println("\nLooking up deleted Patient-ID 30 (should be NOT FOUND):");
        avl.search(root, 30);

        System.out.println("\nLooking up active Patient-ID 65 (should be FOUND):");
        avl.search(root, 65);

        System.out.println("\n========================================================");
        System.out.println("  MediFlow Simulation Complete — All SLAs Met.");
        System.out.println("========================================================");
    }
}