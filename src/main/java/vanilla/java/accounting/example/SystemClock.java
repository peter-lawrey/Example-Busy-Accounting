package vanilla.java.accounting.example;

/**
 * @author peter.lawrey
 */
public enum SystemClock implements IClock {
    INSTANCE;

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
