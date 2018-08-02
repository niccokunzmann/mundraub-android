package eu.quelltext.mundraub;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import eu.quelltext.mundraub.plant.PlantCategory;

public class NewPlantActivity extends AppCompatActivity {

    private static final int INTENT_CODE_CHOOSE_PLANT = 0;
    private static final int INTENT_CODE_TAKE_PHOTO = 1;

    private Button buttonPlantType;
    private NewPlantActivity me = this;
    private PlantCategory plantCategory;
    private Location location;
    private TextView textPosition;
    private TextView textDescription;
    private ImageView plantImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_plant);

        buttonPlantType = (Button) findViewById(R.id.button_plant_type);
        textPosition = (TextView) findViewById(R.id.text_position);
        textDescription = (TextView) findViewById(R.id.text_description);
        plantImage = (ImageView) findViewById(R.id.image_plant);
        buttonPlantType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // form https://developer.android.com/training/basics/firstapp/starting-activity#java
                Intent intent = new Intent(me, ChoosePlantType.class);
                // from https://stackoverflow.com/questions/920306/sending-data-back-to-the-main-activity-in-android#947560
                startActivityForResult(intent, INTENT_CODE_CHOOSE_PLANT);
            }
        });
        setLocation();
        plantImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // from https://stackoverflow.com/a/14421798
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(cameraIntent, INTENT_CODE_TAKE_PHOTO);
            }
        });
    }

    private void setLocation() {
        // from https://stackoverflow.com/a/10917500
        LocationManager locationManager = (LocationManager)
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
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        me.setLocation(location);
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
        this.location = location;
        textPosition.setText(
                Double.toString(location.getLongitude()) +
                        " , " +
                Double.toString(location.getLatitude()));
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
            Log.d("CameraDemo", "Pic saved");
        }
    }

    public void setPlantCategory(PlantCategory plantCategory) {
        this.plantCategory = plantCategory;
        Log.d("NewPlantActivity", "Set plant category to " + plantCategory.toString());
        this.buttonPlantType.setText(plantCategory.getResourceId());
    }
}
