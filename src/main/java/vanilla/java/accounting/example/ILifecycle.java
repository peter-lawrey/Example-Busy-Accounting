package vanilla.java.accounting.example;

import java.io.IOException;

/**
 * @author peter.lawrey
 */
public interface ILifecycle {
    void start() throws IOException;

    void stop() throws IOException;
}
