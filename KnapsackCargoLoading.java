import java.util.*;

/**
 * Case Study : Bangalore-to-Mumbai Logistics
 * Algorithm  : 0/1 Knapsack - Dynamic Programming
 * Problem    : Select cargo consignments that maximise total payment
 *              without exceeding the truck capacity of 24 tons.
 */
public class CargoKnapsack {

    // ---------------------------------------------------------------
    // Cargo data  (8 consignments - A through H)
    // ---------------------------------------------------------------
    static final String[] NAMES   = { "A",  "B",  "C",  "D",  "E",  "F",  "G",  "H" };
    static final int[]    WEIGHTS = {  6,    8,    5,    4,    3,    7,    9,    6  }; // tons
    static final int[]    VALUES  = { 50,   30,   45,   35,   25,   20,   40,   20  }; // Rs. Thousand
    static final int      CAPACITY = 24; // tons
    static final int      N        = NAMES.length;

    // ---------------------------------------------------------------
    // Build the DP table  -  O(N x W)
    // ---------------------------------------------------------------
    static int[][] buildDP() {
        int[][] dp = new int[N + 1][CAPACITY + 1];

        for (int i = 1; i <= N; i++) {
            int w = WEIGHTS[i - 1];
            int v = VALUES[i - 1];
            for (int cap = 0; cap <= CAPACITY; cap++) {
                // Do not take item i
                dp[i][cap] = dp[i - 1][cap];
                // Take item i (only if it fits)
                if (w <= cap) {
                    dp[i][cap] = Math.max(dp[i][cap], dp[i - 1][cap - w] + v);
                }
            }
        }
        return dp;
    }

    // ---------------------------------------------------------------
    // Backtrack to recover selected items  -  O(N)
    // ---------------------------------------------------------------
    static List<Integer> backtrack(int[][] dp) {
        List<Integer> selected = new ArrayList<>();
        int cap = CAPACITY;
        for (int i = N; i >= 1; i--) {
            if (dp[i][cap] != dp[i - 1][cap]) {
                selected.add(i - 1); // 0-based index
                cap -= WEIGHTS[i - 1];
            }
        }
        Collections.reverse(selected);
        return selected;
    }

    // ---------------------------------------------------------------
    // Print the DP table
    // ---------------------------------------------------------------
    static void printDPTable(int[][] dp) {
        System.out.println("\n===== Dynamic Programming Table (dp[item][capacity]) =====");
        System.out.printf("%-6s", "Item");
        for (int c = 0; c <= CAPACITY; c++) {
            System.out.printf("%4d", c);
        }
        System.out.println();

        String line = "";
        for (int k = 0; k < 6 + 4 * (CAPACITY + 1); k++) {
            line += "-";
        }
        System.out.println(line);

        for (int i = 0; i <= N; i++) {
            if (i == 0) {
                System.out.printf("%-6s", "0");
            } else {
                System.out.printf("%-6s", NAMES[i - 1]);
            }
            for (int c = 0; c <= CAPACITY; c++) {
                System.out.printf("%4d", dp[i][c]);
            }
            System.out.println();
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {

        System.out.println("==========================================================");
        System.out.println("  BANGALORE-TO-MUMBAI LOGISTICS - CARGO LOADING SYSTEM    ");
        System.out.println("       Algorithm: 0/1 Knapsack (Dynamic Programming)      ");
        System.out.println("==========================================================");

        // Cargo manifest
        System.out.println("\n----- Cargo Consignment Details -----");
        System.out.printf("%-14s %-15s %-22s%n",
                "Consignment", "Weight (tons)", "Payment (Rs. Thousand)");
        System.out.println("--------------------------------------------------");
        for (int i = 0; i < N; i++) {
            System.out.printf("%-14s %-15d %-22d%n", NAMES[i], WEIGHTS[i], VALUES[i]);
        }
        System.out.println("\nTruck Capacity : " + CAPACITY + " tons");

        // DP computation
        int[][] dp = buildDP();
        printDPTable(dp);

        // Optimal result
        int maxValue = dp[N][CAPACITY];
        List<Integer> selected = backtrack(dp);

        int totalWeight = 0;
        List<String> selectedNames = new ArrayList<>();
        for (int idx : selected) {
            totalWeight += WEIGHTS[idx];
            selectedNames.add(NAMES[idx]);
        }

        System.out.println("\n===== Optimal Cargo Selection =====");
        System.out.println("Maximum Value     = Rs." + maxValue + " Thousand");
        System.out.println("Selected Items    : " + selectedNames);
        System.out.println("Total Weight      = " + totalWeight + " tons");
        System.out.println("Total Value       = Rs." + maxValue + " Thousand");

        // SLA summary
        System.out.println("\n===== SLA Analysis =====");
        System.out.println("Total Cargo Items         : " + N);
        System.out.println("Truck Capacity            : " + CAPACITY + " tons");
        System.out.println("Optimal Items Selected    : " + selected.size());
        System.out.println("Total Weight Utilised     : " + totalWeight + " tons");
        System.out.println("Maximum Payment Earned    : Rs." + maxValue + " Thousand");

        // Complexity
        System.out.println("\n===== Time and Space Complexity =====");
        System.out.println("DP Table Construction     : O(n x W) = O(" + N + " x " + CAPACITY + ")");
        System.out.println("Item Recovery (Backtrack) : O(n)     = O(" + N + ")");
        System.out.println("Total Time Complexity     : O(n x W)");
        System.out.println("Space Complexity          : O(n x W) = O(" + N + " x " + CAPACITY + ")");
        System.out.println("  where n = " + N + " cargo items, W = " + CAPACITY + " tons capacity");
        System.out.println("==========================================================");
    }
}
