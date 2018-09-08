package eu.quelltext.mundraub.api;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.plant.Plant;

public class DummyAPI extends API {

    private int id = 1;

    protected int loginAsync(String username, String password) throws ErrorWithExplanation {
        simulateNetwork();
        if (username.equals("test") && password.equals("test") || username.equals("apptest")) {
            return TASK_SUCCEEDED;
        } else {
            return R.string.invalid_credentials;
        }
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        simulateNetwork();
        return TASK_SUCCEEDED;
    }

    @Override
    protected int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation {
        simulateNetwork();
        return TASK_SUCCEEDED;
    }

    @Override
    protected int setAllPlantMarkersAsync(Progressable progress) throws ErrorWithExplanation {
        return R.string.error_debug_api_can_not_download_offline_data;
    }

    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        simulateNetwork();
        plant.online().publishedWithId(Integer.toString(id++));
        return TASK_SUCCEEDED;
    }

    private int simulateNetwork() throws ErrorWithExplanation {
        try {
            // Simulate network access.
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            abortOperation(TASK_CANCELLED);
        }
        return TASK_SUCCEEDED;
    }


}
