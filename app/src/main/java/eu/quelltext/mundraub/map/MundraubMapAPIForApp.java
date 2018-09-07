package eu.quelltext.mundraub.map;

import java.io.IOException;

import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.error.Logger;
import okhttp3.OkHttpClient;

public class MundraubMapAPIForApp extends MundraubMapAPI {


    private static MundraubMapAPI instance;
    private final Logger.Log log;

    public MundraubMapAPIForApp() {
        super(Settings.hostForMundraubAPI());
        log = Logger.newFor(this);
    }

    @Override
    protected void handleError(Exception e) {
        log.printStackTrace(e);
    }

    public void start() throws IOException {
        super.start();
        log.d("START", this.getClass().getSimpleName() + " at " +  getUrl());
        //log.d("TEST", httpGet(HttpUrl.parse(getUrl())).body().string());
    }

    public void stop() {
        super.stop();
        log.d("STOP", this.getClass().getSimpleName() + " at " + getUrl());
    }

    @Override
    protected void debug(String s) {
        log.d("debug", s);
    }

    protected OkHttpClient client() {
        log.d("client", "for api");
        return Settings.getOkHttpClient();
    }

}
