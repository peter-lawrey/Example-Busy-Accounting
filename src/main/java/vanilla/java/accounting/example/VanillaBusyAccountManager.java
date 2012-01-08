package vanilla.java.accounting.example;

import java.io.*;

/**
 * @author peter.lawrey
 */
public class VanillaBusyAccountManager implements IBusyAccountManager {
    private final IClock clock;
    private final String baseDir;
    private final IBusyAccountJournal journal;

    private final boolean flush;

    private DataOutputStream transferLog;
    private int[] accountBalances;
    private IBusyAccountJournal listener = null;

    public VanillaBusyAccountManager(IClock clock, String baseDir, int accounts, IBusyAccountJournal journal, boolean flush, IBusyAccountJournal listener) {
        this.clock = clock;
        this.baseDir = baseDir;
        this.journal = journal;
        this.flush = flush;
        this.listener = listener;
        accountBalances = new int[accounts];
    }

    @Override
    public void initialBalance(int accountNo, int balance, long timeSubmittedNS) throws IOException {
        logInitialBalance(accountNo, balance, timeSubmittedNS);
        accountBalances[accountNo] = balance;
        if (flush)
        transferLog.flush();
    }

    @Override
    public void transfer(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS) throws IOException {
        logTransfer(accountNoFrom, accountNoTo, amount, timeSubmittedNS);

        if (accountBalances[accountNoFrom] < amount) {
            journal.insufficientFunds(accountNoFrom, accountNoTo, amount, timeSubmittedNS, clock.nanoTime());
        } else {
            accountBalances[accountNoFrom] -= amount;
            accountBalances[accountNoTo] += amount;
            journal.transferSuccess(accountNoFrom, accountNoTo, amount, timeSubmittedNS, clock.nanoTime());
        }
        if (flush)
        transferLog.flush();
    }

    @Override
    public void start() throws IOException {
        new File(baseDir).mkdirs();
        transferLog = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(baseDir + "/transfer.log")));
    }

    @Override
    public void stop() throws IOException {
        transferLog.close();
    }

    private void logInitialBalance(int accountNo, int balance, long timeSubmittedNS) throws IOException {
        transferLog.writeByte(RecordTypes.InitialBalance.ordinal());
        transferLog.writeInt(accountNo);
        transferLog.writeInt(balance);
        transferLog.writeLong(timeSubmittedNS);
    }

    private void logTransfer(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS) throws IOException {
        transferLog.writeByte(RecordTypes.Transfer.ordinal());
        transferLog.writeInt(accountNoFrom);
        transferLog.writeInt(accountNoTo);
        transferLog.writeInt(amount);
        transferLog.writeLong(timeSubmittedNS);
    }
}
