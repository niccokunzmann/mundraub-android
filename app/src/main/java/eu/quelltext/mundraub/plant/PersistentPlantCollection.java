package eu.quelltext.mundraub.plant;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class PersistentPlantCollection extends PlantCollection {

    private static final String STORAGE_DIRECTORY_NAME = "eu.quelltext.mundraub";
    private static final String JSON_FILE = "plant-json.txt";

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
        loadAllPlants();
    }

    private void loadAllPlants() {
        File directory = persistentDirectory();
        // from https://stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory#8647397
        File[] files = directory.listFiles();
        for (File file : files) {
            File dataFile = new File(file, JSON_FILE);
            if (!file.isFile()) {
                Log.d("LOADING PLANTS", "File " + file.toString() + "can not be used to load a plant.");
                continue;
            }

            try {
                FileInputStream is = new FileInputStream(file);
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                JSONObject json = new JSONObject(jsonTxt);
                Plant plant = new Plant(this, json);
                plant.save();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private File dataFileForPlant(Plant plant) {
        return new File(directoryForPlant(plant), JSON_FILE);
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
