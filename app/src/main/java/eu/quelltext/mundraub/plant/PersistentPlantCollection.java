package eu.quelltext.mundraub.plant;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class PersistentPlantCollection extends PlantCollection {

    private static final String STORAGE_DIRECTORY_NAME = "eu.quelltext.mundraub";

    private final File persistentDirectory() {
        // from https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage#7887114
        String root = Environment.getExternalStorageDirectory().toString();
        File directory = new File(root, STORAGE_DIRECTORY_NAME);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        Log.d("DEBUG", "Plants are saved and loaded from " + directory.toString());
        return directory;
    }

    private File directoryForPlant(Plant plant) {
        File directory = new File(persistentDirectory(), plant.getId());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public PersistentPlantCollection() {
        super();

    }

    private File dataFileForPlant(Plant plant) {
        return new File(directoryForPlant(plant), "plant-json.txt");
    }

    public void save(Plant plant) {
        // https://stackoverflow.com/a/20117216/1320237
        try {
            JSONObject json = plant.toJSON();
            FileWriter out = new FileWriter(dataFileForPlant(plant));
            out.write(json.toString(2));
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // TODO: Show errors in log files
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        super.save(plant);
    }

    public void delete(Plant plant) {
        super.delete(plant);
        try {
            // from https://stackoverflow.com/a/23678498/1320237
            FileUtils.deleteDirectory(directoryForPlant(plant));
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: add to logs
            return;
        }
    }
}
