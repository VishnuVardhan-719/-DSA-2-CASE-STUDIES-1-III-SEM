import java.util.*;

public class KnapsackCargoLoading {

    public static void main(String[] args) {

        String[] items = {"A", "B", "C", "D", "E", "F", "G", "H"};

        int[] weights = {5, 8, 3, 10, 4, 6, 7, 2};
        int[] values  = {40, 50, 20, 70, 30, 35, 45, 15};

        int W = 24;
        int n = weights.length;

        int[][] dp = new int[n + 1][W + 1];

        // Build DP Table
        for (int i = 1; i <= n; i++) {
            for (int w = 0; w <= W; w++) {

                if (weights[i - 1] <= w) {
                    dp[i][w] = Math.max(
                            dp[i - 1][w],
                            dp[i - 1][w - weights[i - 1]] + values[i - 1]
                    );
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        System.out.println("Maximum Value = ₹" + dp[n][W] + " Thousand");

        // Backtracking
        List<String> selected = new ArrayList<>();
        int w = W;

        for (int i = n; i > 0; i--) {

            if (dp[i][w] != dp[i - 1][w]) {
                selected.add(items[i - 1]);
                w -= weights[i - 1];
            }
        }

        Collections.reverse(selected);

        System.out.println("Selected Items: " + selected);

        int totalWeight = 0;
        int totalValue = 0;

        for (String item : selected) {
            for (int i = 0; i < items.length; i++) {
                if (items[i].equals(item)) {
                    totalWeight += weights[i];
                    totalValue += values[i];
                }
            }
        }

        System.out.println("Total Weight = " + totalWeight + " tons");
        System.out.println("Total Value = ₹" + totalValue + " Thousand");
    }
}