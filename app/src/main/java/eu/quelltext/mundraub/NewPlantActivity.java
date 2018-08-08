package eu.quelltext.mundraub;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import eu.quelltext.mundraub.plant.Plant;
import eu.quelltext.mundraub.plant.PlantCategory;

public class NewPlantActivity extends AppCompatActivity {
    /*
      - intent.putString(NewPlantActivity.ARG_PLANT_ID, plant_id)
        will open this view on a specific plant
        If no value is passed, a new plant is created.
            ARG_PLANT_ID - the id of the plant
     */
    public static final String ARG_PLANT_ID = "plant_id";

    private static final int INTENT_CODE_CHOOSE_PLANT = 0;
    private static final int INTENT_CODE_TAKE_PHOTO = 1;

    private Button buttonPlantType;
    private Button buttonSave;
    private Button buttonCancel;
    private NewPlantActivity me = this;
    private TextView textPosition;
    private TextView textDescription;
    private ImageView plantImage;
    private Plant plant;
    private EditText numberOfPlants;
    private TextView textTip;
    private Button buttonGPS;

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
            autoFillGPSLocation(); // load GPS location for new plants automatically
        }

        buttonPlantType = (Button) findViewById(R.id.button_plant_type);
        buttonSave = (Button) findViewById(R.id.button_save);
        buttonGPS = (Button) findViewById(R.id.button_gps);
        buttonCancel = (Button) findViewById(R.id.button_cancel);
        textPosition = (TextView) findViewById(R.id.text_position);
        textDescription = (TextView) findViewById(R.id.text_description);
        textTip = (TextView) findViewById(R.id.new_plant_explanation);
        plantImage = (ImageView) findViewById(R.id.image_plant);
        numberOfPlants = (EditText) findViewById(R.id.number_of_plants);
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
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // crash? https://stackoverflow.com/questions/1910608/android-action-image-capture-intent
                // from https://stackoverflow.com/a/6485850/1320237
                //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, savedPicture.toURI());
                startActivityForResult(cameraIntent, INTENT_CODE_TAKE_PHOTO);
            }
        });
        buttonGPS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                autoFillGPSLocation();
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plant.save();
                finish();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // from https://stackoverflow.com/a/10833558/1320237
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
        numberOfPlants.setText(Integer.toString(plant.getCount()));
        textDescription.setText(plant.getDescription());
        if (plant.hasPosition()) {
            textPosition.setText(
                    Double.toString(plant.getLongitude()) +
                            " , " +
                            Double.toString(plant.getLatitude()));
        }
        plant.setImageOf(plantImage);
        textTip.setText(plant.hasRequiredFieldsFilled() ? R.string.all_plant_fields_are_filled : R.string.add_new_plant_heading);
    }

    private void autoFillGPSLocation() {
        // from https://stackoverflow.com/a/10917500
        final LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // TODO: or create alert box:
            //       protected void alertbox in http://rdcworld-android.blogspot.com/2012/01/get-current-location-coordinates-city.html
            Log.d("DEBUG", "Access to GPS position is not granted.");
            return;
        }
        if (locationManager == null) {
            return;
        }
        final double oldLatitude = plant.getLatitude();
        final double oldLongitude = plant.getLongitude();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (plant.getLatitude() != oldLatitude || plant.getLongitude() != oldLongitude) {
                            return; // Avoid race conditions of several button presses
                        }
                        me.setLocation(location);
                        locationManager.removeUpdates(this);
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
                });
    }

    private void setLocation(Location location) {
        plant.setLocation(location);
        loadViewFromPlant();
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
                Log.d("CameraDemo", "photo " + photo + " to " + savedPicture);
                FileOutputStream fos = new FileOutputStream(savedPicture);
                photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                plant.setPicture(savedPicture);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            loadViewFromPlant();
        }
    }

    public void setPlantCategory(PlantCategory plantCategory) {
        this.plant.setCategory(plantCategory);
        Log.d("NewPlantActivity", "Set plant category to " + plantCategory.toString());
        loadViewFromPlant();
    }
}
