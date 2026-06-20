// Cricket Scoring Service using Counting Sort
// Sorts ball-by-ball delivery records using composite key (over, ball)
// Uses LSD Radix style: first sort by ball number, then by over number

public class CricketCountingSort {

    // Delivery record holding over number and ball number
    static class Delivery {
        int over;
        int ball;

        Delivery(int over, int ball) {
            this.over = over;
            this.ball = ball;
        }

        @Override
        public String toString() {
            return "(" + over + "," + ball + ")";
        }
    }

    // Stable Counting Sort on a specific key (0 = ball, 1 = over)
    static Delivery[] countingSort(Delivery[] arr, int keyType, int maxVal) {
        int n = arr.length;
        int[] count = new int[maxVal + 1];

        // Count occurrences of each key
        for (Delivery d : arr) {
            int key = (keyType == 0) ? d.ball : d.over;
            count[key]++;
        }

        // Compute prefix sums (cumulative counts)
        for (int i = 1; i <= maxVal; i++) {
            count[i] += count[i - 1];
        }

        // Build output array using reverse traversal for stability
        Delivery[] output = new Delivery[n];
        for (int i = n - 1; i >= 0; i--) {
            int key = (keyType == 0) ? arr[i].ball : arr[i].over;
            output[--count[key]] = arr[i];
        }

        return output;
    }

    // Print deliveries in a formatted row
    static void printDeliveries(Delivery[] arr) {
        for (Delivery d : arr) {
            System.out.print(d + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {

        // 10 delivery records (over, ball)
        Delivery[] deliveries = {
            new Delivery(2, 4),
            new Delivery(1, 1),
            new Delivery(3, 6),
            new Delivery(1, 5),
            new Delivery(2, 2),
            new Delivery(3, 1),
            new Delivery(1, 3),
            new Delivery(2, 6),
            new Delivery(3, 4),
            new Delivery(1, 2)
        };

        System.out.println("Unsorted Deliveries:");
        printDeliveries(deliveries);
        System.out.println();

        int maxBall = 6;   // Maximum ball number per over
        int maxOver = 3;   // Maximum over number

        // LSD Radix style:
        // Pass 1: Sort by ball number (least significant key)
        Delivery[] sortedByBall = countingSort(deliveries, 0, maxBall);

        // Pass 2: Sort by over number (most significant key) — stable, preserves ball order
        Delivery[] sortedFinal = countingSort(sortedByBall, 1, maxOver);

        System.out.println("Sorted Deliveries:");
        printDeliveries(sortedFinal);

        System.out.println();
        System.out.println("=== SLA Analysis ===");
        System.out.println("Total Deliveries  = " + deliveries.length);
        System.out.println("Number of Overs   = " + maxOver);
        System.out.println("Maximum Ball No.  = " + maxBall);
        System.out.println("Sorting Passes    = 2");
        System.out.println("Time Complexity   = O(n + k) per pass = O(n + k) total");
    }
}
