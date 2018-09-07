package eu.quelltext.mundraub.map;

import eu.quelltext.mundraub.error.Logger;

/*
 * This class is created as a base class for the app to log all errors occurring.
 * We can use the MundraubMapAPI standalone without Android. This class introduces the first
 * dependencies to the Android environment.
 */
public class ErrorAwareMundraubMapAPI extends MundraubMapAPI {
    private Logger.Log log;

    public ErrorAwareMundraubMapAPI() {
        super();
        log = Logger.newFor(this);
    }

    @Override
    protected void handleError(Exception e) {
        log.printStackTrace(e);
    }
}
