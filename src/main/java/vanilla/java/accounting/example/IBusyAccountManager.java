package vanilla.java.accounting.example;

import java.io.IOException;

/**
 * @author peter.lawrey
 */
public interface IBusyAccountManager extends ILifecycle {
    void initialBalance(int accountNo, int balance, long timeSubmittedNS) throws IOException;

    void transfer(int accountNoFrom, int accountNoTo, int amount, long timeSubmittedNS) throws IOException;
}
