package eu.quelltext.mundraub.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.activities.map.ChooseMapPosition;
import eu.quelltext.mundraub.map.MapCache;
import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;

public class NewPlantActivity extends MundraubBaseActivity {
    /*
      - intent.putString(NewPlantActivity.ARG_PLANT_ID, plant_id)
        will open this view on a specific plant
        If no value is passed, a new plant is created.
            ARG_PLANT_ID - the id of the plant
     */
    public static final String ARG_PLANT_ID = "plant_id";
    public static final String ARG_PLANT_LOCATION_MAP_URL = "plant_location_map_url";

    private static final int INTENT_CODE_CHOOSE_PLANT = 0;
    private static final int INTENT_CODE_TAKE_PHOTO = 1;

    private Button buttonPlantType;
    private Button buttonSave;
    private Button buttonCancel;
    private NewPlantActivity me = this;
    private TextView textPosition;
    private EditText textDescription;
    private ImageView plantImage;
    private Plant plant;
    private EditText numberOfPlants;
    private TextView textTip;
    private Button buttonGPS;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Button mapButton;
    private ImageView mapImage;
    private TextView textLicense;
    private Button buttonPlus;
    private Button buttonMinus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_plant);

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_PLANT_ID)) {
            // load saved state first
            plant = Plant.withId(savedInstanceState.getString(ARG_PLANT_ID));
        } else if (extras != null && extras.containsKey(ARG_PLANT_ID)) {
            // load plant from arguments
            plant = Plant.withId(getIntent().getStringExtra(ARG_PLANT_ID));
        } else {
            plant = new Plant();
            plant.save();
            if (extras != null && extras.containsKey(ARG_PLANT_LOCATION_MAP_URL)) {
                String url = extras.getString(ARG_PLANT_LOCATION_MAP_URL);
                plant.setPositionFromMapUrl(url); // load plant location from map url
            } else {
                autoFillGPSLocation(); // load GPS location for new plants automatically
            }
        }

        buttonPlantType = (Button) findViewById(R.id.button_plant_type);
        buttonSave = (Button) findViewById(R.id.button_save);
        buttonPlus = (Button) findViewById(R.id.button_plus);
        buttonMinus = (Button) findViewById(R.id.button_minus);
        buttonGPS = (Button) findViewById(R.id.button_gps);
        buttonCancel = (Button) findViewById(R.id.button_cancel);
        textPosition = (TextView) findViewById(R.id.text_position);
        textLicense = (TextView) findViewById(R.id.text_map_license);
        textDescription = (EditText) findViewById(R.id.text_description);
        textTip = (TextView) findViewById(R.id.new_plant_explanation);
        plantImage = (ImageView) findViewById(R.id.image_plant);
        numberOfPlants = (EditText) findViewById(R.id.number_of_plants);
        mapButton = (Button) findViewById(R.id.button_map);
        mapImage = (ImageView) findViewById(R.id.image_plant_map);
        buttonPlantType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // form https://developer.android.com/training/basics/firstapp/starting-activity#java
                Intent intent = new Intent(me, ChoosePlantType.class);
                // from https://stackoverflow.com/questions/920306/sending-data-back-to-the-main-activity-in-android#947560
                startActivityForResult(intent, INTENT_CODE_CHOOSE_PLANT);
            }
        });
        plantImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // from https://stackoverflow.com/a/14421798
                if (getPermissions().CAMERA.askIfNotGranted()) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // crash? https://stackoverflow.com/questions/1910608/android-action-image-capture-intent
                    // from https://stackoverflow.com/a/6485850/1320237
                    //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, savedPicture.toURI());
                    startActivityForResult(cameraIntent, INTENT_CODE_TAKE_PHOTO);
                }
            }
        });
        buttonGPS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (getPermissions().ACCESS_FINE_LOCATION.askIfNotGranted()) {
                    autoFillGPSLocation();
                }
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plant.save();
                finish();
            }
        });
        //buttonCancel = null; // TEST ERROR REPORTER
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopGPSUpdates();
                plant.delete();
                finish();
            }
        });
        // from https://stackoverflow.com/a/20824665/1320237
        textDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                plant.setDescription(textDescription.getText().toString());
            }
        });
        numberOfPlants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String text = numberOfPlants.getText().toString();
                if (!text.isEmpty()) {
                    plant.setCount(Integer.parseInt(text));
                }
            }
        });
        final Context context = this;
        View.OnClickListener openMap = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChooseMapPosition.class);
                intent.putExtra(ChooseMapPosition.ARG_PLANT_ID, plant.getId());
                context.startActivity(intent);
            }
        };
        mapButton.setOnClickListener(openMap);
        mapImage.setOnClickListener(openMap);
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plant.setCount(plant.getCount() + 1);
                updatePlantCount();
            }
        });
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newCount = plant.getCount() - 1;
                if (newCount >= 0) {
                    plant.setCount(newCount);
                    updatePlantCount();
                }
            }
        });

        updatePlantCount();
    }

    private void updatePlantCount() {
        setTextAndKeepTheCursorPosition(numberOfPlants, Integer.toString(plant.getCount()));
        if (plant.getCount() <= 0) {
            buttonMinus.setVisibility(View.INVISIBLE);
        } else {
            buttonMinus.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // from https://stackoverflow.com/a/10833558/1320237
        super.onSaveInstanceState(outState);
        outState.putString(ARG_PLANT_ID, plant.getId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadViewFromPlant();
    }

    private void loadViewFromPlant() {
        if (plant.hasCategory()) {
            if (plant.getCategory().isUnknown()) {
                buttonPlantType.setText(R.string.choose_plant_type);
            } else {
                buttonPlantType.setText(plant.getCategory().getResourceId());
            }
        }
        updatePlantCount();
        setTextAndKeepTheCursorPosition(textDescription, plant.getDescription());
        if (plant.hasPosition()) {
            textPosition.setText(
                    Double.toString(plant.getLongitude()) +
                            " , " +
                            Double.toString(plant.getLatitude()));
        }
        plant.setPictureToPlant(plantImage);
        textTip.setText(plant.hasRequiredFieldsFilled() ? R.string.all_plant_fields_are_filled : R.string.plant_has_unfilled_form_fields);
        plant.setPictureToMap(mapImage, new MapCache.Callback() {
            @Override
            public void onSuccess(File file) {
                mapImage.setVisibility(View.VISIBLE);
                textLicense.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure() {
                mapImage.setVisibility(View.GONE);
                textLicense.setVisibility(View.GONE);
            }
        });
    }

    private void setTextAndKeepTheCursorPosition(EditText editText, String text) {
        int selectionStart = editText.getSelectionStart();
        int selectionEnd = editText.getSelectionEnd();
        editText.setText(text);
        editText.setSelection(
                selectionStart < text.length() ? selectionStart : text.length(),
                selectionEnd < text.length() ? selectionEnd : text.length());
    }

    @SuppressLint("MissingPermission")
    private void autoFillGPSLocation() {
        if (!tryCreateLocationManager()) {
            return;
        }
        final double oldLatitude = plant.getLatitude();
        final double oldLongitude = plant.getLongitude();
        stopGPSUpdates();
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (plant.getLatitude() != oldLatitude || plant.getLongitude() != oldLongitude) {
                    return; // Avoid race conditions if the location is determined in another way e.g. by the map
                }
                setLocation(location);
                stopGPSUpdates();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO: Check if this can be used in a meaningful way
            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO: Check if this can be used in a meaningful way
            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO: Check if this can be used in a meaningful way
            }
        };
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean tryCreateLocationManager() {
        if (locationManager != null) {
            return true;
        }
        locationManager = createLocationManager();
        return locationManager != null;
    }

    private void stopGPSUpdates() {
        if (locationManager != null && locationListener != null){
            locationManager.removeUpdates(locationListener);
        }
    }


    private void setLocation(Location location) {
        if (plant.exists()) {
            plant.setLocation(location);
            loadViewFromPlant();
        }
    }

    // This method is called when the second activity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // check that it is the SecondActivity with an OK result
        if (requestCode == INTENT_CODE_CHOOSE_PLANT && resultCode == RESULT_OK) {
            this.setPlantCategory(PlantCategory.fromIntent(intent));
        }
        if (requestCode == INTENT_CODE_TAKE_PHOTO && resultCode == RESULT_OK) {

            Bitmap photo = (Bitmap) intent.getExtras().get("data");
            //plantImage.setImageBitmap(photo);
            try {
                // from https://stackoverflow.com/a/28720264/1320237
                File savedPicture = File.createTempFile("plant", ".jpg", getExternalCacheDir());
                log.d("CameraDemo", "photo " + photo + " to " + savedPicture);
                FileOutputStream fos = new FileOutputStream(savedPicture);
                photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                plant.setPicture(savedPicture);
            } catch (FileNotFoundException e) {
                log.printStackTrace(e);
            } catch (IOException e) {
                log.printStackTrace(e);
            }
            loadViewFromPlant();
        }
    }

    public void setPlantCategory(PlantCategory plantCategory) {
        this.plant.setCategory(plantCategory);
        log.d("NewPlantActivity", "Set plant category to " + plantCategory.toString());
        loadViewFromPlant();
    }
}
