package eu.quelltext.mundraub.api;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.progress.Progressable;
import eu.quelltext.mundraub.plant.Plant;

public class NaOvoceAPI extends API {
    @Override
    protected int addPlantAsync(Plant plant) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int loginAsync(String username, String password) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int deletePlantAsync(String plantId) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int updatePlantAsync(Plant plant, String plantId) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }

    @Override
    protected int setAllPlantMarkersAsync(Progressable progress) throws ErrorWithExplanation {
        return R.string.error_not_implemented;
    }
}
