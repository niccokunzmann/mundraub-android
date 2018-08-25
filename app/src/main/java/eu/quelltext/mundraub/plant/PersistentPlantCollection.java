package eu.quelltext.mundraub.plant;

import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import eu.quelltext.mundraub.common.Helper;

public class PersistentPlantCollection extends PlantCollection {

    private static final String STORAGE_DIRECTORY_NAME = "eu.quelltext.mundraub";
    private static final String JSON_FILE = "plant-json.txt";
    private static final String PICTURE_FILE = "plant.jpg";

    private final File persistentDirectory() {
        // from https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage#7887114
        String root = Environment.getExternalStorageDirectory().toString();
        File directory = new File(root, STORAGE_DIRECTORY_NAME);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                // TODO: We can not acceess this directory. Ask for the permissions. Or not: If there are no permissions, there are no plants?
            }
        }
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
        if (files == null) {
            // TODO: We can not acceess this directory. Ask for the permissions. Or not: If there are no permissions, there are no plants?
            return;
        }
        for (File plantDirectory : files) {
            File file = new File(plantDirectory, JSON_FILE);
            if (!file.isFile()) {
                log.d("LOADING PLANTS", "File " + file.toString() + " can not be used to load a plant.");
                continue;
            }

            try {
                BufferedReader in = new BufferedReader (new InputStreamReader(new FileInputStream(file), "UTF-8"));
                StringBuilder jsonTxt = new StringBuilder();
                while (true) {
                    String s = in.readLine();
                    if (s == null || s.isEmpty()) {
                        break;
                    }
                    jsonTxt.append(s);
                }
                JSONObject json = new JSONObject(jsonTxt.toString());
                Plant plant = new Plant(this, json);
                addPlantToCollection(plant);
                log.d("LOADING PLANTS", "File " + file.toString() + " loaded.");
            } catch (FileNotFoundException e) {
                log.printStackTrace(e);
                log.d("LOADING PLANTS", "File " + file.toString() + " not found.");
            } catch (JSONException e) {
                log.printStackTrace(e);
                log.d("LOADING PLANTS", "File " + file.toString() + " is invalid JSON.");
            } catch (IOException e) {
                log.printStackTrace(e);
                log.d("LOADING PLANTS", "An error occurred while processing " + file.toString() + ".");
            }

        }
    }

    private File dataFileForPlant(Plant plant) {
        return new File(directoryForPlant(plant), JSON_FILE);
    }

    private File pictureForPlant(Plant plant) {
        return new File(directoryForPlant(plant), PICTURE_FILE);
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
            log.printStackTrace(e);
            // TODO: Show errors in log files
            return;
        } catch (JSONException e) {
            log.printStackTrace(e);
            return;
        } catch (IOException e) {
            log.printStackTrace(e);
            return;
        }
        File expectedPicutureLocation = pictureForPlant(plant);
        if (plant.hasPicture() && !plant.getPicture().equals(expectedPicutureLocation)) {
            plant.movePictureTo(expectedPicutureLocation);
        }
        addPlantToCollection(plant);
    }

    private void addPlantToCollection(Plant plant) {
        super.save(plant);
    }

    public void delete(Plant plant) {
        super.delete(plant);
        Helper.deleteDir(directoryForPlant(plant));
    }

}
