package eu.quelltext.mundraub.map;

import java.io.IOException;

public interface MundraubProxy {
    void start() throws IOException;
    void stop();
    int getPort();
}
