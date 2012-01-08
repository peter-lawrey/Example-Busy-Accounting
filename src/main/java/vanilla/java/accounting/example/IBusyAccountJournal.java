package vanilla.java.accounting.example;

import java.io.IOException;

/**
 * @author peter.lawrey
 */
public interface IBusyAccountJournal extends ILifecycle {
    void initialBalance(int accountNo, int balance, long timeSubmittedNS, long timeActionedNS) throws IOException;

    void transferSuccess(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException;

    void insufficientFunds(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS, long timeActionedNS) throws IOException;
}
