package vanilla.java.accounting.example;

/**
 * @author peter.lawrey
 */
public class LatencyCounter {
    private final int factor;
    private final int factor2;
    private final int[] buckets;
    private int undersize;
    private int oversize;
    private long count = 0;

    public LatencyCounter(int factor, int noBuckets) {
        this.factor = factor;
        this.factor2 = factor / 2;
        this.buckets = new int[noBuckets];
    }

    public void sample(long n) {
        count++;
        if (n < 0) {
            undersize++;
            return;
        }
        long bucket = (n + factor2) / factor;
        if (bucket >= buckets.length) {
            System.out.printf("over: %,d%n" , n);
            oversize++;
            return;
        }
        buckets[((int) bucket)]++;
    }

    public int undersizeCount() {
        return undersize;
    }

    public int oversizeCount() {
        return oversize;
    }

    public long count() {
        return count;
    }

    public long average() {
        // ignoring undersize and oversized values.
        long total = 0;
        for (int i = 1; i < buckets.length; i++) {
            int n = buckets[i];
            total += n * i;
        }
        return total * factor / count;
    }

    public int percentile(double d) {
        long percCount = Math.round((1-d) * count);
        percCount -= oversize;
        if (percCount <= 0) return Integer.MAX_VALUE;
        for (int i = buckets.length - 1; i >= 0; i--) {
            percCount -= buckets[i];
            if (percCount <= 0)
                return i * factor;
        }
        return Integer.MIN_VALUE;
    }
}
