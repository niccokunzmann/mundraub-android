package eu.quelltext.mundraub.plant;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.common.Settings;


public class PersistentPlantCollection extends PlantCollection implements Settings.ChangeListener {

    private static final String JSON_FILE = "plant-json.txt";
    private static final String PICTURE_FILE = "plant.jpg";
    private File persistentDirectory = Settings.getPersistentPlantDirectory();

    private boolean allPlantsLoaded = false;

    private File directoryForPlant(Plant plant) {
        File directory = new File(persistentDirectory, plant.getId());
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public PersistentPlantCollection() {
        super();
        ensureAllPlantsAreLoaded();
        Settings.onChange(this);
    }

    private void ensureAllPlantsAreLoaded() {
        if (allPlantsLoaded) {
            return;
        }
        // from https://stackoverflow.com/questions/8646984/how-to-list-files-in-an-android-directory#8647397
        File[] files = persistentDirectory.listFiles();
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
        allPlantsLoaded = true;
    }

    @Override
    public List<Plant> all() {
        ensureAllPlantsAreLoaded();
        return super.all();
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
        updatePlantPicture(plant);
        addPlantToCollection(plant);
    }

    private void updatePlantPicture(Plant plant) {
        File expectedPictureLocation = pictureForPlant(plant);
        if (plant.hasPicture() && !plant.getPicture().equals(expectedPictureLocation)) {
            plant.movePictureTo(expectedPictureLocation);
        }
    }

    private void addPlantToCollection(Plant plant) {
        updatePlantPicture(plant);
        super.save(plant);
    }

    public void delete(Plant plant) {
        super.delete(plant);
        Helper.deleteDir(directoryForPlant(plant));
    }

    @Override
    public int settingsChanged() {
        try {
            moveTo(Settings.getPersistentPlantDirectory());
        } catch (IOException e) {
            e.printStackTrace();
            return R.string.error_could_not_move_plants;
        }
        return SETTINGS_CAN_CHANGE;
    }

    private void moveTo(File newDirectory) throws IOException {
        if (persistentDirectory.equals(newDirectory)) {
            return;
        }
        log.d("move plants", "from " + persistentDirectory.toString() + " to " + newDirectory.toString());
        // FileUtils.moveDirectory(persistentDirectory, newDirectory); // can not move to existing directory
        String[] entries = persistentDirectory.list();
        if (entries != null) {
            // source directory exists
            for (String entry : entries) {
                File source = new File(persistentDirectory, entry);
                File destination = new File(newDirectory, entry);
                log.d("move entry", source.toString() + " to " + destination.toString());
                if (destination.exists()) {
                    if (destination.isFile()) {
                        destination.delete();
                    } else {
                        Helper.deleteDir(destination);
                    }
                }
                if (source.isFile()) {
                    // untested
                    source.renameTo(destination);
                } else {
                    FileUtils.moveDirectoryToDirectory(source, newDirectory, true);
                }
            }
        }
        persistentDirectory = newDirectory;
        allPlantsLoaded = false;
        ensureAllPlantsAreLoaded();
    }
}
