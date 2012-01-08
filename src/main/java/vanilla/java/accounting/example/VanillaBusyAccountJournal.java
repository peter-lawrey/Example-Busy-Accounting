package vanilla.java.accounting.example;

import java.io.*;

/**
 * @author peter.lawrey
 */
public class VanillaBusyAccountJournal implements IBusyAccountJournal {
    private final String baseDir;
    private final boolean flush;
    private final IBusyAccountJournal listener;
    private DataOutputStream journalLog;

    public VanillaBusyAccountJournal(String baseDir, boolean flush, IBusyAccountJournal listener) {
        this.baseDir = baseDir;
        this.flush = flush;
        this.listener = listener;
    }

    @Override
    public void initialBalance(int accountNo, int balance, long timeSubmittedNS, long timeActionedNS) throws IOException {
        logInitialBalance(accountNo, balance, timeSubmittedNS, timeActionedNS);
        listener.initialBalance(accountNo, balance, timeSubmittedNS, timeActionedNS);
        if (flush)
            journalLog.flush();
    }

    @Override
    public void transferSuccess(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException {
        logTransferSuccess(accountNoFrom, accountNoTo, amount, timeSubmittedNS, timeActionedNS);
        listener.transferSuccess(accountNoFrom, accountNoTo, amount, timeSubmittedNS, timeActionedNS);
        if (flush)
            journalLog.flush();
    }

    @Override
    public void insufficientFunds(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException {
        logInsufficientFunds(accountNoFrom, accountNoTo, amount, timeSubmittedNS, timeActionedNS);
        listener.insufficientFunds(accountNoFrom, accountNoTo, amount, timeSubmittedNS, timeActionedNS);
        if (flush)
            journalLog.flush();
    }

    @Override
    public void start() throws IOException {
        new File(baseDir).mkdirs();
        journalLog = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(baseDir + "/journal.log")));
    }

    @Override
    public void stop() throws IOException {
        journalLog.close();
    }

    private void logInitialBalance(int accountNo, int balance, long timeSubmittedNS, long timeActionedNS) throws IOException {
        journalLog.writeByte(RecordTypes.InitialBalance.ordinal());
        journalLog.writeInt(accountNo);
        journalLog.writeInt(balance);
        journalLog.writeLong(timeSubmittedNS);
    }

    private void logInsufficientFunds(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException {
        journalLog.writeByte(RecordTypes.InsufficientFunds.ordinal());
        journalLog.writeInt(accountNoFrom);
        journalLog.writeInt(accountNoTo);
        journalLog.writeInt(amount);
        journalLog.writeLong(timeSubmittedNS);
    }

    private void logTransferSuccess(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException {
        journalLog.writeByte(RecordTypes.TransferSuccess.ordinal());
        journalLog.writeInt(accountNoFrom);
        journalLog.writeInt(accountNoTo);
        journalLog.writeInt(amount);
        journalLog.writeLong(timeSubmittedNS);
    }
}
