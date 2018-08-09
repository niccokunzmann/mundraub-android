package eu.quelltext.mundraub.map;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import eu.quelltext.mundraub.Helper;
import eu.quelltext.mundraub.plant.Plant;

public class MapCache {

    private static final String CACHE_DIRECTORY = "plant-map-cache";
    private static final String PLANT_IMAGE_TYPE = "png";
    private File allTilesDirectory;

    public MapCache() {

        allTilesDirectory = null;
    }

    public void initilizeOnCacheDirectoryFrom(Context context) {
        if (allTilesDirectory == null) {
            allTilesDirectory = new File(context.getCacheDir(), CACHE_DIRECTORY);
            allTilesDirectory.mkdirs();
        }
    }

    private File directoryOfPlant(Plant plant) {
        File directory = new File(allTilesDirectory, plant.getPathComponent() + "-map");
        directory.mkdirs();
        return directory;
    }

    private File plantLocationFile(Plant plant, Plant.Position position) {
        return new File(directoryOfPlant(plant), position.asId() + "." + PLANT_IMAGE_TYPE);
    }

    public void mapPreviewOf(Plant plant) {
        mapPreviewOf(plant, Callback_NULL);
    }

    public void mapPreviewOf(Plant plant, Callback callback) {
        Plant.Position position = plant.getPosition();
        File positionFile = plantLocationFile(plant, position);
        if (positionFile.exists()) {
            callback.onSuccess(positionFile);
            return;
        }
        Helper.deleteDir(directoryOfPlant(plant));
        Task task = new Task(plant, position, positionFile, callback);
        task.execute();
    }

    private void createPreviewImageFor() {

    }

    private static class Task extends AsyncTask<Void, Void, Boolean> {

        private final Plant plant;
        private final Plant.Position position;
        private final File positionFile;
        private final Callback callback;

        private Task(Plant plant, Plant.Position position, File positionFile, Callback callback) {
            super();
            this.plant = plant;
            this.position = position;
            this.positionFile = positionFile;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = position.getOpenStreetMapExportUrl(PLANT_IMAGE_TYPE);
            Log.d("MAP CACHE", "GET " + url);
            try {
                return Helper.saveUrlToFile(url, positionFile) && plant.getPosition().equals(position);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                callback.onSuccess(positionFile);
            } else {
                callback.onFailure();
            }
        }
    }

    public interface Callback {
        void onSuccess(File file);
        void onFailure();
    }
    public Callback Callback_NULL = new Callback() {
        @Override
        public void onSuccess(File file) {
        }

        @Override
        public void onFailure() {
        }
    };
}
