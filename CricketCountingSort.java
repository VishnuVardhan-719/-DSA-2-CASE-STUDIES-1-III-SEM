import java.util.*;

class Delivery {
    int over, ball;

    Delivery(int over, int ball) {
        this.over = over;
        this.ball = ball;
    }
}

public class CricketCountingSort {

    // Stable counting sort by ball number
    static Delivery[] countingSortByBall(Delivery[] in) {
        int K = 12; // max ball number
        int[] count = new int[K + 1];

        // Count occurrences
        for (Delivery d : in) {
            count[d.ball]++;
        }

        // Prefix sum
        for (int i = 1; i <= K; i++) {
            count[i] += count[i - 1];
        }

        Delivery[] out = new Delivery[in.length];

        // Reverse order for stability
        for (int i = in.length - 1; i >= 0; i--) {
            Delivery d = in[i];
            out[--count[d.ball]] = d;
        }

        return out;
    }

    // Stable counting sort by over number
    static Delivery[] countingSortByOver(Delivery[] in) {
        int K = 50; // max overs
        int[] count = new int[K + 1];

        // Count occurrences
        for (Delivery d : in) {
            count[d.over]++;
        }

        // Prefix sum
        for (int i = 1; i <= K; i++) {
            count[i] += count[i - 1];
        }

        Delivery[] out = new Delivery[in.length];

        // Reverse order for stability
        for (int i = in.length - 1; i >= 0; i--) {
            Delivery d = in[i];
            out[--count[d.over]] = d;
        }

        return out;
    }

    public static void main(String[] args) {
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
        for (Delivery d : deliveries) {
            System.out.print("(" + d.over + "," + d.ball + ") ");
        }

        // Step 1: Sort by ball
        deliveries = countingSortByBall(deliveries);

        // Step 2: Sort by over
        deliveries = countingSortByOver(deliveries);

        System.out.println("\n\nSorted Deliveries:");
        for (Delivery d : deliveries) {
            System.out.print("(" + d.over + "," + d.ball + ") ");
        }
    }
}