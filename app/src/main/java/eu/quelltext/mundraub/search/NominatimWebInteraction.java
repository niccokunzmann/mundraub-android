package eu.quelltext.mundraub.search;

import java.net.URLEncoder;

import eu.quelltext.mundraub.api.AsyncNetworkInteraction;
import eu.quelltext.mundraub.api.progress.Progress;

public class NominatimWebInteraction extends AsyncNetworkInteraction implements INominatimInteraction {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";

    @Override
    public void search(final String searchTerm, final INominatimCallback cb) {
        final String[] content = {null};
        doAsynchronously(new Callback() {
            @Override
            public void onSuccess() {
                cb.onResult(content[0]);
            }

            @Override
            public void onFailure(int errorResourceString) {
                cb.onError(errorResourceString);
            }
        }, new AsyncOperation() {
            @Override
            protected int operate(Progress progress) throws ErrorWithExplanation {
                try {
                    String url = NOMINATIM_URL + URLEncoder.encode(searchTerm, "UTF-8");
                    content[0] = httpGet(url);
                } catch (Exception e) {
                    return handleExceptionConsistently(e);
                }
                return TASK_SUCCEEDED;
            }
        });
    }
}
