package vanilla.java.accounting.example;

import java.io.IOException;
import java.util.Random;

/**
 * @author peter.lawrey
 */
public class BusyAccountingMain {
    public static final long TRANSACTIONS = Long.getLong("transactions", 1000000);
    public static final String BASE_DIR = System.getProperty("base.dir", "tmp");
    public static final boolean FLUSH = Boolean.parseBoolean(System.getProperty("flush", "false"));

    public static void main(String... args) throws IOException {
        System.out.print("-Dtransactions=" + TRANSACTIONS);
        System.out.print(" -Dbase.dir=" + BASE_DIR);
        System.out.println(" -Dflush=" + FLUSH);
        int accounts = getAccountNumber(TRANSACTIONS);
        int[] nums = generateRandom(accounts);
        long[] successCount = {0L}, insufficientFundsCount = {0L};
        IClock clock = SystemClock.INSTANCE;
        LatencyCounter latencyCounter = new LatencyCounter(50, 10000);
        IBusyAccountJournal listener = new MyBusyAccountJournal(clock, latencyCounter, successCount, insufficientFundsCount);
        IBusyAccountJournal journal = new VanillaBusyAccountJournal(BASE_DIR, FLUSH, listener);
        IBusyAccountManager bam = new VanillaBusyAccountManager(clock, BASE_DIR, accounts, journal, FLUSH, listener);
        int repeats = (int) ((TRANSACTIONS + accounts - 1) / accounts);

        long start = System.nanoTime();
        journal.start();
        bam.start();
        for (int i = 0; i < accounts; i++)
            bam.initialBalance(i, 5000, clock.nanoTime());
        for (int r = 0; r < repeats; r++) {
            for (int i = 0; i < accounts; i++) {
                int to = nums[(i + r) % accounts];
                int amount = nums[(i + r + r) % accounts] & 127;
                bam.transfer(i, to, amount, clock.nanoTime());
            }
        }

        bam.stop();
        journal.stop();
        long time = System.nanoTime() - start;
        double throughput = (accounts + repeats * accounts) / 1e6 / (time / 1e9);
        System.out.printf("Throughput was %.3f million transfers per second%n", throughput);
        System.out.printf("Transfers successful %,d, insufficient funds %,d%n", successCount[0], insufficientFundsCount[0]);
        System.out.printf("Latencies 50/avg/99/99.9/99.99%%tile were %,d/%,d/%,d/%,d/%,d us%n",
                latencyCounter.percentile(0.5),
                latencyCounter.average(),
                latencyCounter.percentile(0.99),
                latencyCounter.percentile(0.999),
                latencyCounter.percentile(0.9999)
        );
    }

    private static int[] generateRandom(int accounts) {
        Random rnd = new Random(1);
        int[] nums = new int[accounts];
        for (int i = 0; i < accounts; i++) nums[i] = rnd.nextInt(accounts);
        return nums;
    }

    private static int getAccountNumber(long transactions) {
        // 1K => 100
        // 1M => 10K
        // 1B => 1M
        // 1T => 100M
        return (int) (Math.min(1000 * 1000 * 1000, Math.ceil(Math.pow(transactions, 2.0 / 3) / 100) * 100));
    }

    private static class MyBusyAccountJournal implements IBusyAccountJournal {
        private final IClock clock;
        private final LatencyCounter latencyCounter;
        private final long[] successCount;
        private final long[] insufficientFundsCount;

        public MyBusyAccountJournal(IClock clock, LatencyCounter latencyCounter, long[] successCount, long[] insufficientFundsCount) {
            this.clock = clock;
            this.latencyCounter = latencyCounter;

            this.successCount = successCount;
            this.insufficientFundsCount = insufficientFundsCount;
        }

        @Override
        public void initialBalance(int accountNo, int balance, long timeSubmittedNS, long timeActionedNS) throws IOException {
//            latencyCounter.sample(clock.nanoTime() - timeSubmittedNS);
        }

        @Override
        public void transferSuccess(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException {
            latencyCounter.sample(clock.nanoTime() - timeSubmittedNS);
            successCount[0]++;
        }

        @Override
        public void insufficientFunds(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException {
            latencyCounter.sample(clock.nanoTime() - timeSubmittedNS);
            insufficientFundsCount[0]++;
        }

        @Override
        public void start() throws IOException {
        }

        @Override
        public void stop() throws IOException {
        }
    }
}
